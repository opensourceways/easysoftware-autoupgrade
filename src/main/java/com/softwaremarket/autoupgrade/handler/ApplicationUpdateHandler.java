package com.softwaremarket.autoupgrade.handler;

import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.autoupgrade.config.ApplicationConfig;
import com.softwaremarket.autoupgrade.dto.ApplicationUpdateInfoDto;
import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.dto.PrInfoDto;
import com.softwaremarket.autoupgrade.dto.TreeEntryExpandDto;
import com.softwaremarket.autoupgrade.enums.CommitInfoEnum;
import com.softwaremarket.autoupgrade.enums.GiteeRepoEnum;
import com.softwaremarket.autoupgrade.service.IGiteeService;
import com.softwaremarket.autoupgrade.util.DateTimeStrUtils;
import com.softwaremarket.autoupgrade.util.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
@EnableAsync

public class ApplicationUpdateHandler extends BaseCommonUpdateHandler {
    @Autowired
    private IGiteeService giteeService;
    @Autowired
    private ApplicationConfig applicationConfig;

    private PrInfoDto pulllRequestConfig;
    private ForkInfoDto forkConfig;


    ApplicationUpdateHandler(ApplicationConfig applicationConfig) {
        this.pulllRequestConfig = applicationConfig.getPrInfo();
        this.forkConfig = applicationConfig.getForkInfo();
        super.forkConfig = applicationConfig.getForkInfo();
    }

    public void batchUpdatePremiumApp(List<String> allOsVersionList, ApplicationUpdateInfoDto premiumAppUpdateInfo) {
        for (String osversion : allOsVersionList) {
            try {
                String versionNewFormat = osversion + "";
                if (versionNewFormat.contains("-sp")) {
                    versionNewFormat = versionNewFormat.replace("lts", "");
                }
                versionNewFormat = "oe" + versionNewFormat.replace(".", "").replace("-", "");
                if (versionNewFormat.equals(premiumAppUpdateInfo.getCommunityCurrentOsVersion())) {
                    continue;
                }
                premiumAppUpdateInfo.setCommunityOtherOsVersion(osversion);
                handlePremiumApp(premiumAppUpdateInfo);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }


    public void handlePremiumApp(ApplicationUpdateInfoDto premiumAppUpdateInfo) {
        String oeAppLatestVersion = premiumAppUpdateInfo.getOeAppLatestVersion();
        String upAppLatestVersion = premiumAppUpdateInfo.getUpAppLatestVersion();
        String communityOsVersion = premiumAppUpdateInfo.getCommunityCurrentOsVersion();
        String communityOtherOsVersion = premiumAppUpdateInfo.getCommunityOtherOsVersion();
        // 需要更新的软件 name
        String name = premiumAppUpdateInfo.getAppName();

        String prTitle = String.format(pulllRequestConfig.getPrTitle(), name, (communityOtherOsVersion == null ? communityOsVersion : communityOtherOsVersion), name + " " + upAppLatestVersion);
        String giteeOwner = GiteeRepoEnum.PREMIUMAPP.getOwner();
        String giteeRepo = GiteeRepoEnum.PREMIUMAPP.getRepo();
        String token = forkConfig.getAccessToken();
        // 版本相同或者已经提交过相同pr将不再处理
        if (upAppLatestVersion.equals(oeAppLatestVersion) || checkHasCreatePR(prTitle, giteeOwner, giteeRepo, token))
            return;


        //获取fork后的仓库信息
        List<JSONObject> contents = giteeService.getContents(forkConfig.getOwner(), giteeRepo, "/", token, CommitInfoEnum.PremiumApp.getBranch());

        //代码提交、pr的源分支
        String handleBranch = name + "-" + (communityOtherOsVersion == null ? communityOsVersion : communityOtherOsVersion) + "软件市场自动升级" + DateTimeStrUtils.getTodayDate();

        if (CollectionUtils.isEmpty(contents)) {
            // fork仓库作为中间操作仓库
            JSONObject forkedObj = forkStore(giteeOwner, giteeRepo, token, giteeRepo, giteeRepo);
            try {
                //线程休眠 fork仓库有延迟
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            contents = giteeService.getContents(forkConfig.getOwner(), giteeRepo, "/", token, CommitInfoEnum.PremiumApp.getBranch());
            if (CollectionUtils.isEmpty(forkedObj) || CollectionUtils.isEmpty(contents)) {
                log.info("精品仓库fork失败");
                return;
            }

        } else {
            // 强制更新代码

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
        if (checkHasExsitedVersion(currentAppTreeInfo.getString("sha"), communityOtherOsVersion == null ? communityOsVersion : communityOtherOsVersion, upAppLatestVersion))
            return;

        // 判断master中是否有升级需要的原始信息
        if (!checkHasExsitedVersion(currentAppTreeInfo.getString("sha"), communityOsVersion, oeAppLatestVersion))
            return;


        // 创建当前操作的分支
        RepoBranchesBody repoBranchesBody = new RepoBranchesBody();
        repoBranchesBody.setRefs("master");
        repoBranchesBody.branchName(handleBranch);
        giteeService.postReposOwnerRepoBranches(token, forkConfig.getOwner(), giteeRepo, repoBranchesBody);

        try {
            //线程休眠 gitee有延迟
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        contents = giteeService.getContents(forkConfig.getOwner(), giteeRepo, "/", forkConfig.getAccessToken(), handleBranch);


        if (!CollectionUtils.isEmpty(contents)) {
            contents = contents.stream().filter(a -> {
                return name.equals(a.getString("name"));
            }).collect(Collectors.toList());
            JSONObject object = contents.get(0);
            //获取文件树
            List<TreeEntryExpandDto> treeBlob = getTreeBlob(object.getString("name"), giteeRepo, object.getString("sha"));
            //获取committbody
            RepoCommitsBody treeRepoCommitsBody = getTreeRepoCommitsBody(String.format(CommitInfoEnum.PremiumApp.getMessage(), communityOtherOsVersion == null ? communityOsVersion.toLowerCase(Locale.ROOT) : communityOtherOsVersion.toLowerCase(Locale.ROOT), name, upAppLatestVersion), CommitInfoEnum.PremiumApp.getBranch());
            //对每一个文件提交信息出理
            getGitActions(treeRepoCommitsBody.getActions(), treeBlob);
            //对commit信息处理
            handleTreeRepoCommitsBody(handleBranch, treeRepoCommitsBody, oeAppLatestVersion, upAppLatestVersion, communityOsVersion, communityOtherOsVersion);
            //提交commit
            RepoCommitWithFiles repoCommitWithFiles = giteeService.postReposOwnerRepoCommits(token, forkConfig.getOwner(), giteeRepo, treeRepoCommitsBody);
            if (repoCommitWithFiles != null) {
                log.info("文件commit更新成功：" + repoCommitWithFiles.getCommentsUrl());
                // 创建issue
                Issue issue = createIssue(token, giteeOwner, prTitle, giteeRepo, pulllRequestConfig.getIssueNum());
                //提交pr并和issue关联
                PullRequest pullRequest = giteeService.postReposOwnerRepoPulls(token, giteeOwner, giteeRepo, createRepoPullsBody(issue, forkConfig.getOwner() + ":" + handleBranch, CommitInfoEnum.PremiumApp.getBranch()));
                log.info("pr 已提交：" + pullRequest);
            }

            EmailSenderUtil.applicationSednMailMap.add(EmailSenderUtil.applicationMailMap.get(name), getPRinfoByPrTitle(prTitle, giteeOwner, giteeRepo, token));

        }

    }


    private void handleTreeRepoCommitsBody(String branch, RepoCommitsBody treeRepoCommitsBody, String currentVersion, String latestVersion, String currentOsversion, String latestOsVersion) {
        treeRepoCommitsBody.setBranch(branch);
        List<GitAction> actions = treeRepoCommitsBody.getActions();
        String dockfilepath = "";
        GitAction metaGitAction = null;
        for (int i = 0; i < actions.size(); i++) {
            GitAction gitAction = actions.get(i);
            String path = gitAction.getPath();
            if (path.endsWith("meta.yml")) {
                metaGitAction = gitAction;
                actions.remove(i);
                i--;
                continue;
            }
            String longCurrentOsVersion = "";
            //更新文件路径
            String replacePath = path.replace(currentVersion, latestVersion);
            if (latestOsVersion != null) {
                String[] split = replacePath.split("/");
                replacePath = "";
                for (int m = 0; m < split.length; m++) {
                    String s = split[m];
                    if (s.contains("-sp"))
                        s = s.replace("-lts-", "");
                    s = s.replace(".", "").replace("-", "");
                    if (currentOsversion.contains(s)) {
                        longCurrentOsVersion = split[m];
                        split[m] = latestOsVersion;
                    }

                    replacePath = replacePath + "/" + split[m];
                }
            }

            if (path.contains("-sp"))
                path = path.replace("lts", "");
            path = path.replace(".", "").replace("-", "");
            if ((!path.contains(currentVersion.replace(".", "")))
                    || (!path.contains(currentOsversion.split("oe")[1]))) {
                actions.remove(i);
                i--;
                continue;
            }
            gitAction.setPath(replacePath);

            //更改dockerfile
            if (path.endsWith("Dockerfile")) {
                String content = gitAction.getContent();
                content = content.replace(currentVersion, latestVersion);
                if (latestOsVersion != null) {
                    content = content.replace(longCurrentOsVersion, latestOsVersion);
                }
                dockfilepath = gitAction.getPath();
                gitAction.setContent(content);
            }


        }
        //update meta.yml
        if (metaGitAction != null) {
            metaGitAction.setAction(GitAction.ActionEnum.UPDATE);
            StringBuilder content = new StringBuilder(metaGitAction.getContent());
            content.append("\n").append(latestVersion).append("-").append(currentOsversion).append(":").append("\n").append("  path: ").append(dockfilepath);
            metaGitAction.setContent(content.toString());
            actions.add(metaGitAction);
        }

    }

}
