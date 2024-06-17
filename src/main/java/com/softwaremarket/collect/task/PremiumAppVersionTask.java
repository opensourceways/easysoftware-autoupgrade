package com.softwaremarket.collect.task;

import com.softwaremarket.collect.dto.PremiumAppUpdateInfoDto;
import com.softwaremarket.collect.handler.SoftVersionInfoHandler;
import com.softwaremarket.collect.helper.EasysoftwareVersionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class PremiumAppVersionTask {

    private final SoftVersionInfoHandler softVersionInfoHandler;

    private final EasysoftwareVersionHelper easysoftwareVersionHelper;

    // @Scheduled(cron = "${softwareconfig.appkgschedule}")
    public void premiumAppAutocommit(Set<String> appNameSet) {
        log.info("开始自动更新数据");
        // 从软件市场获取全量精品应用
        if (appNameSet == null)
            appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        System.out.println(appNameSet);

        for (String appName : appNameSet) {
            try {
                PremiumAppUpdateInfoDto premiumAppUpdateInfoDto = new PremiumAppUpdateInfoDto();
                //从软件市场获取精品应用上下游版本
                easysoftwareVersionHelper.initUpdateInfo(appName, premiumAppUpdateInfoDto);
                if (premiumAppUpdateInfoDto.checkInfoIsComplete() && !premiumAppUpdateInfoDto.getOeAppLatestVersion().equals(premiumAppUpdateInfoDto.getUpAppLatestVersion())) {
                    log.info("精品应用{}当前欧拉版本：{},镜像版本：{},上游最新版本：{},触发自动更新！", appName, premiumAppUpdateInfoDto.getCommunityCurrentOsVersion(), premiumAppUpdateInfoDto.getOeAppLatestVersion(), premiumAppUpdateInfoDto.getUpAppLatestVersion());
                    softVersionInfoHandler.handlePremiumApp(premiumAppUpdateInfoDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // @Scheduled(cron = "${softwareconfig.appkgschedule}")
    public void premiumAppAutocommitLatestOsVersion(Set<String> appNameSet) {
        log.info("开始自动更新数据精品应用openeuler最新系统数据");
        // 从软件市场获取全量精品应用
        if (appNameSet == null)
            appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        String openeulerLatestOsVersion = easysoftwareVersionHelper.getOpeneulerLatestOsVersion().split("openEuler-")[1].toLowerCase(Locale.ROOT);
        for (String appName : appNameSet) {
            try {
                PremiumAppUpdateInfoDto premiumAppUpdateInfoDto = new PremiumAppUpdateInfoDto();
                easysoftwareVersionHelper.initUpdateInfo(appName, premiumAppUpdateInfoDto);
                if (!premiumAppUpdateInfoDto.checkInfoIsComplete() || currentOsVersionLatest(openeulerLatestOsVersion, premiumAppUpdateInfoDto.getCommunityCurrentOsVersion()))
                    continue;
                premiumAppUpdateInfoDto.setCommunityOtherOsVersion(openeulerLatestOsVersion);
                softVersionInfoHandler.handlePremiumApp(premiumAppUpdateInfoDto);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //  @Scheduled(cron = "${softwareconfig.appkgschedule}")
    public void premiumAppAllOsVersionUpdate(Set<String> appNameSet) {
        List<String> dockerHubOpeneulerOsVersion =new ArrayList<>(); //easysoftwareVersionHelper.getDockerHubOpeneulerOsVersion();
        dockerHubOpeneulerOsVersion.add("24.03-lts");
        log.info("开始自动更新数据精品应用openeuler所有系统数据");
        if (appNameSet == null)
            appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        for (String appName : appNameSet) {
            try {
                PremiumAppUpdateInfoDto premiumAppUpdateInfoDto = new PremiumAppUpdateInfoDto();
                //从软件市场获取精品应用上下游版本
                easysoftwareVersionHelper.initUpdateInfo(appName, premiumAppUpdateInfoDto);
                premiumAppUpdateInfoDto.setOeAppLatestVersion("2.11.1");
                if (premiumAppUpdateInfoDto.checkInfoIsComplete()) {
                    log.info("精品应用{}当前欧拉版本：{},镜像版本：{},上游最新版本：{},触发自动更新！", appName, premiumAppUpdateInfoDto.getCommunityCurrentOsVersion(), premiumAppUpdateInfoDto.getOeAppLatestVersion(), premiumAppUpdateInfoDto.getUpAppLatestVersion());
                    softVersionInfoHandler.batchUpdatePremiumApp(dockerHubOpeneulerOsVersion, premiumAppUpdateInfoDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
