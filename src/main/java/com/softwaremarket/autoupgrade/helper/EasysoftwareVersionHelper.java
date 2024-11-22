package com.softwaremarket.autoupgrade.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.softwaremarket.autoupgrade.config.CollectConfig;
import com.softwaremarket.autoupgrade.dto.UpdateInfoDto;
import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.service.IGiteeService;
import com.softwaremarket.autoupgrade.util.EmailSenderUtil;
import com.softwaremarket.autoupgrade.util.HttpRequestUtil;
import com.softwaremarket.autoupgrade.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EasysoftwareVersionHelper {
    private final CollectConfig collectConfig;
    private final IGiteeService giteeService;

    public void initUpdateInfo(UpdateInfoDto premiumAppUpdateInfoDto) {
        String projectsInfoUrl = collectConfig.getProjectsInfoUrl();
        String url = projectsInfoUrl + premiumAppUpdateInfoDto.getAppName();
        String result = HttpRequestUtil.sendGet(url, new HashMap<>());
        if (result != null) {
            JSONObject resultObj = JacksonUtils.toObject(JSONObject.class, result);
            if (CollectionUtils.isEmpty(resultObj))
                return;

            JSONArray items = resultObj.getJSONArray("items");
            if (CollectionUtils.isEmpty(items))
                return;

            for (Object item : items) {
                JSONObject o = new JSONObject((Map) item);
                //"GitHub".equals(o.getString("backend"))
                if ("app_up".equals(o.getString("tag"))||"GitHub".equals(o.getString("backend"))) {
                    premiumAppUpdateInfoDto.setUpAppLatestVersion(o.getString("version").replace("_", "."));
                }
                if ("app_openeuler".equals(o.getString("tag"))) {
                    String version = o.getString("version");
                    premiumAppUpdateInfoDto.setOeAppLatestVersion(version);
                    JSONArray rawVersions = o.getJSONArray("raw_versions");
                    List collect = (List) rawVersions.stream().filter(v -> String.valueOf(v).contains(version)).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(collect))
                        premiumAppUpdateInfoDto.setCommunityCurrentOsVersion(String.valueOf(collect.get(0)).split(version + "-")[1]);
                }
            }
        }
    }


    public Set<String> getEasysoftApppkgSet() {
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
                JSONObject data = resultObj.getJSONObject("data");
                list = data.getJSONArray("list");
                if (!CollectionUtils.isEmpty(list)) {
                    list.stream().forEach(a -> {
                        JSONObject app = new JSONObject((Map) a);
                        //grafana  prometheus
                        appNameSet.add(String.valueOf(app.get("name")).toLowerCase(Locale.ROOT));

                        String pkgId = app.getString("pkgId");
                        setAppkgMail(app.getString("name").toLowerCase(Locale.ROOT), pkgId);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage());
                        }
                    });
                    if (list.size() == 50)
                        currentPage++;
                }
            }
        } while (list != null && list.size() == 50);
        return appNameSet;
    }


    public void setAppkgMail(String name, String pkgId) {
        String detailUrl = collectConfig.getApppkgDetailUrl();
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("pkgId", pkgId);
        String result = HttpRequestUtil.sendGet(detailUrl, paramMap);
        if (result != null) {
            JSONObject resultObj = JacksonUtils.toObject(JSONObject.class, result);
            JSONObject data = resultObj.getJSONObject("data");

            JSONArray list = data.getJSONArray("list");
            Map detail = (Map) list.get(0);
            EmailSenderUtil.applicationMailMap.put(name, String.valueOf(detail.get("maintainerEmail")));
        }
    }

    public String getOpeneulerLatestOsVersion() {
        String openEulerOsVersionInfoUrl = collectConfig.getOpenEulerOsVersionInfoUrl();
        String result = HttpRequestUtil.sendGet(openEulerOsVersionInfoUrl, new HashMap<>());
        if (result != null) {
            JSONObject resultObj = JacksonUtils.toObject(JSONObject.class, result);
            JSONArray data = resultObj.getJSONArray("data");
            return data.get(0) + "";
        }
        return null;
    }

    // 获取dockerhub上欧拉所有版本
    public List<String> getDockerHubOpeneulerOsVersion() {
        String openEulerOsVersionInfoUrl = collectConfig.getDockerHubOpeneulerOsVersionInfoUrl();
        JSONObject paramMap = new JSONObject();
        paramMap.put("dry_run", "true");
        paramMap.put("version_url", "openeuler/openeuler");
        paramMap.put("version_scheme", "ModifiedSemantic");
        paramMap.put("homepage", collectConfig.getDockerHubOpeneulerHomepage());

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", collectConfig.getDockerHubOpeneulerAuthorization());
        headerMap.put("Content-Type", "application/json");
        String result = HttpRequestUtil.sendPost(openEulerOsVersionInfoUrl, headerMap, paramMap);
        if (result != null) {
            JSONObject resultObj = JacksonUtils.toObject(JSONObject.class, result);
            JSONArray rawVersions = resultObj.getJSONArray("raw_versions");
            return JSON.parseArray(JSONObject.toJSONString(rawVersions), String.class);
        }
        return null;
    }


    public void getToken(ForkInfoDto forkInfo) {
        String tokenByPassword = giteeService.getTokenByPassword(forkInfo);
        forkInfo.setAccessToken(tokenByPassword);
    }
}
