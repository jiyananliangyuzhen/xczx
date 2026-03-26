package dsa1.xczxx.wfs.ws.util;

import com.alibaba.fastjson.JSONObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OATaskDataSend {
    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    private static final Log log = LogFactory.getLog(OATaskDataSend.class);

    public static JSONObject doPost(String method, String content) throws IOException {
//        log.debug("method = " + method);
        String result;
        String url = "http://192.169.1.67:8082" + method;
//        log.debug("url = " + url);
        List<BasicHeader> header = new ArrayList<>();
        header.add(new BasicHeader("accept", "*/*"));
        header.add(new BasicHeader("connection", "Keep-Alive"));
        header.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        BasicHeader[] basicHeader = header.toArray(new BasicHeader[0]);

        result = HttpUtil.postToString(url, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8), content, basicHeader);
        log.debug("result = " + result);
        return JSONObject.parseObject(result);
    }

    public static String doPost1(Map<String,String> params,String url){
//        log.info("doPost begin ");
        CloseableHttpResponse response = null;
        try {
            Map<String,String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.size();
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> parameters = new ArrayList<>(0);
            if(params != null && params.size() > 0){
                for(String key : params.keySet()){
                    parameters.add(new BasicNameValuePair(key, params.get(key)));
                }
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters,"utf-8");
            if(headers != null && headers.size() > 0){
                for(String key : headers.keySet()){
                    httpPost.setHeader(key, headers.get(key));
                }
            }

            httpPost.setEntity(formEntity);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();//设置请求和传输超时时间
            httpPost.setConfig(requestConfig);
            response = httpclient.execute(httpPost);
            String content = EntityUtils.toString(response.getEntity(),"utf-8");
            int code = response.getStatusLine().getStatusCode();
            if (code != 200) {
                log.error("content = {}",content);
            }
            return content;
        } catch (Exception e) {
            log.error("catch in doPost",e);
            return "推送待办失败失败"+e.getMessage();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
//                httpclient.close();
            } catch (IOException e) {
                log.error("httpclient error ",e);
            }

        }
    }

    public static String doPost2(String params, String url) {
        log.info("doPost begin ");
        CloseableHttpResponse response = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8);
            if (headers != null && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpPost.setHeader(key, headers.get(key));
                }
            }
            httpPost.setEntity(stringEntity);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();//设置请求和传输超时时间
            httpPost.setConfig(requestConfig);
            response = httpclient.execute(httpPost);
            String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int code = response.getStatusLine().getStatusCode();
            log.info("code = {}", code);
            if (code != 200) {
                log.error("content = {}", content);
            }
            return content;
        } catch (Exception e) {
            return "接口访问失败" + e.getMessage();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
//                httpclient.close();
            } catch (IOException e) {
                log.error("httpclient error ", e);
            }

        }
    }

    public static String doPost3(String params, String url) {
        log.info("doPost begin ");
        CloseableHttpResponse response = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8);
            if (headers != null && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpPost.setHeader(key, headers.get(key));
                }
            }
            httpPost.setEntity(stringEntity);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();//设置请求和传输超时时间
            httpPost.setConfig(requestConfig);
            response = httpclient.execute(httpPost);
            String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int code = response.getStatusLine().getStatusCode();
            log.info("code = {}", code);
            if (code != 200) {
                log.error("content = {}", content);
            }
            return content;
        } catch (Exception e) {
            return "接口访问失败" + e.getMessage();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
//                httpclient.close();
            } catch (IOException e) {
                log.error("httpclient error ", e);
            }

        }
    }


    private static final int DEFAULT_CONNECT_TIMEOUT = 10_000; // 毫秒
    private static final int DEFAULT_SOCKET_TIMEOUT = 10_000; // 毫秒

    public static String execute(String params, String url) {
        log.info("HTTP POST request started. URL: {}", url);

        // 1. 使用try-with-resources自动管理响应资源
        try (CloseableHttpResponse response = executeRequest(params, url)) {
            // 4. 集中处理响应
            return handleResponse(response);
        } catch (Exception e) {
            log.error("HTTP request failed. URL: {}, Error: {}", url, e.getMessage(), e);
            return "接口访问失败: " + e.getMessage();
        }
    }

    private static CloseableHttpResponse executeRequest(String params, String url) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        // 2. 优化header设置
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // 3. 设置请求实体和超时配置
        httpPost.setEntity(new StringEntity(params, StandardCharsets.UTF_8));
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                .build();
        httpPost.setConfig(requestConfig);

        return httpclient.execute(httpPost);
    }

    private static String handleResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        log.info("HTTP response status: {}", statusCode);

        if (statusCode != HttpStatus.SC_OK) {
            log.error("Non-200 response received. Status: {}, Body: {}", statusCode,
                    responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
        }

        return responseBody;
    }


    public static String doGet(String url) {
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
