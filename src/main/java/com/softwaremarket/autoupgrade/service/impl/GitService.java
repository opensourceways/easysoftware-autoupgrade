/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/
package com.softwaremarket.autoupgrade.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.eclipse.jgit.lib.Repository;
import com.softwaremarket.autoupgrade.util.FileUtil;
import com.softwaremarket.autoupgrade.config.GitConfig;


@Component
public class GitService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);

    /**
     * config.
     */
    @Autowired
    private GitConfig config;

    /**
     * clone or pull the repo.
     */
    public void cloneOrPull(String remotePath) {
        // repo仓库存放绝对路径地址 
        String localPath = generateLocalGitPath(remotePath);
        // repo名称
        String repoName = localPath.replace(config.getStorePath(), "");
        // folder地址
        String folderPath = config.getStorePath();

        File folder = new File(folderPath);
        // 遍历目录下是否存在repo仓库
        File[] repoFiles = folder.listFiles((dir, name) -> repoName.equals(name));

        // 如果不存在 则创建localPath 并clone仓库 否则刷新
        if (repoFiles == null || repoFiles.length == 0) {
            File repo = new File(localPath);
            FileUtil.mkdirIfUnexist(repo);
            cloneRepo(remotePath);
        } else {
            pullRepo(localPath);
        }
    }

    /**
     * get provider.
     *
     * @return provider.
     */
    public UsernamePasswordCredentialsProvider getProvider() {
        return new UsernamePasswordCredentialsProvider(config.getUserName(), config.getPassword());
    }

    /**
     * git pull the repo.
     */
    public void pullRepo(String localPath) {
        UsernamePasswordCredentialsProvider provider = getProvider();
        try {
            Git git = Git.open(new File(localPath));
            git.pull()
                    .setCredentialsProvider(provider)
                    .call();
        } catch (Exception e) {
            LOGGER.error("fail to git pull repo: {}, err: {}", localPath, e.getMessage());
        }
    }

    /**
     * git pull the repo.
     */
    public List<String> fetchCommitIdsInRange(String pkgName, String startId, String endId) {
        String localPath = config.getStorePath() + pkgName;
        if (startId == null || endId == null) {
            LOGGER.error("One or both commit IDs/tags not found: " + startId + ", " + endId);
            return Collections.emptyList();
        }

        List<String> res = new ArrayList<>();

        File repoDir = new File(localPath); // 仓库路径，包含.git目录

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(new File(repoDir, ".git"))
                .readEnvironment()
                .findGitDir()
                .build()) {
            try (Git git = new Git(repository)) {
                // 解析起始和结束提交ID（或标签）  
                ObjectId startObjectId = repository.resolve(startId + "^{commit}");
                ObjectId endObjectId = repository.resolve(endId + "^{commit}");

                Iterable<RevCommit> commits = git.log()
                        .addRange(startObjectId, endObjectId)
                        .call();  // 注意：这里的范围是[start, end)，不包括end  

                for (RevCommit commit : commits) {
                    String[] strCommitId = commit.getId().toString().split(" ");
                    String strId = "";
                    if (strCommitId.length >= 1) {
                        strId = strCommitId[1];
                    } else {
                        strId = commit.getId().toString();
                    }
                    res.add(strId);
                }
            }
        } catch (GitAPIException e) {
            LOGGER.error("fail to clone repo: {}", localPath);
        } catch (IOException e) {
            LOGGER.error("fail to clone repo: {}", localPath);
        } catch (NullPointerException e) {
            LOGGER.error("chekc the input tag start:{} end:{}, tags not found in upstream", startId, endId);
        }

        return res;
    }

    /**
     * clone the repo.
     */
    public void cloneRepo(String remotePath) {
        if (remotePath == null || remotePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Remote path cannot be null or empty");
        }

        UsernamePasswordCredentialsProvider provider = getProvider();

        String localPath = generateLocalGitPath(remotePath);

        try (Git git = Git.cloneRepository()
                .setURI(remotePath)
                .setDirectory(new File(localPath))
                .setCredentialsProvider(provider)
                .setCloneSubmodules(true)
                .call()) {
            git.getRepository().close();
        } catch (GitAPIException e) {
            LOGGER.error("fail to clone repo: {}", localPath);
        }
    }

    private String generateLocalGitPath(String remoteGitPath) {

        String[] suffix = remoteGitPath.split("/");

        String localPath = remoteGitPath;
        if (suffix.length > 1) {
            localPath = config.getStorePath() + suffix[suffix.length - 1];
        } else {
            throw new IllegalArgumentException("invalid remote github url");
        }

        return localPath;
    }

}
