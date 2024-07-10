package com.softwaremarket.autoupgrade.task;

import com.alibaba.fastjson.JSONObject;
import com.softwaremarket.autoupgrade.handler.RpmUpdateHandler;
import com.softwaremarket.autoupgrade.helper.EasysoftwareVersionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class RpmVersionTask {

    private final EasysoftwareVersionHelper easysoftwareVersionHelper;
    private final RpmUpdateHandler rpmUpdateHandler;

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
                    rpmUpdateHandler.handleRpm(upObj, openeulerObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
