package com.softwaremarket.collect.task;

import com.alibaba.fastjson.JSONObject;
import com.softwaremarket.collect.config.ForkConfig;
import com.softwaremarket.collect.config.RpmConfig;
import com.softwaremarket.collect.handler.SoftVersionInfoHandler;
import com.softwaremarket.collect.helper.EasysoftwareVersionHelper;
import com.softwaremarket.collect.service.IGiteeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class RpmVersionTask {

    private final EasysoftwareVersionHelper easysoftwareVersionHelper;
    private final SoftVersionInfoHandler softVersionInfoHandler;

    //@Scheduled(cron = "${softwareconfig.rpmSchedule}")
    public void rpmAutocommit() {
        //  Set<String> rpmNameSet = giteeService.getReposProjects(rpmConfig.getRepo(), forkConfig.getAccessToken());
        Set<String> rpmNameSet = new HashSet<>();
        rpmNameSet.add("curl");
        System.out.println(rpmNameSet);
        for (String appName : rpmNameSet) {
            try {
                com.alibaba.fastjson.JSONObject upObj = new com.alibaba.fastjson.JSONObject();
                com.alibaba.fastjson.JSONObject openeulerObj = new JSONObject();
              //  easysoftwareVersionHelper.getEasysoftVersion(appName, upObj, openeulerObj);
                if (upObj.size() > 0 && openeulerObj.size() > 0)
                    softVersionInfoHandler.handleRpm(upObj, openeulerObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
