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
package com.softwaremarket.autoupgrade.task;

import com.alibaba.fastjson.JSONObject;
import com.softwaremarket.autoupgrade.config.RepoConfig;
import com.softwaremarket.autoupgrade.config.RpmConfig;
import com.softwaremarket.autoupgrade.dto.RepoInfoDto;
import com.softwaremarket.autoupgrade.dto.UpdateInfoDto;
import com.softwaremarket.autoupgrade.handler.RpmUpdateHandler;
import com.softwaremarket.autoupgrade.helper.EasysoftwareVersionHelper;
import com.softwaremarket.autoupgrade.service.impl.GitService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Service
@EnableAsync
@RequiredArgsConstructor
public class RpmVersionTask {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RpmVersionTask.class);

    /**
     * easysoftwareVersionHelper.
     */
    private final EasysoftwareVersionHelper easysoftwareVersionHelper;

    /**
     * rpmUpdateHandler.
     */
    private final RpmUpdateHandler rpmUpdateHandler;

    /**
     * git service.
     */
    @Autowired
    GitService gitSvc;

    /**
     * config of repo.
     */
    @Autowired
    RepoConfig repoConfig;
    @Autowired
    private RpmConfig rpmConfig;

    /**
     * auto upgrade rpm version.
     */
    //@Scheduled(cron = "${softwareconfig.rpmSchedule}")
    public void rpmAutocommit() {
        //  refreshUpstreamRepo();
        //  Set<String> rpmNameSet = giteeService.getReposProjects(rpmConfig.getRepo(), forkConfig.getAccessToken());
        RepoInfoDto repoInfo = rpmConfig.getRepoInfo();
        easysoftwareVersionHelper.getToken(rpmConfig.getForkInfo());
        Set<String> rpmNameSet = new HashSet<>(Arrays.asList(repoInfo.getRepo().split(",")));
        for (String appName : rpmNameSet) {
            try {
                UpdateInfoDto updateInfoDto = new UpdateInfoDto();
                updateInfoDto.setAppName(appName);
                //从软件市场获取精品应用上下游版本
                easysoftwareVersionHelper.initUpdateInfo(updateInfoDto);
                updateInfoDto.setCommunityCurrentOsVersion(null);

                //手动更改名称和版本
               /* if (appName.equals("redis6")) {
                    updateInfoDto.setUpAppLatestVersion("6.2.14");
                }*/
                rpmUpdateHandler.handleRpm(updateInfoDto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * refresh remote upstream repo by using git clone or git pull.
     */
    public void refreshUpstreamRepo() {

        List<String> remotePathList = repoConfig.getRepolist();

        for (String remotePath : remotePathList) {
            gitSvc.cloneOrPull(remotePath);
        }

        LOGGER.info("refresh upstream end");
    }

}
