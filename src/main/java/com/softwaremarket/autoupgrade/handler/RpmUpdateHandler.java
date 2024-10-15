package com.softwaremarket.autoupgrade.handler;

import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.autoupgrade.config.RpmConfig;
import com.softwaremarket.autoupgrade.dto.PrInfoDto;
import com.softwaremarket.autoupgrade.enums.CommitInfoEnum;
import com.softwaremarket.autoupgrade.enums.GiteeRepoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class RpmUpdateHandler extends BaseCommonUpdateHandler {
    private PrInfoDto pulllRequestConfig;

    RpmUpdateHandler(RpmConfig rpmConfig) {
        this.pulllRequestConfig = rpmConfig.getPrInfo();
        this.forkConfig = rpmConfig.getForkInfo();
        super.forkConfig = rpmConfig.getForkInfo();
    }

    public void handleRpm(JSONObject upObj, JSONObject communityObj) {
        String community_latest_version = communityObj.getString("latest_version");
        community_latest_version = "8.4.0";
        String upObj_latest_version = upObj.getString("latest_version");
        upObj_latest_version = "8.7.1";
        String os_version = communityObj.getString("os_version");
        os_version = "openEuler-24.03-LTS";
        // 需要更新的软件 name
        String name = "curl";  //communityObj.getString("name");


        String branch = CommitInfoEnum.PremiumApp.getBranch();
        String prTitle = String.format(pulllRequestConfig.getPrTitle(), name /*+ "-" + os_version*/, community_latest_version, upObj_latest_version);

        String giteeOwner = GiteeRepoEnum.RPM.getOwner();

        // 版本相同或者已经提交过相同pr将不再处理
        if (upObj_latest_version.equals(community_latest_version) ||
                checkHasCreatePR(prTitle, giteeOwner, name, forkConfig.getAccessToken())) {
            return;
        }


        //获取fork后的仓库信息
        List<JSONObject> contents = giteeService.getContents(forkConfig.getOwner(), name, "/", forkConfig.getAccessToken(), branch);

        //代码提交、pr的源分支
        String handleBranch = branch;

        if (CollectionUtils.isEmpty(contents)) {
            // fork仓库作为中间操作仓库
            JSONObject forkedObj = forkStore(giteeOwner, name, forkConfig.getAccessToken(), name, name);
            try {
                //线程休眠 fork仓库有延迟
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contents = giteeService.getContents(forkConfig.getOwner(), name, "/", forkConfig.getAccessToken(), branch);
            if (CollectionUtils.isEmpty(forkedObj)) {
                log.info("RPM仓库fork失败 projrct：{}", name);
                return;
            }

            if (CollectionUtils.isEmpty(contents)) {
                log.info("RPM仓库内容获取失败失败 projrct：{},branch：{}", name, branch);
                return;
            }

        }
        contents = contents.stream().filter(a -> {
            return (name + ".spec").equals(a.getString("name"));
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(contents)) {
            log.info("仓库中没有 spec文件");
            return;
        }
        JSONObject specFileInfo = contents.get(0);
        String path = specFileInfo.getString("path");
        RepoCommitsBody repoCommitsBody = handleRpmCommitsBody(name, path, branch, community_latest_version, upObj_latest_version);
        if (repoCommitsBody != null) {
            RepoCommitWithFiles repoCommitWithFiles = giteeService.postReposOwnerRepoCommits(forkConfig.getAccessToken(), forkConfig.getOwner(), name, repoCommitsBody);
            log.info("文件commit update成功：" + repoCommitWithFiles.getCommentsUrl());
            // 创建issue
            Issue issue = createIssue(forkConfig.getAccessToken(), giteeOwner, prTitle, name, pulllRequestConfig.getIssueNum());
            //提交pr并和issue关联
            PullRequest pullRequest = giteeService.postReposOwnerRepoPulls(forkConfig.getAccessToken(), giteeOwner, name, createRepoPullsBody(issue, forkConfig.getOwner() + ":" + handleBranch, branch, null));
            log.info("pr 已提交：" + pullRequest);
        }

    }


    private RepoCommitsBody handleRpmCommitsBody(String name, String path, String branch, String oldestVersion, String latestVersion) {
        RepoCommitsBody repoCommitsBody = null;
        File file = giteeService.getReposOwnerRepoRawPath(forkConfig.getAccessToken(), forkConfig.getOwner(), name, path, branch);
        if (file != null) {
            repoCommitsBody = getTreeRepoCommitsBody(String.format(CommitInfoEnum.RPM.getMessage(), name, latestVersion), branch);
            List<GitAction> gitActionList = repoCommitsBody.getActions();
            GitAction gitAction = new GitAction();
            gitActionList.add(gitAction);
            gitAction.setAction(GitAction.ActionEnum.UPDATE);
            gitAction.setEncoding(GitAction.EncodingEnum.TEXT);
            gitAction.setPath(path);
            try {
                String fileContent = rpmSpecFileContentHandel(file.getPath(), name, oldestVersion, latestVersion);
                gitAction.setContent(fileContent);
                //删除下载的文件
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return repoCommitsBody;

    }


    private String rpmSpecFileContentHandel(String filePath, String name, String oldestVersion, String latestVersion) {
        final String CHARSET_NAME = "UTF-8";
        List<String> content = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), CHARSET_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder contentBuilder = new StringBuilder();
        LocalDate now = LocalDate.now();
        String week = String.valueOf(now.getDayOfWeek()).toLowerCase(Locale.ROOT).substring(0, 3);
        week = week.substring(0, 1).toUpperCase() + week.substring(1);
        String month = String.valueOf(now.getMonth()).toLowerCase(Locale.ROOT).substring(0, 3);
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        String day = String.valueOf(now.getDayOfMonth() < 10 ? "0" + now.getDayOfMonth() : now.getDayOfMonth()).toLowerCase(Locale.ROOT);
        String year = String.valueOf(now.getYear()).toLowerCase(Locale.ROOT);

        String updateTime = new StringBuilder().append(week).append(" ").append(month).append(" ").append(day).append(" ").append(year).toString();
        for (int i = 0; i < content.size(); i++) {
            String s = content.get(i);
            if (s.contains("Version") && s.contains(oldestVersion)) {
                s = s.replace(oldestVersion, latestVersion);
            }

            if (s.startsWith("Release")) {
                String[] split = s.split("Release:");
                String releaseValue = split[1].trim();
                s = s.replace(releaseValue, "1");
            }
            contentBuilder.append(s);
            if (i < content.size() - 1) {
                contentBuilder.append("\n");
            }
            // todo changgelog 写死
            if (s.contains("%changelog")) {
                contentBuilder.append(pulllRequestConfig.getChangelog().replace("TIME", updateTime).replace("VERSION", latestVersion)).append("\n");
                contentBuilder.append("- Type:requirement").append("\n");
                contentBuilder.append("- CVE:NA").append("\n");
                contentBuilder.append("- SUG:NA").append("\n");
                String format = String.format("- DESC:update %s to %s \n", name, latestVersion);
                contentBuilder.append(format).append("\n");
            }
        }
        return contentBuilder.toString();
    }
}
