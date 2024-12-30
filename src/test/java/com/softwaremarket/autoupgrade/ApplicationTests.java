package com.softwaremarket.autoupgrade;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.softwaremarket.autoupgrade.config.*;
import com.softwaremarket.autoupgrade.dto.MailInfoDto;
import com.softwaremarket.autoupgrade.helper.EasysoftwareVersionHelper;
import com.softwaremarket.autoupgrade.service.IGiteeService;
import com.softwaremarket.autoupgrade.service.impl.GitService;
import com.softwaremarket.autoupgrade.task.ApplicationVersionTask;
import com.softwaremarket.autoupgrade.task.RpmVersionTask;
import com.softwaremarket.autoupgrade.util.Base64Util;
import com.softwaremarket.autoupgrade.util.EmailSenderUtil;
import com.softwaremarket.autoupgrade.util.FileUtil;
import com.softwaremarket.autoupgrade.util.PatchRegexPatterns;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;

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

    @Autowired
    PatchRegexPatterns patchRegexPatterns;

    @Autowired
    GitService gitService;

    @Before
    public void setupMockMvc() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }


    @Test //精品应用软件升级
    public void test_app() throws Exception {
        String a = "a-卡哈汽车";
        System.out.println(a.substring(0, a.length() - 3));
       /* String tempBase64Str = Base64Utils.fileToBase64Str(filePath);
        System.out.println("文件 转 Base64，完成，使用方法【2】反转验证。");


        String fileURL = "https://github.com/AcademySoftwareFoundation/openexr/archive/v3.3.1/openexr-3.3.1.tar.gz";
        String savePath = "C:/repo/openexr-3.3.1.tar.gz";

        try (InputStream in = new URL(fileURL).openStream();
             ReadableByteChannel rbc = Channels.newChannel(in);
             FileOutputStream fos = new FileOutputStream(savePath)) {

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            System.out.println("Download complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        //    rpmVersionTask .refreshUpstreamRepo();
       /* HashSet<String> nameSet = new HashSet<>();
        nameSet.add("mlflow");
        System.out.println(applicationConfig);
        upstreamVersionTask.premiumAppAutocommit();
        // iGiteeService.getTokenByPassword(applicationConfig.getForkInfo());
        Set<String> appNameSet = easysoftwareVersionHelper.getEasysoftApppkgSet();
        System.out.println(EmailSenderUtil.applicationMailMap);*/
        //   MailInfoDto mailInfo = applicationConfig.getMailInfo();
        //   System.out.println(gitService.fetchCommitIdsInRange("C:\\repo\\redis", "7.2.6", "7.4.1"));

        // EmailSenderUtil.sendEmailByQQ(mailInfo.getHost(), mailInfo.getSenderUsername(), mailInfo.getSenderPassword(), mailInfo.getFrom(), "980488905@qq.com", "测试", "是个测试");
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

   /* public static void main(String[] args) {
        StringBuilder content = new StringBuilder();
        try {
            // 指定目标 URL
            String urlString = "https://metacpan.org/dist/Test-File";
            URL url = new URL(urlString);

            // 创建 HttpURLConnection 对象
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 设置请求头（如果需要）
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                }
                in.close();

                // 打印网页内容
                System.out.println("Page Content:");
                System.out.println(content.toString());
            } else {
                System.out.println("Failed to fetch the page. HTTP Response Code: " + responseCode);
            }

            // 断开连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(content.toString());
   //     Document node = Jsoup.parse(renderer.render(document));
    }*/


    public static void main(String[] args) {
        try {
            // 指定目标 URL
            String url = "https://metacpan.org/dist/Test-File";

            // 使用 Jsoup 获取页面内容
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0") // 设置 User-Agent
                    .timeout(5000)           // 超时时间
                    .get();

            // 打印网页标题
            System.out.println("Page Title: " + doc.title());

            // 打印网页内容
            System.out.println("Page Content:");

            String text = doc.body().text();
            String content = text.substring(text.indexOf("Changes for version"), text.indexOf("[ Show lessShow more ]"));

            System.out.println(content);

            // 获取版本信息和发布日期
            Elements whatsnew = doc.select("#whatsnew");
            HashMap<String, String> titleInfoMap = new HashMap<>();

            ArrayList<String> titleList = new ArrayList<>();
            for (int i = whatsnew.size() - 1; i >= 0; i--) {
                Element versionElement = whatsnew.get(i);
                if (versionElement != null) {
                    String versionInfo = versionElement.text();

                    titleList.add(versionInfo);
                    String[] split = content.split(versionInfo);
                    if (split != null && split.length > 1) {
                        content = split[0];
                        String detail = split[1];

                        titleInfoMap.put(versionInfo, detail);
                    }
                }
            }
            HashMap<String, String> detailMap = new HashMap<>();
            // 获取变更日志列表
            Elements changeEntries = doc.select(".change-entries ul li .change-entry");
            for (Map.Entry<String, String> stringStringEntry : titleInfoMap.entrySet()) {
                String key = stringStringEntry.getKey();
                String value = stringStringEntry.getValue();
                StringBuilder detailBuilder = new StringBuilder();
                if (!changeEntries.isEmpty()) {
                    for (Element change : changeEntries) {
                        if (value.contains(change.text())) {
                            detailBuilder.append("-").append(change.text()).append("\n");
                        }
                    }
                }
                detailMap.put(key, detailBuilder.toString());
            }
            StringBuilder changgeLogBuilder = new StringBuilder();
            for (String s : titleList) {
                changgeLogBuilder.append(s).append(":").append(detailMap.get(s)).append("\n");

            }
            System.out.println("responce:"+changgeLogBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}