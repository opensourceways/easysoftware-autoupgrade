package com.softwaremarket.autoupgrade.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.ApiClient;
import com.gitee.sdk.gitee5j.Configuration;
import com.gitee.sdk.gitee5j.api.GitDataApi;
import com.gitee.sdk.gitee5j.api.IssuesApi;
import com.gitee.sdk.gitee5j.api.PullRequestsApi;
import com.gitee.sdk.gitee5j.api.RepositoriesApi;
import com.gitee.sdk.gitee5j.auth.OAuth;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.enums.GiteeUrlEnum;
import com.softwaremarket.autoupgrade.service.IGiteeService;
import com.softwaremarket.autoupgrade.util.HttpRequestUtil;
import com.softwaremarket.autoupgrade.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class GiteeService implements IGiteeService {
    private static volatile OAuth OAuth2 = null;

    static {
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
    }

    @Override
    public JSONObject fork(Map parameter) {
        String forkUrl = String.format(GiteeUrlEnum.PostV5ReposOwnerRepoForksUrl.getUrl(), parameter.get("owner"), parameter.get("repo"));
        parameter.remove("owner");
        parameter.remove("repo");
        String result = HttpRequestUtil.sendPost(forkUrl, parameter);
        if (!StringUtils.isEmpty(result)) {
            return JacksonUtils.toObject(JSONObject.class, result);
        }
        return new JSONObject();
    }

    @Override
    public PullRequest createPullRequest(RepoPullsBody body, String token, String owner, String repo) {

        PullRequest result = null;
        OAuth2.setAccessToken(token);

        PullRequestsApi apiInstance = new PullRequestsApi();
        try {
            result = apiInstance.postReposOwnerRepoPulls(owner, repo, body);
            log.info(result + "");
        } catch (Exception e) {
            log.error("Exception when calling PullRequestsApi#postReposOwnerRepoPulls");
            log.info(e.getMessage());
        }
        return result;
    }

    @Override
    public Issue createIssue(String token, String owner, OwnerIssuesBody body) {
        Issue result = null;
        OAuth2.setAccessToken(token);
        IssuesApi apiInstance = new IssuesApi();
        try {
            result = apiInstance.postReposOwnerIssues(owner, body);
            log.info(result + "");
        } catch (Exception e) {
            log.error("Exception when calling IssuesApi#postReposOwnerIssues");
            log.info(e.getMessage());
        }
        return result;
    }

    @Override
    public List<JSONObject> getV5ReposOwnerRepoPulls(String url) {
        String result = HttpRequestUtil.sendGet(url);
        if (!StringUtils.isEmpty(result)) {
            return JacksonUtils.toObjectList(JSONObject.class, result);
        }
        return null;
    }

    //https://gitee.com/api/v5/repos/{owner}/{repo}/contents(/{path})
    @Override
    public List<JSONObject> getContents(String owner, String repo, String path, String token, String branch) {
        try {
            path = URLEncoder.encode(path, "GBK");
            String url = GiteeUrlEnum.ContentsUrl.getUrl().replace("{owner}", owner).replace("{repo}", repo).replace("{path}", path).replace("{access_token}", token).replace("{ref}", branch);
            String result = HttpRequestUtil.sendGet(url);
            if (!StringUtils.isEmpty(result)) {
                return JacksonUtils.toObjectList(JSONObject.class, result);
            }
        } catch (UnsupportedEncodingException e) {
            log.info(e.getMessage());
        }

        return null;
    }

    @Override
    // String | 仓库所属空间地址(企业、组织或个人的地址path)
    // String | 仓库路径(path)
    // String | 可以是分支名(如master)、Commit或者目录Tree的SHA值
    // Integer | 赋值为1递归获取目录
    public Tree getReposOwnerRepoGitTreesSha(String token, String owner, String repo, String sha, Integer recursive) {
        Tree result = null;
        OAuth2.setAccessToken(token);

        GitDataApi apiInstance = new GitDataApi();

        try {
            result = apiInstance.getReposOwnerRepoGitTreesSha(owner, repo, sha, recursive);
        } catch (Exception e) {
            log.error("Exception when calling GitDataApi#getReposOwnerRepoGitTreesSha");
            log.info(e.getMessage());
        }
        return result;
    }

    @Override
    public File getReposOwnerRepoRawPath(String token, String owner, String repo, String path, String ref) {
        File result = null;
        OAuth2.setAccessToken(token);

        RepositoriesApi apiInstance = new RepositoriesApi();
        try {
            result = apiInstance.getReposOwnerRepoRawPath(owner, repo, path, ref);
            log.info(result + "");
        } catch (Exception e) {
            log.error(owner,repo,path,ref);
            log.error("Exception when calling RepositoriesApi#getReposOwnerRepoRawPath");
            log.info(e.getMessage());
        }
        return result;
    }

    @Override
    public RepoCommitWithFiles postReposOwnerRepoCommits(String token, String owner, String repo, RepoCommitsBody body) {
        RepoCommitWithFiles result = null;
        OAuth2.setAccessToken(token);

        RepositoriesApi apiInstance = new RepositoriesApi();
        // String | 仓库所属空间地址(企业、组织或个人的地址path)
        // String | 仓库路径(path)
        // RepoCommitsBody |
        try {
            result = apiInstance.postReposOwnerRepoCommits(owner, repo, body);
            log.info(result + "");
        } catch (Exception e) {
            log.error("Exception when calling RepositoriesApi#postReposOwnerRepoCommits");
            log.info(e.getMessage());
        }
        return result;
    }

    @Override
    public PullRequest postReposOwnerRepoPulls(String token, String owner, String repo, RepoPullsBody body) {


        OAuth2.setAccessToken(token);

        PullRequestsApi apiInstance = new PullRequestsApi();
        try {
            PullRequest result = apiInstance.postReposOwnerRepoPulls(owner, repo, body);
            log.info(result + "");
        } catch (Exception e) {
            log.error("Exception when calling PullRequestsApi#postReposOwnerRepoPulls");
            log.info(e.getMessage());
        }
        return null;
    }

    @Override
    public CompleteBranch postReposOwnerRepoBranches(String token, String owner, String repo, RepoBranchesBody body) {
        CompleteBranch result = null;
        OAuth2.setAccessToken(token);

        RepositoriesApi apiInstance = new RepositoriesApi();
        try {
            result = apiInstance.postReposOwnerRepoBranches(owner, repo, body);
            log.info(result + "");
        } catch (Exception e) {
            log.error("Exception when calling RepositoriesApi#postReposOwnerRepoBranches");
            log.info(e.getMessage());
        }
        return result;
    }

    @Override
    public HashSet<String> getReposProjects(String repo, String token) {
        HashSet<String> projectSet = new HashSet<>();
        JSONArray resultArray = new JSONArray();
        Integer page = 0;
        String orgsUrl = String.valueOf(GiteeUrlEnum.ReposInfoUrl.getUrl()).replace("{org}", repo).replace("{token}", token);
        do {
            page++;
            StringBuilder urlBuilder = new StringBuilder();
            try {
                urlBuilder = new StringBuilder(orgsUrl).append(URLEncoder.encode(String.valueOf(page), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String httpResponse = HttpRequestUtil.sendGet(urlBuilder.toString(), new HashMap<>());
            if (httpResponse != null) {
                resultArray = JSONArray.parseArray(httpResponse);
                if (CollectionUtils.isEmpty(resultArray))
                    return projectSet;
                resultArray.stream().forEach(a -> {
                    try {
                        JSONObject each = new JSONObject((Map) a);
                        projectSet.add(each.getString("name"));
                    } catch (Exception e) {
                        log.error("gitee数据处理错误：" + e);
                    }
                });

            }
        } while (resultArray != null && resultArray.size() == 20);

        return projectSet;
    }

    @Override
    public String getTokenByPassword(ForkInfoDto forkInfoDto) {

        HttpClient client = HttpClient.newHttpClient();
        StringBuilder bodyBuilder = new StringBuilder();

        bodyBuilder.append("grant_type=password")
                .append("&username=").append(URLEncoder.encode(forkInfoDto.getEmail()))
                .append("&password=").append(URLEncoder.encode(forkInfoDto.getPassword()))
                .append("&client_id=").append(forkInfoDto.getClientId())
                .append("&client_secret=").append(forkInfoDto.getClientSecret())
                .append("&scope=").append(forkInfoDto.getScope());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://gitee.com/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(bodyBuilder.toString(), StandardCharsets.UTF_8))
                .build();

        // 发送请求并接收响应
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject responseBody = JacksonUtils.toObject(JSONObject.class, response.body());
                String access_token = responseBody.getString("access_token");
                Long created_at = responseBody.getLong("created_at");
                Long expires_in = responseBody.getLong("expires_in");
                Long expires_at = created_at + expires_in;
                return access_token;
            } else {
                log.error("Error: Unexpected response code: " + response);
            }
        } catch (IOException | InterruptedException e) {
            log.info(e.getMessage());
        }
        return null;
    }


}
