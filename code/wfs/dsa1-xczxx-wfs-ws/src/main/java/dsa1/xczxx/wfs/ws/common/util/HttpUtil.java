package dsa1.xczxx.wfs.ws.common.util;


import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Jade Xie (xieyucheng-s@inspur.com)
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})

public class HttpUtil {
    private static final Log log = LogFactory.getLog(HttpUtil.class);

    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String OPTIONS = "OPTIONS";
    public static final String HEAD = "HEAD";
    public static final String TRACE = "TRACE";
    public static final String PATCH = "PATCH";
    public static final String CONNECT = "CONNECT";


    /**
     * pool中获得一个connection的超时时间
     */
    private static final int CONNECTION_REQUEST_TIMEOUT = 10 * 1000;

    /**
     * 链接建立的超时时间
     */
    private static final int CONNECT_TIMEOUT = 10 * 1000;

    /**
     * 响应超时时间，超过此时间不再读取响应
     */
    private static final int RESPONSE_TIMEOUT = 120 * 1000;

    /**
     * 默认的 RequestConfig
     */
    private static final RequestConfig REQUEST_CONFIG = RequestConfig
            .custom()
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setSocketTimeout(RESPONSE_TIMEOUT)
            .build();


    /**
     * 发起GET请求
     *
     * @param url         url地址
     * @param contentType ContentType 详见{@link ContentType}
     *                    e.g. {@code org.apache.http.entity.ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)}
     *                    字符编码也可使用{@link Consts}
     *                    注意: org.apache.http.entity.ContentType的字符编码需明确指定以避免意外情况, 详见其源码
     * @return 响应体字符串
     * @throws IOException IO异常
     */
    public static String get(String url, ContentType contentType) throws IOException {
        return get(url, contentType, null);
    }


    /**
     * 发起GET请求
     *
     * @param url         url地址
     * @param contentType ContentType 详见{@link ContentType}
     *                    e.g. {@code org.apache.http.entity.ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)}
     *                    字符编码也可使用{@link Consts}
     *                    注意: org.apache.http.entity.ContentType的字符编码需明确指定以避免意外情况, 详见其源码
     * @param headers     其他请求头, 数组格式, 建议使用 {@link org.apache.http.message.BasicHeader}作为元素, HTTP标头可使用{@link HttpHeaders}
     * @return 响应体字符串
     * @throws IOException IO异常
     */
    public static String get(String url, ContentType contentType, Header[] headers) throws IOException {

        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("参数URL不能为空");
        }

        if (contentType == null) {
            throw new IllegalArgumentException("参数contentType不能为空");
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8).getMimeType());
            if (headers != null && headers.length > 0) {
                httpGet.setHeaders(headers);
            }
            httpGet.setConfig(REQUEST_CONFIG);

            // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST call CloseableHttpResponse#close() from a finally clause.
            // Please note that if response content is not fully consumed the underlying
            // connection cannot be safely re-used and will be shut down and discarded
            // by the connection manager.

            try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                HttpEntity entity1 = response1.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed

                return getResponse(entity1, contentType.getCharset());
            }
        }
    }

    /**
     * 发起POST请求, 返回response响应体
     *
     * @param url         url地址
     * @param contentType ContentType 详见{@link ContentType}
     *                    e.g. {@code org.apache.http.entity.ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)}
     *                    字符编码也可使用{@link Consts}
     *                    注意: org.apache.http.entity.ContentType的字符编码需明确指定以避免意外情况, 详见其源码
     * @param requestBody 请求体字符串
     * @return CloseableHttpResponse
     * @throws IOException IO异常
     */
    public static CloseableHttpResponse postToResponse(String url, ContentType contentType, String requestBody) throws IOException {
        return postToResponse(url, contentType, requestBody, null);
    }

    /**
     * 发起POST请求, 返回响应体
     *
     * @param url         url地址
     * @param contentType ContentType 详见{@link ContentType}
     *                    e.g. {@code org.apache.http.entity.ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)}
     *                    字符编码也可使用{@link Consts}
     *                    注意: org.apache.http.entity.ContentType的字符编码需明确指定以避免意外情况, 详见其源码
     * @param requestBody 请求体字符串
     * @param headers     其他请求头, 数组格式, 建议使用 {@link org.apache.http.message.BasicHeader}作为元素, HTTP标头可使用{@link HttpHeaders}
     * @return CloseableHttpResponse
     * @throws IOException IO异常
     */
    public static CloseableHttpResponse postToResponse(String url, ContentType contentType, String requestBody, Header[] headers) throws IOException {

        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("参数URL不能为空");
        }

        if (contentType == null) {
            throw new IllegalArgumentException("参数contentType不能为空");
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(REQUEST_CONFIG);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            if (headers != null && headers.length > 0) {
                httpPost.setHeaders(headers);
            }
            httpPost.setEntity(new StringEntity(requestBody, contentType));
            return httpclient.execute(httpPost);
        }
    }


    /**
     * 发起POST请求, 返回响应体字符串
     *
     * @param url         url地址
     * @param contentType ContentType 详见{@link ContentType}
     *                    e.g. {@code org.apache.http.entity.ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)}
     *                    字符编码也可使用{@link Consts}
     *                    注意: org.apache.http.entity.ContentType的字符编码需明确指定以避免意外情况, 详见其源码
     * @param requestBody 请求体字符串
     * @param headers     其他请求头, 数组格式, 建议使用 {@link org.apache.http.message.BasicHeader}作为元素, HTTP标头可使用{@link HttpHeaders}
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String postToString(String url, ContentType contentType, String requestBody, Header[] headers) throws IOException {
        try (CloseableHttpResponse response2 = postToResponse(url, contentType, requestBody, headers)) {
            StatusLine statusLine = response2.getStatusLine();
            HttpEntity entity2 = response2.getEntity();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                // do something useful with the response body
                // and ensure it is fully consumed
                return getResponse(entity2, contentType.getCharset());
            } else {

                try {
                    EntityUtils.consume(entity2);
                } catch (Exception e) {

                }
                return "";
            }
        }
    }

    /**
     * 发起POST请求
     *
     * @param url         url地址
     * @param contentType ContentType 详见{@link ContentType}
     *                    e.g. {@code org.apache.http.entity.ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)}
     *                    字符编码也可使用{@link Consts}
     *                    注意: org.apache.http.entity.ContentType的字符编码需明确指定以避免意外情况, 详见其源码
     * @param requestBody 请求体字符串
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String postToString(String url, ContentType contentType, String requestBody) throws IOException {
        return postToString(url, contentType, requestBody, null);
    }

    /**
     * @param entity  响应实体 {@link HttpEntity}
     * @param charset 字符编码
     * @return 响应字符串
     * @throws IOException 异常
     */
    private static String getResponse(HttpEntity entity, Charset charset) throws IOException {

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), charset))) {
            if (!reader.ready()) {
                throw new IOException("reader is not ready");
            }

            char[] buffer = new char[4096];
            int readCount;
            while (true) {
                readCount = reader.read(buffer);
                if (readCount == -1) {
                    break;
                }
                String s = String.valueOf(buffer, 0, readCount);
                result.append(buffer, 0, readCount);
            }
            EntityUtils.consume(entity);
            return result.toString();
        }
    }

    // 添加方法 url是需要调用的地址，param是消息体，用map转换成json字符串就行了
    public static String sendPost(String url, String param, Map headerMap) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            //下面注释的setRequestProperty可要可不要
            conn.setRequestProperty("accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            // conn.setRequestProperty("Connection", "keep-alive");
            //conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            //conn.setRequestProperty("Content-Length", "80");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
            //conn.setRequestProperty("user-agent",
            //"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            OutputStreamWriter outWriter = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
            out = new PrintWriter(outWriter);
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String postData(String url, String param) throws Exception {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            //下面注释的setRequestProperty可要可不要
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/json");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            OutputStreamWriter outWriter = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
            out = new PrintWriter(outWriter);
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;

    }
    public static  String HttpPost_token(String url,String param,String temp){

        OutputStreamWriter out=null;
        BufferedReader in=null;
        HttpURLConnection conn;
        StringBuilder result = new StringBuilder();
        try{
            URL url1=new URL(url);
            //得到connection对象
            conn =(HttpURLConnection) url1.openConnection();

            //允许写出
            conn.setDoOutput(true);

            //允许写入
            conn.setDoInput(true);

            //设置请求方式
            conn.setRequestMethod("POST");
            //连接超时
            conn.setConnectTimeout(60*1000);
            //读取超时
            conn.setReadTimeout(60*1000);

            // 设置通用的请求属性  请求头
            conn.setRequestProperty("Content-type","application/json");
            conn.setRequestProperty("accessToken",temp);

            //设置是否使用缓存，post请求不能使用缓存
            conn.setUseCaches(false);

            //打开连接
            conn.connect();

            //获取输出流
            out=new OutputStreamWriter(conn.getOutputStream());
            out.write(param);
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            //建立一个数据读取流，读取连接返回的数据
            in=new BufferedReader(new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8));
            String line;

            while((line=in.readLine())!=null){
                result.append(line);

            }

        }catch (Exception e){
            e.printStackTrace();
            //使用finally 关闭输入输出流
        }finally {
            try{
                if(out!=null){
                    out.close();
                }
                if (in !=null){
                    in.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return result.toString();
    }
    public static  String HttpPost3(String url,String param){

        OutputStreamWriter out=null;
        BufferedReader in=null;
        HttpURLConnection conn;
        StringBuilder result = new StringBuilder();

        try{

            URL url1=new URL(url);
            //得到connection对象
            conn =(HttpURLConnection) url1.openConnection();

            //允许写出
            conn.setDoOutput(true);

            //允许写入
            conn.setDoInput(true);

            //设置请求方式
            conn.setRequestMethod("POST");
            //连接超时
            conn.setConnectTimeout(60*1000);
            //读取超时
            conn.setReadTimeout(60*1000);

            // 设置通用的请求属性  请求头
            conn.setRequestProperty("Content-type","application/json");
            conn.setRequestProperty("Accept-language","zh-CHS");
            conn.setRequestProperty("X-ECC-Current-Tenant", "10000");

            //设置是否使用缓存，post请求不能使用缓存
            conn.setUseCaches(false);

            //打开连接
            conn.connect();

            //获取输出流
            out=new OutputStreamWriter(conn.getOutputStream());
            out.write(param);
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            //建立一个数据读取流，读取连接返回的数据
            in=new BufferedReader(new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8));
            String line;

            while((line=in.readLine())!=null){
                result.append(line);

            }

        }catch (Exception e){
            e.printStackTrace();
            //使用finally 关闭输入输出流
        }finally {
            try{
                if(out!=null){
                    out.close();
                }
                if (in !=null){
                    in.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static  String HttpPost2(String url){

        OutputStreamWriter out=null;
        BufferedReader in=null;
        HttpURLConnection conn;
        StringBuilder result = new StringBuilder();

        try{

            URL url1=new URL(url);
            //得到connection对象
            conn =(HttpURLConnection) url1.openConnection();

            //允许写出
            conn.setDoOutput(true);

            //允许写入
            conn.setDoInput(true);

            //设置请求方式
            conn.setRequestMethod("POST");
            //连接超时
            conn.setConnectTimeout(60*1000);
            //读取超时
            conn.setReadTimeout(60*1000);

            // 设置通用的请求属性  请求头
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            // conn.setRequestProperty("Accept-language","zh-CHS");
            // conn.setRequestProperty("X-ECC-Current-Tenant", "10000");
            //
            //设置是否使用缓存，post请求不能使用缓存
            conn.setUseCaches(false);

            //打开连接
            conn.connect();

            //获取输出流
            out=new OutputStreamWriter(conn.getOutputStream());
            // out.write(param);
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            //建立一个数据读取流，读取连接返回的数据
            in=new BufferedReader(new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8));
            String line;

            while((line=in.readLine())!=null){
                result.append(line);

            }

        }catch (Exception e){
            e.printStackTrace();
            //使用finally 关闭输入输出流
        }finally {
            try{
                if(out!=null){
                    out.close();
                }
                if (in !=null){
                    in.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static String doPost(String params, String url) {
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
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(6000).build();//设置请求和传输超时时间
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

}



