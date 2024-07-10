package com.softwaremarket.autoupgrade.handler;

import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.dto.TreeEntryExpandDto;
import com.softwaremarket.autoupgrade.enums.GiteeRepoEnum;
import com.softwaremarket.autoupgrade.enums.GiteeUrlEnum;
import com.softwaremarket.autoupgrade.enums.TreeTypeEnum;
import com.softwaremarket.autoupgrade.service.IGiteeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component

public class BaseCommonUpdateHandler {
    @Autowired
    protected IGiteeService giteeService;

    ForkInfoDto forkConfig;

    // 判断精品仓库是否存在有当前版本镜像
    protected Boolean checkHasExsitedVersion(String sha, String osVersion, String appVersion) {
        Tree reposOwnerRepoGitTreesSha = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), GiteeRepoEnum.PREMIUMAPP.getRepo(), sha, 56);
        if (reposOwnerRepoGitTreesSha != null) {
            List<TreeEntry> treesSha = reposOwnerRepoGitTreesSha.getTree();
            for (TreeEntry treeEntry : treesSha) {
                if (TreeTypeEnum.TREE.getType().equals(treeEntry.getType()) && treeEntry.getPath().equals(appVersion)) {
                    String treeSha = treeEntry.getSha();
                    Tree suggTree = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), GiteeRepoEnum.PREMIUMAPP.getRepo(), treeSha, 56);
                    if (suggTree != null) {
                        List<TreeEntry> tree = suggTree.getTree();
                        for (TreeEntry entry : tree) {
                            String path = entry.getPath();
                            if (path.contains("-sp"))
                                path = path.replace("lts", "");
                            path = "oe" + path.replace(".", "").replace("-", "");
                            if (TreeTypeEnum.TREE.getType().equals(entry.getType()) && (entry.getPath().equals(osVersion) || path.equals(osVersion)))
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    // 创建issue
    protected Issue createIssue(String token, String owner, String title, String repo, String number) {
        Issue issue = null;
        if (number == null) {
            OwnerIssuesBody ownerIssuesBody = new OwnerIssuesBody();
            ownerIssuesBody.setTitle(title);
            ownerIssuesBody.setRepo(repo);
            issue = giteeService.createIssue(token, owner, ownerIssuesBody);
            System.out.println(issue);
            if (issue != null) {
                return issue;
            }
        } else {
            issue = new Issue();
            issue.setNumber(number);
            issue.setTitle(title);
        }
        return issue;
    }


    // 组装文件commit body
    protected RepoCommitsBody getTreeRepoCommitsBody(String message, String branch) {
        RepoCommitsBody repoCommitsBody = new RepoCommitsBody();
        List<GitAction> gitActionList = new ArrayList<>();
        repoCommitsBody.setActions(gitActionList);
        repoCommitsBody.setBranch(branch);
        repoCommitsBody.setMessage(message);
        GitUserBasic gitUserBasic = new GitUserBasic();
        gitUserBasic.setEmail(forkConfig.getEmail());
        gitUserBasic.setName(forkConfig.getName());
        repoCommitsBody.setAuthor(gitUserBasic);

        //getGitActions(gitActionList, treeBlob);
        return repoCommitsBody;
    }

    // 组装需要提交的每个文件
    protected void getGitActions(List<GitAction> gitActionList, List<TreeEntryExpandDto> treeBlob) {
        for (TreeEntryExpandDto treeEntryExpandDto : treeBlob) {
            String type = treeEntryExpandDto.getType();
            if (TreeTypeEnum.TREE.getType().equals(type)) {
                getGitActions(gitActionList, treeEntryExpandDto.getNext());

            } else {
                GitAction gitAction = new GitAction();
                gitActionList.add(gitAction);
                gitAction.setAction(GitAction.ActionEnum.CREATE);
                gitAction.setEncoding(GitAction.EncodingEnum.TEXT);
                gitAction.setPath(treeEntryExpandDto.getPath());
                try {
                    gitAction.setContent(FileUtils.readFileToString(treeEntryExpandDto.getFile(), StandardCharsets.UTF_8));
                    //删除下载的文件
                    FileUtils.forceDelete(treeEntryExpandDto.getFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 获取文件树
    protected List<TreeEntryExpandDto> getTreeBlob(String path, String rep, String sha) {
        List<TreeEntryExpandDto> treeEntryExpandDtoList = new ArrayList<>();
        Tree reposOwnerRepoGitTreesSha = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), rep, sha, 56);
        List<TreeEntry> treesSha = reposOwnerRepoGitTreesSha.getTree();
        if (CollectionUtils.isEmpty(treesSha))
            return treeEntryExpandDtoList;

        for (int i = 0; i < treesSha.size(); i++) {
            TreeEntry treeEntry = treesSha.get(i);
            String type = treeEntry.getType();
            TreeEntryExpandDto treeEntryExpandDto = new TreeEntryExpandDto(treeEntry);
            treeEntryExpandDtoList.add(treeEntryExpandDto);
            String newPath = path + "/" + treeEntry.getPath();
            if (TreeTypeEnum.BLOB.getType().equals(type)) {
                System.out.println("path:" + newPath);
                File file = giteeService.getReposOwnerRepoRawPath(forkConfig.getAccessToken(), forkConfig.getOwner(), rep, newPath, null);
                treeEntryExpandDto.setFile(file);

                System.out.println("file:" + file.getPath());
                treeEntryExpandDto.setPath(newPath);
            } else if (TreeTypeEnum.TREE.getType().equals(type)) {
                System.out.println(newPath);
                treeEntryExpandDto.setNext(getTreeBlob(newPath, rep, treeEntry.getSha()));
            }

        }
        return treeEntryExpandDtoList;
    }

    //根据openeuler版本、name、前后软化版本判断当前升级是否已经提交过pr
    protected Boolean checkHasCreatePR(String prTitle, String owner, String repo, String token) {
        List<JSONObject> v5ReposOwnerRepoPulls = new ArrayList<>();
        int page = 0;
        String url = GiteeUrlEnum.GiteeGetV5ReposOwnerRepoPullsUrl.getUrl().replace("{owner}", owner).replace("{repo}", repo).replace("{access_token}", token);
        do {
            page++;
            v5ReposOwnerRepoPulls.addAll(giteeService.getV5ReposOwnerRepoPulls(url.replace("{page}", String.valueOf(page))));
        } while (v5ReposOwnerRepoPulls.size() == page * 100);
        if (!CollectionUtils.isEmpty(v5ReposOwnerRepoPulls)) {
            for (JSONObject v5ReposOwnerRepoPull : v5ReposOwnerRepoPulls) {
                String title = v5ReposOwnerRepoPull.getString("title");
                if (!StringUtils.isEmpty(title) &&
                        title.equals(prTitle)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }


    // fork 代码仓库到forkConfig.getAccessToken()的代码仓库
    protected JSONObject forkStore(String owner, String repo, String accessToken, String name, String path) {
        HashMap<Object, Object> parameter = new HashMap<>();
        parameter.put("owner", owner);
        parameter.put("repo", repo);
        parameter.put("access_token", accessToken);
        parameter.put("name", name);
        parameter.put("path", path);
        return giteeService.fork(parameter);
    }


    protected RepoPullsBody createRepoPullsBody(Issue issue, String headBranch, String baseBranch) {
        RepoPullsBody body = new RepoPullsBody();
        //会根据issue的title和body去填充pr的
        body.setTitle(issue.getTitle());
        // body.setIssue(issue.getNumber());
        body.setHead(headBranch);
        body.setBase(baseBranch);
        //将pr和issue关联
        body.setBody("#" + issue.getNumber());
        //合并pr后删除源分支
        /* body.pruneSourceBranch(Boolean.TRUE);*/

        //合并pr后关闭issue
        body.closeRelatedIssue(Boolean.FALSE);
        return body;
    }
}
