package dsa1.xczxx.wfs.ws.mservice.oa;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.OaUserResponse;
import dsa1.xczxx.wfs.ws.util.OATaskDataSend;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.login.thirdauth.ThirdSSOAuthHandler;
import kd.bos.login.thirdauth.UserAuthResult;
import kd.bos.login.thirdauth.UserProperType;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.util.RevProxyUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 泛微OA OAuth2单点登录插件
 */
public class TrdSSOAuthLogin implements ThirdSSOAuthHandler {

    private static final Log logger = LogFactory.getLog(TrdSSOAuthLogin.class);

    // OAuth2配置（从MC配置中心读取）
    private static final String KEY_OA_SSO_URL = "sso_url";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";

    @Override
    public void callTrdSSOLogin(HttpServletRequest request,
                                HttpServletResponse response,
                                String s) {
        logger.info("========== callTrdSSOLogin 开始 ==========");

        try {
            String ticket = request.getParameter("ticket");
            String apptype = request.getParameter("apptype");
            logger.info("请求参数 - apptype: {}, ticket: {}", apptype, ticket);

            String host = System.getProperty("host");
            if (StringUtils.isEmpty(host)) {
                logger.error("callTrdSSOLogin  host,请检查MC配置");
                throw new KDBizException("host为空,请检查MC配置");
            }
            // 情况1：从OA待办过来的
            if ("oamessagetodo".equals(apptype)) {
                // 如果没有ticket，说明是第一次访问，需要跳转到OA登录页
                if (ticket == null || ticket.isEmpty()) {
                    String ssoUrl = getOaConfig(KEY_OA_SSO_URL);
                    String clientId = getOaConfig(KEY_CLIENT_ID);
                    String redirectUri   = getBaseURL(request);
                    String url =   host+redirectUri;
                    logger.info("url: {}", ticket);
                    String authUrl = buildAuthorizeUrl(ssoUrl, clientId, url);
                    logger.info("OA待办首次访问，重定向到OA登录页: {}", authUrl);
                    response.sendRedirect(authUrl);
                } else {
                    // 有ticket，说明是OA回调，出现异常才会进入这里
                    logger.info("OA待办回调，交由getTrdSSOAuth处理");
                    // 这里不进行任何操作，等待getTrdSSOAuth被调用
                }
            }
            // 情况2：直接访问星瀚系统（非OA待办）
            else {
                String contextUrl = System.getProperty("domain.contextUrl");
                if (contextUrl == null || contextUrl.isEmpty()) {
                    contextUrl = "";
                }
                logger.info("直接访问星瀚系统，重定向到登录页: {}/login.html", contextUrl);
                response.sendRedirect(contextUrl + "/login.html");
            }

        } catch (Exception e) {
            logger.error("callTrdSSOLogin异常", e);
            try {
                response.sendRedirect("/login.html");
            } catch (IOException ex) {
                logger.error("重定向失败", ex);
            }
        }
    }

    @Override
    public UserAuthResult getTrdSSOAuth(HttpServletRequest request,
                                        HttpServletResponse response) {
        logger.info("========== getTrdSSOAuth 开始 ==========");

        UserAuthResult result = new UserAuthResult();
        result.setSucess(false);

        try {
            String ticket = request.getParameter("ticket");
            String apptype = request.getParameter("apptype");
            logger.info("ticket: {}, apptype: {}", ticket, apptype);
            String host = System.getProperty("host");
            if (StringUtils.isEmpty(host)) {
                logger.error("callTrdSSOLogin  host,请检查MC配置");
                throw new KDBizException("host为空,请检查MC配置");
            }
            // 只处理待办消息
            if (!"oamessagetodo".equals(apptype)) {
                return result;
            }

            if (ticket == null || ticket.isEmpty()) {
                return result;
            }

            // ========== 步骤1：用ticket换取access_token ==========
            logger.info("步骤1：用ticket换取access_token");
            JSONObject tokenResult = getAccessToken(ticket, request,host);

            if (tokenResult == null) {
                logger.error("获取access_token失败 - 返回null");
                return result;
            }

            logger.info("tokenResult: {}", tokenResult.toJSONString());

            // 检查是否成功获取access_token
            String accessToken = null;
            if (tokenResult.containsKey("access_token")) {
                accessToken = tokenResult.getString("access_token");
                logger.info("获取到access_token: {}", accessToken);
            }

            if (accessToken == null || accessToken.isEmpty()) {
                logger.error("未获取到access_token");
                return result;
            }

            // ========== 步骤2：用access_token获取用户信息 ==========
            logger.info("步骤2：用access_token获取用户信息");
            JSONObject userInfoResult = getUserInfo(accessToken);

            if (userInfoResult == null) {
                logger.error("获取用户信息失败");
                return result;
            }

            logger.info("userInfoResult: {}", userInfoResult.toJSONString());

            // ========== 步骤3：解析用户信息 ==========
            String loginid = "";
            String oaid = "";
            String code = userInfoResult.getString("code");
            Integer status = userInfoResult.getInteger("status");
            if ("0".equals(code) && status == 200) {
                // 从id字段获取登录名
                oaid = userInfoResult.getString("id");

                // 可选：从attributes中获取更多信息
                JSONObject attributes = userInfoResult.getJSONObject("attributes");
                if (attributes != null) {
                    loginid = attributes.getString("loginid");

                    logger.info("用户详细信息 - loginid: {}", loginid);
                }
            } else {
                logger.error("获取用户信息失败，code: {}, status: {}, msg: {}",
                        code, status, userInfoResult.getString("msg"));
            }



            if (loginid == null || loginid.isEmpty()) {
                logger.error("未获取到登录名");
                return result;
            }

            logger.info("OA登录名: {}", loginid);

            // ========== 步骤4：查找星瀚用户 ==========
            String cosUserId = findCosUser(loginid);

            if (cosUserId == null) {
                logger.error("未找到对应的星瀚用户: {}", loginid);
                return result;
            }

            // ========== 步骤5：认证成功 ==========
            result.setUserType(UserProperType.UserId);
            result.setUser(cosUserId);
            result.setSucess(true);

            logger.info("认证成功: {} -> {}", loginid, cosUserId);

        } catch (Exception e) {
            logger.error("getTrdSSOAuth异常", e);
        }

        return result;
    }



    /**
     * 用access_token获取用户信息（使用POST方式）
     */
//    private JSONObject getUserInfo(String accessToken) {
//        try {
//            String ssoUrl = getOaConfig(KEY_OA_SSO_URL);
//
//            // 构建请求参数 - POST方式
//            Map<String, String> params = new HashMap<>();
//            params.put("access_token", accessToken);
//
//            logger.info("请求用户信息参数: {}", JSON.toJSONString(params));
//
//            // 调用OA的profile接口（使用POST）
//            String response = OATaskDataSend.doPost3(
//                    JSON.toJSONString(params),
//                    ssoUrl + "/sso/oauth2.0/profile"
//            );
//
//            logger.info("用户信息响应: {}", response);
//
//            return JSON.parseObject(response);
//
//        } catch (Exception e) {
//            logger.error("获取用户信息异常", e);
//            return null;
//        }
//    }

    private JSONObject getUserInfo(String accessToken) {
        try {
            String ssoUrl = getOaConfig(KEY_OA_SSO_URL);

            // 构建GET请求的URL参数

            String requestUrl = ssoUrl + "/sso/oauth2.0/profile" + "?access_token=" + accessToken;

            logger.info("请求用户信息URL: {}", requestUrl);

            // 调用OA的profile接口（使用GET）
            String response = OATaskDataSend.doGet(requestUrl);

            logger.info("用户信息响应: {}", response);

            return JSON.parseObject(response);

        } catch (Exception e) {
            logger.error("获取用户信息异常", e);
            return null;
        }
    }

    private String getOaConfig(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new KDBizException("配置缺失: " + key);
        }
        return value;
    }

    private String getRedirectUri(HttpServletRequest request) {
        String contextPath = RevProxyUtil.getURLContextPath(request);
        return contextPath + "/login/third/oauth2?apptype=oasendmessage";
    }

    private String buildAuthorizeUrl(String ssoUrl, String clientId, String redirectUri) {
        try {
            return ssoUrl + "/sso/oauth2.0/authorize"
                    + "?client_id=" + clientId
                    + "&response_type=code"
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
        } catch (Exception e) {
            logger.error("构建授权URL异常", e);
            throw new RuntimeException("构建授权URL失败", e);
        }
    }

//    private JSONObject getAccessToken(String ticket, HttpServletRequest request) {
//        try {
//            String ssoUrl = getOaConfig(KEY_OA_SSO_URL);
//            String clientId = getOaConfig(KEY_CLIENT_ID);
//            String clientSecret = getOaConfig(KEY_CLIENT_SECRET);
//            String redirectUri   = getBaseURL(request);
//           String  result   =  redirectUri.replaceAll("&ticket=[^&]*", "");
//            Map<String, String> params = new HashMap<>();
//            params.put("client_id", clientId);
//            params.put("client_secret", clientSecret);
//            params.put("grant_type", "authorization_code");
//            params.put("code", ticket);
//            params.put("redirect_uri",  URLEncoder.encode(result, "UTF-8"));
//
//            String response = OATaskDataSend.doPost3(
//                    JSON.toJSONString(params),
//                    ssoUrl + "/sso/oauth2.0/accessToken"
//            );
//
//            return JSON.parseObject(response);
//
//        } catch (Exception e) {
//            logger.error("获取token异常", e);
//            return null;
//        }
//    }



    private JSONObject getAccessToken(String ticket, HttpServletRequest request,String host) {
        try {
            String ssoUrl = getOaConfig(KEY_OA_SSO_URL);
            String clientId = getOaConfig(KEY_CLIENT_ID);
            String clientSecret = getOaConfig(KEY_CLIENT_SECRET);
            String redirectUri = getBaseURL(request);
            String url =  host+redirectUri;
            logger.info("url = {}", url);
            String result = url.replaceAll("&ticket=[^&]*", "");
            logger.info("result = {}", result);
            // 构建GET请求的URL参数

            String requestUrl = ssoUrl + "/sso/oauth2.0/accessToken" + "?client_id=" + URLEncoder.encode(clientId, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") +
                    "&grant_type=" + URLEncoder.encode("authorization_code", "UTF-8") +
                    "&code=" + URLEncoder.encode(ticket, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(result, "UTF-8");

            // 调用GET请求方法
            String response = OATaskDataSend.doGet(requestUrl);

            return JSON.parseObject(response);
        } catch (Exception e) {
            logger.error("获取accessToken失败", e);
            return null;
        }
    }
    private String findCosUser(String oaLoginName) {
        DynamicObject user = QueryServiceHelper.queryOne("bos_user", "id",
                new QFilter[]{new QFilter("dsa1_oamapping", QCP.equals, oaLoginName)});

        return user != null ? user.getString("id") : null;
    }

    /**
     * 获取完整的请求URL（包含参数）
     */
    public static String getFullURL(HttpServletRequest request) {
        StringBuffer fullURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString != null) {
            fullURL.append("?").append(queryString);
        }

        return fullURL.toString();
    }

    /**
     * 获取基础URL（协议+域名+端口+上下文路径）
     */
    public static String getBaseURL(HttpServletRequest request) {

        // 获取请求URI（包含路径和参数）
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        // 拼接完整地址
        String redirectUri =   requestURI;
        if (queryString != null && !queryString.isEmpty()) {
            redirectUri += "?" + queryString;
        }


        return redirectUri;
    }
}