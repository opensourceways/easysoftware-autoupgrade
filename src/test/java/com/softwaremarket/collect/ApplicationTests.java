package com.softwaremarket.collect;


import com.alibaba.fastjson.JSONObject;
import com.gitee.sdk.gitee5j.model.Issue;
import com.softwaremarket.collect.config.ForkConfig;
import com.softwaremarket.collect.config.PremiumAppConfig;
import com.softwaremarket.collect.config.PulllRequestConfig;
import com.softwaremarket.collect.config.RpmConfig;
import com.softwaremarket.collect.handler.SoftVersionInfoHandler;
import com.softwaremarket.collect.service.IGiteeService;
import com.softwaremarket.collect.task.PremiumAppVersionTask;
import com.softwaremarket.collect.task.RpmVersionTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@ContextConfiguration
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j

public class ApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;


    @Autowired
    PremiumAppVersionTask upstreamVersionTask;

    @Autowired
    SoftVersionInfoHandler softVersionInfoHandler;
    @Autowired
    ForkConfig forkConfig;
    @Autowired
    IGiteeService iGiteeService;
    @Autowired
    PulllRequestConfig pulllRequestConfig;
    @Autowired
    PremiumAppConfig premiumapp;

    @Autowired
    RpmConfig rpmConfig;
    @Autowired
    RpmVersionTask rpmVersionTask;

    @Before
    public void setupMockMvc() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }


    @Test
    public void test() throws Exception {
       // Issue issue = softVersionInfoHandler.createIssue(forkConfig.getAccessToken(), rpmConfig.getRepo(), "prTitle", "curl");
        //   rpmVersionTask.rpmAutocommit();
       // softVersionInfoHandler.handleRpm(new JSONObject(), new JSONObject());
        //  System.out.println(softVersionInfoHandler.createIssue(forkConfig.getAccessToken(), premiumapp.getOwner(),String.format(pulllRequestConfig.getTitleTemplate(), "redis", "6.2.7", "9.0.4"),premiumapp.getRepo()));
        //  upstreamVersionTask.premiumAppAutocommitLatestOsVersion();
        //   iGiteeService.getContents("li-fanlakers","openeuler-docker-images2024-03-26","/","ba0f9023244ce9c6aee590752e7dca1a");

        //     System.out.println(softVersionInfoHandler.createIssue(forkConfig.getAccessToken(), "opensourceway",String.format(pulllRequestConfig.getTitleTemplate(), "redis", "6.2.7", "9.0.4"),"EaseSearch"));
      /*  RepoBranchesBody repoBranchesBody = new RepoBranchesBody();
        repoBranchesBody.setRefs("master");
        repoBranchesBody.branchName("redis软件市场自动升级"+ DateTimeStrUtils.getTodayDate());
        CompleteBranch completeBranch = iGiteeService.postReposOwnerRepoBranches(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumapp.getRepo(), repoBranchesBody);
        System.out.println(JSONObject.toJSONString(completeBranch));*/

    }
}