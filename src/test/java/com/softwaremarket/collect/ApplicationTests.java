package com.softwaremarket.collect;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sdk.gitee5j.model.CompleteBranch;
import com.gitee.sdk.gitee5j.model.Issue;
import com.gitee.sdk.gitee5j.model.RepoBranchesBody;
import com.gitee.sdk.gitee5j.model.RepoPullsBody;
import com.softwaremarket.collect.config.ForkConfig;
import com.softwaremarket.collect.config.PremiumAppConfig;
import com.softwaremarket.collect.config.PulllRequestConfig;
import com.softwaremarket.collect.handler.SoftVersionInfoHandler;
import com.softwaremarket.collect.service.IGiteeService;
import com.softwaremarket.collect.task.UpstreamVersionTask;
import com.softwaremarket.collect.util.DateTimeStrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    UpstreamVersionTask upstreamVersionTask;

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

    @Before
    public void setupMockMvc() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }


    @Test
    public void test() throws Exception {
        //  System.out.println(softVersionInfoHandler.createIssue(forkConfig.getAccessToken(), premiumapp.getOwner(),String.format(pulllRequestConfig.getTitleTemplate(), "redis", "6.2.7", "9.0.4"),premiumapp.getRepo()));
          upstreamVersionTask.getUpstreamVersionInfo();
        //   iGiteeService.getContents("li-fanlakers","openeuler-docker-images2024-03-26","/","ba0f9023244ce9c6aee590752e7dca1a");

   //     System.out.println(softVersionInfoHandler.createIssue(forkConfig.getAccessToken(), "opensourceway",String.format(pulllRequestConfig.getTitleTemplate(), "redis", "6.2.7", "9.0.4"),"EaseSearch"));
      /*  RepoBranchesBody repoBranchesBody = new RepoBranchesBody();
        repoBranchesBody.setRefs("master");
        repoBranchesBody.branchName("redis软件市场自动升级"+ DateTimeStrUtils.getTodayDate());
        CompleteBranch completeBranch = iGiteeService.postReposOwnerRepoBranches(forkConfig.getAccessToken(), forkConfig.getOwner(), premiumapp.getRepo(), repoBranchesBody);
        System.out.println(JSONObject.toJSONString(completeBranch));*/

    }
}