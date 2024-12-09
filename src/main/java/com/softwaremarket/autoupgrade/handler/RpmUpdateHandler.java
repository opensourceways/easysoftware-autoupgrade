package com.softwaremarket.autoupgrade.handler;

import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.autoupgrade.config.GitConfig;
import com.softwaremarket.autoupgrade.config.RpmConfig;
import com.softwaremarket.autoupgrade.dto.PrInfoDto;
import com.softwaremarket.autoupgrade.dto.UpdateInfoDto;
import com.softwaremarket.autoupgrade.enums.CommitInfoEnum;
import com.softwaremarket.autoupgrade.enums.GiteeRepoEnum;
import com.softwaremarket.autoupgrade.service.impl.GitService;
import com.softwaremarket.autoupgrade.util.Base64Util;
import com.softwaremarket.autoupgrade.util.FileUtil;
import com.softwaremarket.autoupgrade.util.PatchRegexPatterns;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component


public class RpmUpdateHandler extends BaseCommonUpdateHandler {

    private PrInfoDto pulllRequestConfig;
    @Autowired
    private GitService gitService;
    @Autowired
    private PatchRegexPatterns patchRegexPatterns;
    @Autowired
    private RpmConfig rpmConfig;

    @Autowired
    private GitConfig config;
    /**
     * git service.
     */
    @Autowired
    GitService gitSvc;

    @PostConstruct
    public void init() {
        super.forkConfig = this.rpmConfig.getForkInfo();
        this.pulllRequestConfig = this.rpmConfig.getPrInfo();
    }

    public void handleRpm(UpdateInfoDto updateInfoDto) {
        String communityLatestVersion = null;
        String sourceUrl;
        String upLatestVersion = updateInfoDto.getUpAppLatestVersion();
        String branch = CommitInfoEnum.PremiumApp.getBranch();

        // 需要更新的软件 name
        String name = updateInfoDto.getAppName();
        //获取原始的仓库信息
        List<JSONObject> contents = giteeService.getContents(GiteeRepoEnum.RPM.getOwner(), name, "/", forkConfig.getAccessToken(), branch);
        List<JSONObject> specContents = contents.stream().filter(a -> {
            return (name + ".spec").equals(a.getString("name"));
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(specContents)) {
            updateInfoBySpec(name, specContents.get(0).getString("path"), branch, updateInfoDto);
            communityLatestVersion = updateInfoDto.getOeAppLatestVersion();
        }
        if (!updateInfoDto.checkRpmInfoIsComplete()) {
            log.info(name + "升级信息不足,取消升级！");
            return;
        }
        sourceUrl = updateInfoDto.getSourceUrl().replace("%{gem_name}", name.contains("rubygem-")? name.replace("rubygem-",""):name).replace("%{version}", upLatestVersion);

        String prTitle = String.format(pulllRequestConfig.getPrTitle(), name /*+ "-" + os_version*/, communityLatestVersion, upLatestVersion);

        String giteeOwner = GiteeRepoEnum.RPM.getOwner();

        // 版本相同或者已经提交过相同pr将不再处理
        if (upLatestVersion.equals(communityLatestVersion) ||
                checkHasCreatePR(prTitle, giteeOwner, name, forkConfig.getAccessToken())) {
            return;
        }


        //获取fork后的仓库信息
        contents = giteeService.getContents(forkConfig.getOwner(), name, "/", forkConfig.getAccessToken(), branch);

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
        specContents = contents.stream().filter(a -> {
            return (name + ".spec").equals(a.getString("name"));
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(specContents)) {
            log.info("仓库中没有 spec文件");
            return;
        }

        List<JSONObject> patchContents = contents.stream().filter(a -> {
            return a.getString("name").endsWith(".patch");
        }).collect(Collectors.toList());

        List<JSONObject> tarContents = contents.stream().filter(a -> {
            return a.getString("name").matches(".*\\.tar\\..*") || a.getString("name").endsWith("gem");
        }).collect(Collectors.toList());

        RepoCommitsBody repoCommitsBody = getTreeRepoCommitsBody(String.format(CommitInfoEnum.RPM.getMessage(), name, upLatestVersion), branch);
        //上传tar包,tar包上传失败取消升级
        if (!hanleTarCommitsBody(tarContents.get(0).getString("name"), sourceUrl, tarContents.get(0).getString("path"), repoCommitsBody)) {
            return;
        }

        //更新sprc文件
        handleSpecCommitsBody(name, specContents.get(0).getString("path"), branch, communityLatestVersion, upLatestVersion, repoCommitsBody);

        //如果数据源来自github,需要比对github的提交信息从而删除patch文件
        if (sourceUrl.contains("github.com")) {
            hanlePatchCommitsBody(sourceUrl.split(name)[0], patchContents, name, branch, repoCommitsBody, communityLatestVersion, upLatestVersion);
        }
        if (repoCommitsBody != null && !CollectionUtils.isEmpty(repoCommitsBody.getActions())) {
            RepoCommitWithFiles repoCommitWithFiles = giteeService.postReposOwnerRepoCommits(forkConfig.getAccessToken(), forkConfig.getOwner(), name, repoCommitsBody);
            log.info("文件commit update成功：" + repoCommitWithFiles.getCommentsUrl());
            // 创建issue
            //  Issue issue = createIssue(forkConfig.getAccessToken(), giteeOwner, prTitle, name, pulllRequestConfig.getIssueNum());
            //提交pr
            PullRequest pullRequest = giteeService.postReposOwnerRepoPulls(forkConfig.getAccessToken(), giteeOwner, name, createRepoPullsBody(null, forkConfig.getOwner() + ":" + handleBranch, branch, null, prTitle));
            log.info("pr 已提交：" + pullRequest);
        }

    }

    //组装tar文件新增body
    private Boolean hanleTarCommitsBody(String fileName, String sourceUrl, String path, RepoCommitsBody repoCommitsBody) {
        String[] split = sourceUrl.split("/");
        String newFileName = split[split.length - 1];
        String tarPath = config.getStorePath() + newFileName;
        boolean downloadFile = FileUtil.downloadFile(sourceUrl, tarPath);
        if (downloadFile) {
            try {
                List<GitAction> actions = repoCommitsBody.getActions();
                // 删除原始包
                GitAction delete = new GitAction();
                actions.add(delete);
                delete.setAction(GitAction.ActionEnum.DELETE);
                delete.setPath(path);

                //上传新包
                GitAction create = new GitAction();
                actions.add(create);
                create.setAction(GitAction.ActionEnum.CREATE);
                create.setEncoding(GitAction.EncodingEnum.BASE64);
                create.setContent(Base64Util.fileToBase64Str(new File(tarPath)));
                create.setPath(path.replace(fileName, newFileName));
                FileUtil.deleteFile(tarPath);
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }
        return downloadFile;
    }

    //组装patch文件删除body
    private void hanlePatchCommitsBody(String remotePath, List<JSONObject> patchContents, String name, String branch, RepoCommitsBody repoCommitsBody, String communityLatestVersion, String upLatestVersion) {
        gitSvc.cloneOrPull(remotePath);
        List<String> fetchCommitIdsInRange = gitService.fetchCommitIdsInRange(name, communityLatestVersion, upLatestVersion);
        System.out.println(fetchCommitIdsInRange);
        for (JSONObject patchContent : patchContents) {
            String path = patchContent.getString("path");
            String fileName = patchContent.getString("name");
            File file = giteeService.getReposOwnerRepoRawPath(forkConfig.getAccessToken(), forkConfig.getOwner(), name, path, branch);
            if (Objects.isNull(file)) {
                continue;
            }
            List<String> commitIdList = patchRegexPatterns.fetchCommitIdFromPatchFile(file.getPath());
            System.out.println("commitIdList:" + commitIdList);
            if (CollectionUtils.isEmpty(commitIdList)) {
                continue;
            }
            if (checkPatchNeedDelete(fetchCommitIdsInRange, commitIdList)) {
                GitAction gitAction = new GitAction();
                repoCommitsBody.getActions().add(gitAction);
                gitAction.setAction(GitAction.ActionEnum.DELETE);
                gitAction.setPath(path);
                continue;
            }

        }
    }


    private Boolean checkPatchNeedDelete(List<String> fetchCommitIdsInRange, List<String> commitIdList) {
        for (String realCommitId : fetchCommitIdsInRange) {
            for (String containsCommitId : commitIdList) {
                if (!containsCommitId.contains(realCommitId)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    // 处理spec文件
    private void handleSpecCommitsBody(String name, String path, String branch, String oldestVersion, String latestVersion, RepoCommitsBody repoCommitsBody) {
        File file = giteeService.getReposOwnerRepoRawPath(forkConfig.getAccessToken(), forkConfig.getOwner(), name, path, branch);
        if (file != null) {
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

    }


    private void updateInfoBySpec(String name, String path, String branch, UpdateInfoDto updateInfoDto) {
        File file = giteeService.getReposOwnerRepoRawPath(forkConfig.getAccessToken(), GiteeRepoEnum.RPM.getOwner(), name, path, branch);
        if (file != null) {
            List<String> content = FileUtil.getFileContetList(file.getPath());
            Boolean hasUpdateVersion = Boolean.FALSE;
            Boolean hasUpdateSource = Boolean.FALSE;
            for (int i = 0; i < content.size(); i++) {
                if (hasUpdateSource && hasUpdateVersion)
                    break;
                String lineContent = content.get(i);
                if (lineContent.contains("Version:")) {
                    String[] split = lineContent.split("Version:");
                    updateInfoDto.setOeAppLatestVersion(split[1].trim());
                    hasUpdateVersion = Boolean.TRUE;
                }
                if (lineContent.contains("Source") && (lineContent.contains(".tar.") || lineContent.endsWith(".gem"))) {
                    String[] split = lineContent.split(":");

                    updateInfoDto.setSourceUrl(split[1].trim()+":"+split[2]);
                    hasUpdateSource = Boolean.TRUE;
                }
            }
            FileUtil.deleteFile(file.getPath());
        }
    }

    private String rpmSpecFileContentHandel(String filePath, String name, String oldestVersion, String latestVersion) {
        List<String> content = FileUtil.getFileContetList(filePath);
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
            if (i == content.size() - 1) {
                contentBuilder.append("\n");
            }
        }
        return contentBuilder.toString();
    }
}
