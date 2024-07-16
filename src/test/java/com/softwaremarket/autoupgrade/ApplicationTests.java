package com.softwaremarket.autoupgrade;


import com.softwaremarket.autoupgrade.config.*;
import com.softwaremarket.autoupgrade.dto.MailInfoDto;
import com.softwaremarket.autoupgrade.helper.EasysoftwareVersionHelper;
import com.softwaremarket.autoupgrade.service.IGiteeService;
import com.softwaremarket.autoupgrade.task.ApplicationVersionTask;
import com.softwaremarket.autoupgrade.task.RpmVersionTask;
import com.softwaremarket.autoupgrade.util.EmailSenderUtil;
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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

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
    ApplicationVersionTask upstreamVersionTask;


    @Autowired
    IGiteeService iGiteeService;


    @Autowired
    RpmConfig rpmConfig;
    @Autowired
    RpmVersionTask rpmVersionTask;
    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    EasysoftwareVersionHelper easysoftwareVersionHelper;

    @Before
    public void setupMockMvc() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test //精品应用软件升级
    public void test_app() throws Exception {
       /* HashSet<String> nameSet = new HashSet<>();
        nameSet.add("mlflow");
        System.out.println(applicationConfig);
        upstreamVersionTask.premiumAppAutocommit();*/
        // iGiteeService.getTokenByPassword(applicationConfig.getForkInfo());
        Set<String> appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        System.out.println(EmailSenderUtil.applicationMailMap);
       /* MailInfoDto mailInfo = applicationConfig.getMailInfo();


        EmailSenderUtil.sendEmailByQQ(mailInfo.getHost(), mailInfo.getSenderUsername(), mailInfo.getSenderPassword(), mailInfo.getFrom(), "", "测试", "是个测试");
*/
    }


    @Test
    public void test_file() throws Exception {
        String fileName = "D:\\curl.spec.txt";
        final String CHARSET_NAME = "UTF-8";
        List<String> content = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), CHARSET_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder contentBuilder = new StringBuilder();
        LocalDate now = LocalDate.now();
        String week = String.valueOf(now.getDayOfWeek()).toLowerCase(Locale.ROOT).substring(0, 3);
        week = week.substring(0, 1).toUpperCase() + week.substring(1);
        String month = String.valueOf(now.getMonth()).toLowerCase(Locale.ROOT).substring(0, 3);
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        String day = String.valueOf(now.getDayOfMonth() < 10 ? "0" + now.getDayOfMonth() : now.getDayOfMonth()).toLowerCase(Locale.ROOT);
        String year = String.valueOf(now.getYear()).toLowerCase(Locale.ROOT);

        String updateTime = new StringBuilder().append(week).append(" ").append(month).append(" ").append(day).append(" ").append(year).toString();
        for (int i = 0; i < content.size(); i++) {
            String s = content.get(i);
            if (s.contains("Version") && s.contains("8.7.1")) {
                s = s.replace("8.7.1", "8.9.2");
            }
            contentBuilder.append(s);
            if (i < content.size() - 1) {
                contentBuilder.append("\n");
            }

            if (s.contains("changelog")) {
                contentBuilder.append("* TIME lifan <lifan140@h-partners.com> - VERSION-1".replace("TIME", updateTime).replace("version", "9.0.2")).append("\n");
                contentBuilder.append("- Type:requirement").append("\n");
                contentBuilder.append("- CVE:NA").append("\n");
                contentBuilder.append("- SUG:NA").append("\n");
                contentBuilder.append("- DESC:update curl to 8.7.1").append("\n");
            }
        }
        System.out.println(contentBuilder);
    }

    @Test
    public void test_rpm() throws Exception {
        //   softVersionInfoHandler.handleRpm(new JSONObject(), new JSONObject());
    }


    @Test //精品应用软件升级-最新欧拉
    public void test_appLatestOe() throws Exception {
        HashSet<String> nameSet = new HashSet<>();
        nameSet.add("loki");
        upstreamVersionTask.premiumAppAutocommitLatestOsVersion(nameSet);
    }

}