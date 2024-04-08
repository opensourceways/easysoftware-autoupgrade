package com.softwaremarket.collect.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.softwaremarket.collect.config.CollectConfig;
import com.softwaremarket.collect.enums.CollectEnum;
import com.softwaremarket.collect.handler.SoftVersionInfoHandler;
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
public class UpstreamVersionTask {
    private final CollectConfig collectConfig;
    private final SoftVersionInfoHandler softVersionInfoHandler;

    @Scheduled(cron = "${softwareconfig.schedule}")
    public void getUpstreamVersionInfo() {
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

        String apppkgInfoUrl = collectConfig.getApppkgInfoUrl();
        HashSet<String> appNameSet = new HashSet<>();
        int currentPage = 1;
        JSONArray list = new JSONArray();
        do {
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("timeOrder", "asc");
            paramMap.put("name", "apppkg");
            paramMap.put("pageSize", "50");
            paramMap.put("pageNum", currentPage);
            String result = HttpRequestUtil.sendGet(apppkgInfoUrl, paramMap);
            if (result != null) {
                JSONObject resultObj = JacksonUtils.toObject(JSONObject.class, result);
                System.out.println(resultObj);
                JSONObject data = resultObj.getJSONObject("data");
                list = data.getJSONArray("list");
                if (!CollectionUtils.isEmpty(list)) {
                    list.stream().forEach(a -> {
                        JSONObject app = new JSONObject((Map) a);
                        JSONArray children = app.getJSONArray("children");
                        if (!CollectionUtils.isEmpty(children)) {
                            children.stream().forEach(c -> {
                                JSONObject child = new JSONObject((Map) c);
                                if("memcached".equals(String.valueOf(child.get("name"))))
                                appNameSet.add(String.valueOf(child.get("name")));
                            });
                        }
                    });
                    if (list.size() == 50)
                        currentPage++;
                }
            }
        } while (list != null && list.size() == 50);
        System.out.println(appNameSet);

        for (String appName : appNameSet) {
            String projectsInfoUrl = collectConfig.getProjectsInfoUrl();
            String result = HttpRequestUtil.sendGet(projectsInfoUrl + appName, new HashMap<>());
            if (result != null) {
                JSONObject resultObj = JacksonUtils.toObject(JSONObject.class, result);
                if (CollectionUtils.isEmpty(resultObj))
                    continue;

                JSONArray items = resultObj.getJSONArray("items");
                if (CollectionUtils.isEmpty(items))
                    continue;

                JSONObject upObj = new JSONObject();
                JSONObject openeulerObj = new JSONObject();

                for (Object item : items) {
                    JSONObject o = new JSONObject((Map) item);
                    if ("app_up".equals(o.getString("tag"))) {
                        upObj.put("latest_version", o.getString("version"));
                    }
                    if ("app_openeuler".equals(o.getString("tag"))) {
                        String version = o.getString("version");
                        openeulerObj.put("latest_version", version);
                        openeulerObj.put("name", appName);
                        JSONArray rawVersions = o.getJSONArray("raw_versions");
                        List collect = (List) rawVersions.stream().filter(v -> String.valueOf(v).contains(version)).collect(Collectors.toList());
                        if (!CollectionUtils.isEmpty(collect))
                            openeulerObj.put("os_version", String.valueOf(collect.get(0)).split(version + "-")[1]);
                    }
                }
                if (upObj.size() > 0 && openeulerObj.size() > 0)
                    softVersionInfoHandler.handlePremiumApp(upObj, openeulerObj);

            }


        }

    }
}
