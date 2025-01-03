package com.softwaremarket.autoupgrade.service;

import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.*;
import com.softwaremarket.autoupgrade.dto.ForkInfoDto;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface IGiteeService {
    JSONObject fork(Map parameter);


    PullRequest createPullRequest(RepoPullsBody body, String token, String owner, String repo);

    Issue createIssue(String token, String owner, OwnerIssuesBody body);

    List<JSONObject> getV5ReposOwnerRepoPulls(String url);

    List<JSONObject> getContents(String owner, String repo, String path, String token, String branch);

    Tree getReposOwnerRepoGitTreesSha(String token, String owner, String repo, String sha, Integer recursive);

    File getReposOwnerRepoRawPath(String token, String owner, String repo, String path, String ref);

    RepoCommitWithFiles postReposOwnerRepoCommits(String token, String owner, String repo, RepoCommitsBody body);

    PullRequest postReposOwnerRepoPulls(String token, String owner, String repo, RepoPullsBody body);


    CompleteBranch postReposOwnerRepoBranches(String token, String owner, String repo, RepoBranchesBody body);


    HashSet<String> getReposProjects(String repo, String token);

    String  getTokenByPassword(ForkInfoDto forkInfoDto);
}
