package com.softwaremarket.collect.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.softwaremarket.collect.config.CollectConfig;
import com.softwaremarket.collect.config.ForkConfig;
import com.softwaremarket.collect.config.GiteeUrlConfig;
import com.softwaremarket.collect.config.PulllRequestConfig;
import com.softwaremarket.collect.enums.CollectEnum;
import com.softwaremarket.collect.service.IGiteeService;
import com.softwaremarket.collect.util.HttpRequestUtil;
import com.softwaremarket.collect.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class GiteeService implements IGiteeService {
    private final PulllRequestConfig pulllRequestConfig;
    private final ForkConfig forkConfig;
    private final GiteeUrlConfig giteeUrlConfig;

    @Override
    public JSONObject fork(Map parameter) {
        String forkUrl = String.format(giteeUrlConfig.getPostV5ReposOwnerRepoForksUrl(), parameter.get("owner"), parameter.get("repo"));
        parameter.remove("owner");
        parameter.remove("repo");
        System.out.println(forkUrl);
        System.out.println(parameter);
        String result = HttpRequestUtil.sendPost(forkUrl, parameter);
        if (!StringUtils.isEmpty(result)) {
            return JacksonUtils.toObject(JSONObject.class, result);
        }
        return new JSONObject();
    }

    @Override
    public Boolean updateStorehouse(Map parameter) {
        return null;
    }

    @Override
    public PullRequest createPullRequest(RepoPullsBody body, String token, String owner, String repo) {

        PullRequest result = null;
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        PullRequestsApi apiInstance = new PullRequestsApi();
        try {
            result = apiInstance.postReposOwnerRepoPulls(owner, repo, body);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Exception when calling PullRequestsApi#postReposOwnerRepoPulls");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Issue createIssue(String token, String owner, OwnerIssuesBody body) {
        Issue result = null;

        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: OAuth2
        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        IssuesApi apiInstance = new IssuesApi();
        try {
            result = apiInstance.postReposOwnerIssues(owner, body);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Exception when calling IssuesApi#postReposOwnerIssues");
            e.printStackTrace();
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
            String url = giteeUrlConfig.getContentsUrl().replace("{owner}", owner).replace("{repo}", repo).replace("{path}", path).replace("{access_token}", token).replace("{ref}", branch);
            String result = HttpRequestUtil.sendGet(url);
            if (!StringUtils.isEmpty(result)) {
                return JacksonUtils.toObjectList(JSONObject.class, result);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Tree getReposOwnerRepoGitTreesSha(String token, String owner, String repo, String sha, Integer recursive) {
        Tree result = null;
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        GitDataApi apiInstance = new GitDataApi();
        // String | 仓库所属空间地址(企业、组织或个人的地址path)
        // String | 仓库路径(path)
        // String | 可以是分支名(如master)、Commit或者目录Tree的SHA值
        // Integer | 赋值为1递归获取目录
        try {
            result = apiInstance.getReposOwnerRepoGitTreesSha(owner, repo, sha, recursive);
            JSON.toJSONString("怎么个事儿：" + result);
        } catch (Exception e) {
            System.err.println("Exception when calling GitDataApi#getReposOwnerRepoGitTreesSha");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public File getReposOwnerRepoRawPath(String token, String owner, String repo, String path, String ref) {
        File result = null;
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        RepositoriesApi apiInstance = new RepositoriesApi();
        try {
            result = apiInstance.getReposOwnerRepoRawPath(owner, repo, path, ref);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Exception when calling RepositoriesApi#getReposOwnerRepoRawPath");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public RepoCommitWithFiles postReposOwnerRepoCommits(String token, String owner, String repo, RepoCommitsBody body) {
        RepoCommitWithFiles result = null;
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: OAuth2
        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        RepositoriesApi apiInstance = new RepositoriesApi();
        // String | 仓库所属空间地址(企业、组织或个人的地址path)
        // String | 仓库路径(path)
        // RepoCommitsBody |
        try {
            result = apiInstance.postReposOwnerRepoCommits(owner, repo, body);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Exception when calling RepositoriesApi#postReposOwnerRepoCommits");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public PullRequest postReposOwnerRepoPulls(String token, String owner, String repo, RepoPullsBody body) {

        ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: OAuth2
        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        PullRequestsApi apiInstance = new PullRequestsApi();
        try {
            PullRequest result = apiInstance.postReposOwnerRepoPulls(owner, repo, body);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Exception when calling PullRequestsApi#postReposOwnerRepoPulls");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public CompleteBranch postReposOwnerRepoBranches(String token, String owner, String repo, RepoBranchesBody body) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        CompleteBranch result = null;
        OAuth OAuth2 = (OAuth) defaultClient.getAuthentication("OAuth2");
        OAuth2.setAccessToken(token);

        RepositoriesApi apiInstance = new RepositoriesApi();
        try {
            result = apiInstance.postReposOwnerRepoBranches(owner, repo, body);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Exception when calling RepositoriesApi#postReposOwnerRepoBranches");
            e.printStackTrace();
        }
        return result;
    }

}
