package com.softwaremarket.autoupgrade.task;

import com.softwaremarket.autoupgrade.config.ApplicationConfig;
import com.softwaremarket.autoupgrade.dto.UpdateInfoDto;
import com.softwaremarket.autoupgrade.handler.ApplicationUpdateHandler;

import com.softwaremarket.autoupgrade.helper.EasysoftwareVersionHelper;
import com.softwaremarket.autoupgrade.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class ApplicationVersionTask {

    private final ApplicationUpdateHandler applicationUpdateHandler;

    private final EasysoftwareVersionHelper easysoftwareVersionHelper;

    private final ApplicationConfig applicationConfig;


    // 将当前正在使用的欧拉使用的app镜像升级至最高级别
    // @Scheduled(cron = "${softwareconfig.appkgschedule}")
    public void premiumAppAutocommit() {
        log.info("开始自动更新数据");
        easysoftwareVersionHelper.getToken(applicationConfig.getForkInfo());
        // 从软件市场获取全量精品应用
        Set<String> appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        for (String appName : appNameSet) {
            try {
                UpdateInfoDto premiumAppUpdateInfoDto = new UpdateInfoDto();
                premiumAppUpdateInfoDto.setAppName(appName);
                //从软件市场获取精品应用上下游版本
                easysoftwareVersionHelper.initUpdateInfo(premiumAppUpdateInfoDto);
                if (premiumAppUpdateInfoDto.checkInfoIsComplete() && !premiumAppUpdateInfoDto.getOeAppLatestVersion().equals(premiumAppUpdateInfoDto.getUpAppLatestVersion())) {
                    log.info("精品应用{}当前欧拉版本：{},镜像版本：{},上游最新版本：{},触发自动更新！", appName, premiumAppUpdateInfoDto.getCommunityCurrentOsVersion(), premiumAppUpdateInfoDto.getOeAppLatestVersion(), premiumAppUpdateInfoDto.getUpAppLatestVersion());
                    applicationUpdateHandler.handlePremiumApp(premiumAppUpdateInfoDto, Boolean.TRUE);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        EmailSenderUtil.sendApplicationEmail(applicationConfig.getMailInfo());

    }

    // 补全所有欧拉版本的最新app镜像
    //@Scheduled(cron = "${softwareconfig.appkgschedule}")
    public void premiumAppAllOsVersionUpdate() {
        easysoftwareVersionHelper.getToken(applicationConfig.getForkInfo());
        List<String> dockerHubOpeneulerOsVersion = applicationConfig.getEulerversion(); //easysoftwareVersionHelper.getDockerHubOpeneulerOsVersion();
        log.info("！开始自动更新数据精品应用openeuler所有系统数据");
        Set<String> appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        for (String appName : appNameSet) {
            try {
                UpdateInfoDto premiumAppUpdateInfoDto = new UpdateInfoDto();
                premiumAppUpdateInfoDto.setAppName(appName);
                //从软件市场获取精品应用上下游版本
                easysoftwareVersionHelper.initUpdateInfo(premiumAppUpdateInfoDto);
                if (premiumAppUpdateInfoDto.checkInfoIsComplete()) {
                    log.info("批量精品应用{}当前欧拉版本：{},镜像版本：{},上游最新版本：{},触发自动更新！", appName, premiumAppUpdateInfoDto.getCommunityCurrentOsVersion(), premiumAppUpdateInfoDto.getOeAppLatestVersion(), premiumAppUpdateInfoDto.getUpAppLatestVersion());
                    applicationUpdateHandler.batchUpdatePremiumApp(dockerHubOpeneulerOsVersion, premiumAppUpdateInfoDto);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        EmailSenderUtil.sendApplicationEmail(applicationConfig.getMailInfo());

    }


    // 只更新当前版本的最新镜像,最新的欧拉版来自于https://datastat.openeuler.org/query/versions?community=openeuler
    public void premiumAppAutocommitLatestOsVersion(Set<String> appNameSet) {
        log.info("开始自动更新数据精品应用openeuler最新系统数据");
        // 从软件市场获取全量精品应用
        if (appNameSet == null)
            appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        String openeulerLatestOsVersion = easysoftwareVersionHelper.getOpeneulerLatestOsVersion().split("openEuler-")[1].toLowerCase(Locale.ROOT);
        for (String appName : appNameSet) {
            try {
                UpdateInfoDto premiumAppUpdateInfoDto = new UpdateInfoDto();
                premiumAppUpdateInfoDto.setAppName(appName);
                easysoftwareVersionHelper.initUpdateInfo(premiumAppUpdateInfoDto);
                if (!premiumAppUpdateInfoDto.checkInfoIsComplete() || currentOsVersionLatest(openeulerLatestOsVersion, premiumAppUpdateInfoDto.getCommunityCurrentOsVersion()))
                    continue;
                premiumAppUpdateInfoDto.setCommunityOtherOsVersion(openeulerLatestOsVersion);
                applicationUpdateHandler.handlePremiumApp(premiumAppUpdateInfoDto, Boolean.TRUE);

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

    }

    private Boolean currentOsVersionLatest(String openeulerLatestOsVersion, String openeulerCurrentOsVersion) {
        if (openeulerLatestOsVersion.contains("lts"))
            openeulerLatestOsVersion = openeulerLatestOsVersion.replace("lts", "");

        openeulerLatestOsVersion = openeulerLatestOsVersion.replace(".", "").replace("-", "");

        int latestOsversionNum = 0;
        int splatestVersionNum = 0;
        int currentOsversionNum = 0;
        int spCurrentVersionNum = 0;
        if (openeulerLatestOsVersion.contains("sp")) {
            String[] sps = openeulerLatestOsVersion.split("sp");
            latestOsversionNum = Integer.parseInt(sps[0]);
            splatestVersionNum = Integer.parseInt(sps[1]);
        } else {
            latestOsversionNum = Integer.parseInt(openeulerLatestOsVersion);
        }


        if (openeulerCurrentOsVersion.contains("oe"))
            openeulerCurrentOsVersion = openeulerCurrentOsVersion.split("oe")[1];

        if (openeulerCurrentOsVersion.contains("sp")) {
            String[] sps = openeulerCurrentOsVersion.split("sp");
            currentOsversionNum = Integer.parseInt(sps[0]);
            spCurrentVersionNum = Integer.parseInt(sps[1]);
        } else {
            spCurrentVersionNum = Integer.parseInt(openeulerCurrentOsVersion);
        }

        if (latestOsversionNum > currentOsversionNum || (latestOsversionNum == currentOsversionNum && splatestVersionNum > spCurrentVersionNum))
            return false;

        return true;
    }
}
