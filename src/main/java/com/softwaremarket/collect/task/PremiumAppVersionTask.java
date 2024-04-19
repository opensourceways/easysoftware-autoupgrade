package com.softwaremarket.collect.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.softwaremarket.collect.config.CollectConfig;
import com.softwaremarket.collect.enums.CollectEnum;
import com.softwaremarket.collect.handler.SoftVersionInfoHandler;
import com.softwaremarket.collect.helper.EasysoftwareVersionHelper;
import com.softwaremarket.collect.util.HttpRequestUtil;
import com.softwaremarket.collect.util.HttpResponceUtil;
import com.softwaremarket.collect.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class PremiumAppVersionTask {

    private final SoftVersionInfoHandler softVersionInfoHandler;

    private final EasysoftwareVersionHelper easysoftwareVersionHelper;

    @Scheduled(cron = "${softwareconfig.appkgschedule}")
    public void premiumAppAutocommit() {
        log.info("开始自动更新数据");
        /*String dockerUpurl = String.format(CollectEnum.DOCKER_UP_STREAM.getUrl(), collectConfig.getSotfwareInfoUrl());
        String dockerUpResult = HttpRequestUtil.sendGet(dockerUpurl, new HashMap<>());
        JSONObject dockerUpObj = JacksonUtils.toObject(JSONObject.class, dockerUpResult);

        String dockerOpeneulerurl = String.format(CollectEnum.DOCKER_OPENEULER.getUrl(), collectConfig.getSotfwareInfoUrl());
        String dockerOpeneulerResult = HttpRequestUtil.sendGet(dockerOpeneulerurl, new HashMap<>());
        JSONObject dockerOpeneulerObj = JacksonUtils.toObject(JSONObject.class, dockerOpeneulerResult);
        if(!HttpResponceUtil.requestSoftIsSuccess(dockerUpObj) &&  !HttpResponceUtil.requestSoftIsSuccess(dockerOpeneulerObj)){
            log.info("dockerUpurl: {},result:{}",dockerUpurl,dockerUpResult);
            log.info("dockerOpeneulerurl: {},result:{}",dockerOpeneulerurl,dockerOpeneulerResult);
            return;
        }*/

        Set<String> appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        System.out.println(appNameSet);

        for (String appName : appNameSet) {
            try {
                JSONObject upObj = new JSONObject();
                JSONObject openeulerObj = new JSONObject();
                easysoftwareVersionHelper.getEasysoftVersion(appName, upObj, openeulerObj);
                if (upObj.size() > 0 && openeulerObj.size() > 0)
                    softVersionInfoHandler.handlePremiumApp(upObj, openeulerObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Scheduled(cron = "${softwareconfig.appkgOsSchedule}")
    public void premiumAppAutocommitLatestOsVersion() {
        log.info("开始自动更新数据精品应用openeuler最新系统数据");
        Set<String> appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        appNameSet.add("loki");
        String openeulerLatestOsVersion = easysoftwareVersionHelper.getOpeneulerLatestOsVersion().split("openEuler-")[1].toLowerCase(Locale.ROOT);
        for (String appName : appNameSet) {
            try {
                JSONObject upObj = new JSONObject();
                JSONObject openeulerObj = new JSONObject();
                easysoftwareVersionHelper.getEasysoftVersion(appName, upObj, openeulerObj);
                String openeulerCurrentOsVersion = openeulerObj.getString("os_version");
                if (openeulerCurrentOsVersion == null)
                    continue;

                if (currentOsVersionLatest(openeulerLatestOsVersion, openeulerCurrentOsVersion))
                    continue;


                if (upObj.size() > 0 && openeulerObj.size() > 0) {
                    openeulerObj.put("latestOsVersion", openeulerLatestOsVersion);
                    softVersionInfoHandler.handlePremiumApp(upObj, openeulerObj);
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
