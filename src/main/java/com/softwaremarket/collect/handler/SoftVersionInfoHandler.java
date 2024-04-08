package com.softwaremarket.collect.handler;

import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.collect.config.GiteeUrlConfig;
import com.softwaremarket.collect.config.PremiumAppConfig;
import com.softwaremarket.collect.config.PulllRequestConfig;
import com.softwaremarket.collect.config.ForkConfig;
import com.softwaremarket.collect.dto.TreeEntryExpandDto;
import com.softwaremarket.collect.enums.CommitInfoEnum;
import com.softwaremarket.collect.enums.TreeTypeEnum;
import com.softwaremarket.collect.service.IGiteeService;
import com.softwaremarket.collect.util.DateTimeStrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class SoftVersionInfoHandler {
    private final IGiteeService giteeService;
    private final PremiumAppConfig premiumAppConfig;
    private final PulllRequestConfig pulllRequestConfig;
    private final ForkConfig forkConfig;
    private final GiteeUrlConfig giteeUrlConfig;

    public void handlePremiumApp(JSONObject upObj, JSONObject communityObj) {
        String community_latest_version = communityObj.getString("latest_version");
        String upObj_latest_version = upObj.getString("latest_version");
        String os_version = communityObj.getString("os_version");
        // 需要更新的软件 name
        String name = communityObj.getString("name");
        /*communityObj.put("os_version", "22.03-lts");

        communityObj.put("latest_version", "6.2.7");*/


        String prTitle = String.format(pulllRequestConfig.getTitleTemplate(), name + "-" + communityObj.getString("os_version"), community_latest_version, upObj_latest_version);

        // 版本相同或者已经提交过相同pr将不再处理
        if (upObj_latest_version.equals(community_latest_version) ||
                checkHasCreatePR(prTitle))
            return;


        //获取fork后的仓库信息
        List<JSONObject> contents = giteeService.getContents(forkConfig.getOwner(), premiumAppConfig.getRepo(), "/", forkConfig.getAccessToken(), "master");

        //代码提交、pr的源分支
        String handleBranch = name + "软件市场自动升级" + DateTimeStrUtils.getTodayDate();

        if (CollectionUtils.isEmpty(contents)) {
            // fork仓库作为中间操作仓库
            JSONObject forkedObj = forkStore();

            contents = giteeService.getContents(forkConfig.getOwner(), premiumAppConfig.getRepo(), "/", forkConfig.getAccessToken(), "master");
            if (CollectionUtils.isEmpty(forkedObj) || CollectionUtils.isEmpty(contents)) {
                log.info("精品仓库fork失败");
                return;
            }

        }

        //筛选需要更新的镜像文件夹
        contents = contents.stream().filter(a -> {
            return name.equals(a.getString("name"));
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(contents)) {
            return;
        }
        JSONObject currentAppTreeInfo = contents.get(0);


        // 判断master分支当前app版本是否已经存在
        if (checkHasExsitedVersion(currentAppTreeInfo.getString("sha"), os_version, upObj_latest_version))
            return;

        // 判断master中是否有升级需要的原始信息
        if (!checkHasExsitedVersion(currentAppTreeInfo.getString("sha"), os_version, community_latest_version))
            return;


        // 创建当前操作的分支
        RepoBranchesBody repoBranchesBody = new RepoBranchesBody();
        repoBranchesBody.setRefs("master");
        repoBranchesBody.branchName(handleBranch);
        giteeService.postReposOwnerRepoBranches(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumAppConfig.getRepo(), repoBranchesBody);


        contents = giteeService.getContents(forkConfig.getOwner(), premiumAppConfig.getRepo(), "/", forkConfig.getAccessToken(), handleBranch);


        if (!CollectionUtils.isEmpty(contents)) {
            contents = contents.stream().filter(a -> {
                return name.equals(a.getString("name"));
            }).collect(Collectors.toList());
            JSONObject object = contents.get(0);
            //获取文件数
            List<TreeEntryExpandDto> treeBlob = getTreeBlob(object.getString("name"), premiumAppConfig.getRepo(), object.getString("sha"));
            //获取committbody
            RepoCommitsBody treeRepoCommitsBody = getTreeRepoCommitsBody(treeBlob, communityObj.getString("os_version").toUpperCase(Locale.ROOT), upObj.getString("latest_version"));
            //对commit信息处理
            handleTreeRepoCommitsBody(handleBranch, treeRepoCommitsBody, communityObj.getString("latest_version"), upObj.getString("latest_version"));
            //提交commit
            RepoCommitWithFiles repoCommitWithFiles = giteeService.postReposOwnerRepoCommits(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumAppConfig.getRepo(), treeRepoCommitsBody);
            if (repoCommitWithFiles != null) {
                log.info("文件commit更新成功：" + repoCommitWithFiles.getCommentsUrl());
                // 创建issue
                Issue issue = createIssue(forkConfig.getAccessToken(), premiumAppConfig.getOwner(), prTitle, premiumAppConfig.getRepo());
                //提交pr并和issue关联
                PullRequest pullRequest = giteeService.postReposOwnerRepoPulls(forkConfig.getAccessToken(), premiumAppConfig.getOwner(), premiumAppConfig.getRepo(), createRepoPullsBody(issue, handleBranch));
                log.info("pr 已提交：" + pullRequest);

            }

        }

    }


    private RepoPullsBody createRepoPullsBody(Issue issue, String branch) {
        RepoPullsBody body = new RepoPullsBody();
        //会根据issue的title和body去填充pr的
        body.setTitle("");
        body.setIssue(issue.getNumber());
        body.setHead(forkConfig.getOwner() + ":" + branch);
        body.setBase(CommitInfoEnum.PremiumApp.getBranch());
        //将pr和issue关联
        body.setBody("#" + issue.getNumber());
        //合并pr后删除源分支
        body.pruneSourceBranch(Boolean.TRUE);

        //合并pr后关闭issue
        body.closeRelatedIssue(Boolean.TRUE);
        return body;
    }


    private void handleTreeRepoCommitsBody(String branch, RepoCommitsBody treeRepoCommitsBody, String pemiumAppOpeneulerVersion, String latestVersion) {
        treeRepoCommitsBody.setBranch(branch);
        List<GitAction> actions = treeRepoCommitsBody.getActions();
        for (int i = 0; i < actions.size(); i++) {
            GitAction gitAction = actions.get(i);
            String path = gitAction.getPath();
            gitAction.setPath(path.replace(pemiumAppOpeneulerVersion, latestVersion));
            if (!path.contains(pemiumAppOpeneulerVersion)) {
                actions.remove(i);
                i--;
                continue;
            }
            if (path.endsWith("Dockerfile")) {
                String content = gitAction.getContent();
                content = content.replace(pemiumAppOpeneulerVersion, latestVersion);
                gitAction.setContent(content);
            }

        }
    }

    // 判断精品仓库是否存在有当前版本镜像
    private Boolean checkHasExsitedVersion(String sha, String openeulerVersion, String appVersion) {
        Tree reposOwnerRepoGitTreesSha = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumAppConfig.getRepo(), sha, 56);
        if (reposOwnerRepoGitTreesSha != null) {
            List<TreeEntry> treesSha = reposOwnerRepoGitTreesSha.getTree();
            for (TreeEntry treeEntry : treesSha) {
                if (TreeTypeEnum.TREE.getType().equals(treeEntry.getType()) && treeEntry.getPath().equals(appVersion)) {
                    String treeSha = treeEntry.getSha();
                    Tree suggTree = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumAppConfig.getRepo(), treeSha, 56);
                    if (suggTree != null) {
                        List<TreeEntry> tree = suggTree.getTree();
                        for (TreeEntry entry : tree) {
                            String path = entry.getPath();
                            if (path.contains("-sp"))
                                path = path.replace("lts", "");
                            path = "oe" + path.replace(".", "").replace("-", "");
                            System.out.println("path:" + path);
                            if (TreeTypeEnum.TREE.getType().equals(entry.getType()) && (entry.getPath().equals(openeulerVersion) || path.equals(openeulerVersion)))
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    private Boolean checkIsNeedUpgrade(String sha, String openeulerVersion, String uplatestVersion) {

        Tree reposOwnerRepoGitTreesSha = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumAppConfig.getRepo(), sha, 56);
        if (reposOwnerRepoGitTreesSha != null) {
            List<TreeEntry> treesSha = reposOwnerRepoGitTreesSha.getTree();
            for (TreeEntry treeEntry : treesSha) {
                if (TreeTypeEnum.TREE.getType().equals(treeEntry.getType()) && treeEntry.getPath().equals(uplatestVersion)) {
                    String treeSha = treeEntry.getSha();
                    Tree suggTree = giteeService.getReposOwnerRepoGitTreesSha(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumAppConfig.getRepo(), treeSha, 56);
                    if (suggTree != null) {
                        List<TreeEntry> tree = suggTree.getTree();
                        for (TreeEntry entry : tree) {
                            String path = entry.getPath();
                            if (path.contains("-sp"))
                                path.replace("lts", "");
                            path = path.replace(".", "").replace("-", "");
                            if (TreeTypeEnum.TREE.getType().equals(entry.getType()) && path.equals(openeulerVersion))
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // 创建issue
    public Issue createIssue(String token, String owner, String title, String repo) {
        Issue issue = null;
        OwnerIssuesBody ownerIssuesBody = new OwnerIssuesBody();
        ownerIssuesBody.setTitle(title);
        ownerIssuesBody.setRepo(repo);
        issue = giteeService.createIssue(token, owner, ownerIssuesBody);
        System.out.println(issue);
        if (issue != null) {
            return issue;
        }

        return null;
    }


    // 组装文件commit body
    private RepoCommitsBody getTreeRepoCommitsBody(List<TreeEntryExpandDto> treeBlob, String osversion, String upLatestVersion) {
        RepoCommitsBody repoCommitsBody = new RepoCommitsBody();
        List<GitAction> gitActionList = new ArrayList<>();
        repoCommitsBody.setActions(gitActionList);
        repoCommitsBody.setBranch(CommitInfoEnum.PremiumApp.getBranch());
        repoCommitsBody.setMessage(String.format(CommitInfoEnum.PremiumApp.getMessage(), osversion, upLatestVersion));
        GitUserBasic gitUserBasic = new GitUserBasic();
        gitUserBasic.setEmail(forkConfig.getEmail());
        gitUserBasic.setName(forkConfig.getName());
        repoCommitsBody.setAuthor(gitUserBasic);

        getGitActions(gitActionList, treeBlob);
        return repoCommitsBody;
    }

    // 组装需要提交的每个文件
    private void getGitActions(List<GitAction> gitActionList, List<TreeEntryExpandDto> treeBlob) {
        for (TreeEntryExpandDto treeEntryExpandDto : treeBlob) {
            String type = treeEntryExpandDto.getType();
            if (TreeTypeEnum.BLOB.getType().equals(type)) {

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
            } else if (TreeTypeEnum.TREE.getType().equals(type)) {
                getGitActions(gitActionList, treeEntryExpandDto.getNext());
            }
        }

    }

    // 获取文件树
    private List<TreeEntryExpandDto> getTreeBlob(String path, String rep, String sha) {
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
    private Boolean checkHasCreatePR(String prTitle) {
        List<JSONObject> v5ReposOwnerRepoPulls = new ArrayList<>();
        int page = 0;
        String url = giteeUrlConfig.getGiteeGetV5ReposOwnerRepoPullsUrl().replace("{owner}", premiumAppConfig.getOwner()).replace("{repo}", premiumAppConfig.getRepo()).replace("{access_token}", forkConfig.getAccessToken());
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
    private JSONObject forkStore() {
        HashMap<Object, Object> parameter = new HashMap<>();
        parameter.put("owner", premiumAppConfig.getOwner());
        parameter.put("repo", premiumAppConfig.getRepo());
        parameter.put("access_token", forkConfig.getAccessToken());
        parameter.put("name", premiumAppConfig.getRepo());
        parameter.put("path", premiumAppConfig.getRepo());
        return giteeService.fork(parameter);
    }


}
