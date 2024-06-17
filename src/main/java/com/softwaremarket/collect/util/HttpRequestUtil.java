package com.softwaremarket.collect.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.*;


@Slf4j
public class HttpRequestUtil {


    private HttpRequestUtil() {

    }

    // 默认字符集
    private static final String ENCODING = "UTF-8";

    /**
     * @param url      请求地址
     * @param headers  请求头
     * @param data     请求实体
     * @param encoding 字符集
     */
    public static String sendPost(String url, Map<String, String> headers, String data, String encoding) {
        //log.info("进入post请求方法...");
        //log.info("请求入参：URL= " + url);
        //log.info("请求入参：headers=" + JacksonUtils.writeValueAsString(headers));
        //log.info("请求入参：data=" + data);
        // 请求返回结果
        String resultJson = null;
        // 创建Client
        CloseableHttpClient client = HttpClients.createDefault();
        // 创建HttpPost对象
        HttpPost httpPost = new HttpPost();

        try {
            // 设置请求地址
            httpPost.setURI(new URI(url));
            // 设置请求头
            if (headers != null) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue());
                    i++;
                }
                httpPost.setHeaders(allHeader);
            }
            // 设置实体
            httpPost.setEntity(new StringEntity(data));
            // 发送请求,返回响应对象
            CloseableHttpResponse response = client.execute(httpPost);
            // 获取响应状态
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
                // 获取响应结果
                resultJson = EntityUtils.toString(response.getEntity(), encoding);
            } else {
                log.error("响应失败，状态码：" + status + " url:" + url);
            }

        } catch (Exception e) {
            log.error("发送post请求失败", e);
        } finally {
            httpPost.releaseConnection();
        }
        return resultJson;
    }

    /**
     * @param url  请求地址
     * @param data 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     */
    public static String sendPost(String url, JSONObject data) {
        // 设置默认请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");

        return sendPost(url, headers, JacksonUtils.writeValueAsString(data), ENCODING);
    }

    /**
     * @param url    请求地址
     * @param params 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     */
    public static String sendPost(String url, Map<String, Object> params) {
        // 设置默认请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        // 将map转成json
        return sendPost(url, headers, JacksonUtils.writeValueAsString(params), ENCODING);
    }

    /**
     * @param url     请求地址
     * @param headers 请求头
     * @param data    请求实体
     * @return String
     * @throws
     * @Title: sendPost
     */
    public static String sendPost(String url, Map<String, String> headers, JSONObject data) {
        return sendPost(url, headers, JacksonUtils.writeValueAsString(data), ENCODING);
    }

    /**
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求实体
     * @return String
     * @throws
     * @Title: sendPost
     */
    public static String sendPost(String url, Map<String, String> headers, Map<String, String> params) {
        // 将map转成json
        return sendPost(url, headers, JacksonUtils.writeValueAsString(params), ENCODING);
    }

    /**
     * @param url      请求地址
     * @param params   请求参数
     * @param encoding 编码
     * @return String
     * @throws
     * @Title: sendGet
     */
    public static String sendGet(String url, Map<String, Object> params, String encoding) {
        //log.info("进入get请求方法...");
        //log.info("请求入参：URL= " + url);
        //log.info("请求入参：params=" + JacksonUtils.writeValueAsString(params));
        // 请求结果
        String resultJson = null;
        // 创建client
        CloseableHttpClient client = HttpClients.createDefault();
        // 创建HttpGet
        HttpGet httpGet = new HttpGet();
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            // 封装参数
            if (params != null) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    builder.addParameter(param.getKey(), param.getValue().toString());
                }
            }
            URI uri = builder.build();
            log.info("请求地址：" + uri);
            // 设置请求地址
            httpGet.setURI(uri);
            // 发送请求，返回响应对象
            CloseableHttpResponse response = client.execute(httpGet);
            // 获取响应状态
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                // 获取响应数据
                resultJson = EntityUtils.toString(response.getEntity(), encoding);
            } else {
                log.error("响应失败，状态码：" + status + " url:" + uri);
            }
        } catch (Exception e) {
            log.error("发送get请求失败", e);
        } finally {
            httpGet.releaseConnection();
        }
        return resultJson;
    }

    /**
     * @param url    请求地址
     * @param params 请求参数
     * @return String
     * @throws
     * @Title: sendGet
     */
    public static String sendGet(String url, Map<String, Object> params) {
        return sendGet(url, params, ENCODING);
    }

    /**
     * @param url 请求地址
     * @return String
     * @throws
     * @Title: sendGet
     */
    public static String sendGet(String url) {
        return sendGet(url, null, ENCODING);
    }
}
