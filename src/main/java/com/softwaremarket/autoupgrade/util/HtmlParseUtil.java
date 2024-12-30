package com.softwaremarket.autoupgrade.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HtmlParseUtil {
    private HtmlParseUtil() {

    }

    // 使用 Jsoup 获取页面内容
    public static Document getHtmlDoc(String url) {
        Document doc = null;

        // 使用 Jsoup 获取页面内容
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0") // 设置 User-Agent
                    .timeout(5000)           // 超时时间
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }


    public static String parseMetacpanChanggeLog(Document doc) {
        String text = doc.body().text();
        String content = text.substring(text.indexOf("Changes for version"), text.indexOf("[ Show lessShow more ]"));
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
            String value = stringStringEntry.getValue();
            StringBuilder detailBuilder = new StringBuilder(" ");
            int n=1;
            if (!changeEntries.isEmpty()) {
                for (int i = 0; i < changeEntries.size(); i++) {
                    Element change = changeEntries.get(i);
                    if (value.contains(change.text())) {
                        detailBuilder.append(" - ").append(change.text()).append("\n");
                        n++;
                    }
                }
            }
            detailMap.put(stringStringEntry.getKey(), detailBuilder.toString());
        }
        StringBuilder changgeLogBuilder = new StringBuilder();
        Collections.reverse(titleList);
        for (String s : titleList) {
            changgeLogBuilder.append("- ").append(s).append(detailMap.get(s)).append("\n");
        }

        return changgeLogBuilder.toString();
    }
}
