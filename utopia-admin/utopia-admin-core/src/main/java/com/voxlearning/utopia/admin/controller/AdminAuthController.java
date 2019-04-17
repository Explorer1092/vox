/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller;

import com.voxlearning.alps.core.config.NCS;
import com.voxlearning.alps.core.network.NetworkUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.auth.PageRightAppKeyProperties;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.admin.service.auth.TicketService;
import com.voxlearning.utopia.admin.service.management.ManagementService;
import com.voxlearning.utopia.admin.util.AdminAuthUtils;
import com.voxlearning.utopia.admin.util.AdminUserPasswordObscurer;
import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import java.net.URLEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AdminAuthController extends AbstractAdminController {

    static public final String RelativeUriPath_AuthLogin = "/auth/login.vpage";
    static public final String RelativeUriPath_Index = "/index.vpage";
    static public final String SSO_LOGIN_URL = "https://sso.oaloft.com/cas/login";
    static public final String SSO_LOGOUT_URL = "https://sso.oaloft.com/cas/logout";
    static public final String SSO_VALIDATE_URL = "https://sso.oaloft.com/cas/validate";

    @Inject protected ManagementService managementService;
    @Inject protected AdminAuthUtils adminAuthUtils;
    @Inject protected AdminCacheSystem adminCacheSystem;

    @Inject private AdminUserServiceClient adminUserServiceClient;
    @Inject private TicketService ticketService;

    @RequestMapping(value = "login.vpage", method = RequestMethod.GET)
    public String login() {
//        return "auth/login";

      return redirect(SSO_LOGIN_URL + "?service="+ ticketService.getDomainName() + "/auth/verify.vpage");
    }

    @RequestMapping(value = "verify.vpage", method = RequestMethod.GET)
    public String verifyLogin() throws Exception {
      String ticket = getRequestParameter("ticket", "");

      String adminUserName = ticketService.validateCasTicket(ticket);
      if (null == adminUserName) {
        getAlertMessageManager().addMessageError("当前验证已过期");
        return "auth/notAllow";
      }

      AdminUser adminUser = adminUserServiceClient.getAdminUserService()
              .loadAdminUser(adminUserName)
              .getUninterruptibly();
      if (adminUser == null) {
        adminUser = new AdminUser();
        adminUser.setPasswordSalt(RandomUtils.randomString(6));
        adminUser.setPassword(AdminUserPasswordObscurer.obscurePassword("123456", adminUser.getPasswordSalt()));
        adminUser.setAdminUserName(adminUserName);
        adminUser.setDepartmentName("unauth");  //默认加入"未授权组"
        adminUser.setComment("");
        adminUser.setRedmineApikey("");
        adminUser.setRealName(adminUserName);
        adminUser.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        adminUser = adminUserServiceClient.getAdminUserService()
                .persistAdminUser(adminUser)
                .getUninterruptibly();
      }
      AuthCurrentAdminUser authAdminUser = new AuthCurrentAdminUser();
      authAdminUser.setAdminUserName(adminUser.getAdminUserName());
      authAdminUser.setDepartmentName(adminUser.getDepartmentName());
      authAdminUser.setRealName(adminUser.getRealName());
      authAdminUser.setPassword(adminUser.getPassword());
      authAdminUser.setPasswordSalt(adminUser.getPasswordSalt());
      authAdminUser.setCcAgentId(adminUser.getAgentId());
      authAdminUser.setSystemAndPageRightMap(getSystemAndPageRightMap(adminUserName));
      if (StringUtils.isEmpty(adminUser.getRedmineApikey())) {
        authAdminUser.setRedmineApikey(DEFAULT_REDMINE_APIKEY);
      } else {
        authAdminUser.setRedmineApikey(adminUser.getRedmineApikey());
      }

      saveAuthToSession(authAdminUser);
      setUserAndSignToCookie(SafeConverter.toString(authAdminUser.getFakeUserId()), adminAuthUtils.getUserSign(authAdminUser.getFakeUserId()));

      addAdminLog("login", adminUserName);
      return redirect("/");
    }

    @RequestMapping(value = "logout.vpage", method = RequestMethod.GET)
    public String logout() throws Exception{
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser != null) {
            adminCacheSystem.removeAuthUser(adminUser.getFakeUserId());
        }

        removeUserAndSignFromCookie();
        return redirect(SSO_LOGOUT_URL+ "?service=" + URLEncoder
                .encode(ticketService.getDomainName() + "/auth/verify.vpage","UTF-8"));
    }


    @RequestMapping(value = "info.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String info() {
        if (isRequestPost()) {
            AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
            AdminUser adminUser = adminUserServiceClient.getAdminUserService()
                    .loadAdminUser(context.getCurrentAdminUser().getAdminUserName())
                    .getUninterruptibly();

            String currentPassword = getRequestParameter("currentPassword", "");
            String newPassword = getRequestParameter("newPassword", "");
            String confirmPassword = getRequestParameter("confirmPassword", "");

            if (!AdminUserPasswordObscurer.obscurePassword(currentPassword, adminUser.getPasswordSalt()).equals(adminUser.getPassword())) {
                getAlertMessageManager().addMessageError("当前密码不对");
            }

            if (!newPassword.equals(confirmPassword)) {
                getAlertMessageManager().addMessageError("密码不匹配");
            }

            if (newPassword.length() < 6) {
                getAlertMessageManager().addMessageError("密码长度至少6位");
            }

            if (!getAlertMessageManager().hasMessageError()) {
                AdminUser adminUserToUpdate = new AdminUser();
                adminUserToUpdate.setPasswordSalt(RandomUtils.randomString(6));
                adminUserToUpdate.setPassword(AdminUserPasswordObscurer.obscurePassword(newPassword, adminUserToUpdate.getPasswordSalt()));
                adminUserToUpdate.setAdminUserName(context.getCurrentAdminUser().getAdminUserName());
                adminUserServiceClient.getAdminUserService()
                        .modifyAdminUser(adminUserToUpdate)
                        .awaitUninterruptibly();
                addAdminLog("changeSelfPassword");

                getAlertMessageManager().addMessageSuccess("修改密码成功");
            }
        }

        return "auth/info";
    }

    @RequestMapping(value = "auth.vpage", method = {RequestMethod.GET})
    public String auth() {
        String url = getRequestParameter("url", "");
        if (url == null || url.length() == 0) {
            return null;
        }

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return null;
        }

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("token", String.valueOf(adminUser.getFakeUserId()));
        url = UrlUtils.buildUrlQuery(url, parameters);

        return redirect(url);
    }

    @RequestMapping(value = "loginUserInfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loginUserInfo() {
        String token = getRequestString("token");
        if (StringUtils.isBlank(token)) {
            return MapMessage.errorMessage();
        }
        AuthCurrentAdminUser adminUser = adminCacheSystem.loadAuthUser(SafeConverter.toLong(token));
        if (adminUser == null) {
            return MapMessage.errorMessage();
        }

        MapMessage mapMessage = new MapMessage();
        mapMessage.setSuccess(true);
        mapMessage.set("username", adminUser.getAdminUserName());

        return mapMessage;
    }

    /**
     * 获得用户路径-角色Map
     */
    public Map<String, Object> getSystemAndPageRightMap(String adminName) {

        Map<String, Object> systemAndPageRightMap = new HashMap<>();
        for (String systemAppName : PageRightAppKeyProperties.PAGE_RIGHT_APP_KEY_MAP.keySet()) {
            /*
            def systemAppKey = PageRightAppKeyProperties.PAGE_RIGHT_APP_KEY_MAP[systemAppName]
            def appKey = DigestUtils.sha1Hex((adminName + systemAppName + systemAppKey).getBytes("UTF-8"))
            def urlParams = [:]
            urlParams.userName = adminName
            urlParams.appName = systemAppName
            urlParams.appKey = appKey
            String urlBase
            if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
                urlBase = "http://admin.17zuoye.net"
            } else {
                urlBase = ProductConfig.get("admin_site_base_url", "http://admin.test.17zuoye.net")
            }

            def pageRightJSON = HttpUtils.httpPost("${urlBase}/management/api/getUserAppPath.vpage", urlParams).getResponseString()
            def pageRightMap = JsonUtils.fromJson(pageRightJSON)
            systemAndPageRightMap.put(systemAppName, pageRightMap)
            */
            systemAndPageRightMap.put(systemAppName, managementService.apiGetUserAppPath(adminName, systemAppName));
        }

        return systemAndPageRightMap;
    }

    //发送https请求，修改了hostnameverifier，以信任自签名证书
    public static String httpsGet(String url) throws Exception {
        OutputStream out = null;
        String str_return = "";
        try {
            URL console = new URL(new String(url.getBytes("utf-8")));

            HttpURLConnection conn = (HttpURLConnection) console.openConnection();
            //如果是https
            if (conn instanceof HttpsURLConnection) {
                javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
                javax.net.ssl.TrustManager tm = new miTM();
                trustAllCerts[0] = tm;
                javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                        .getInstance("SSL");
                sc.init(null, trustAllCerts, null);
                HttpsURLConnection.setDefaultSSLSocketFactory(sc
                        .getSocketFactory());

                ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equals("cas.17zuoye.net")) {
                            return true;
                        }
                        return false;
                    }
                });
            }
            conn.connect();
            InputStream is = conn.getInputStream();
            DataInputStream indata = new DataInputStream(is);
            String ret = "";

            while (ret != null) {
                ret = indata.readLine();
                if (ret != null && !ret.trim().equals("")) {
                    str_return = str_return + new String(ret.getBytes("ISO-8859-1"), "utf-8");
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return str_return;
    }

    static class miTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

//        public boolean isServerTrusted(
//                java.security.cert.X509Certificate[] certs) {
//            return true;
//        }
//
//        public boolean isClientTrusted(
//                java.security.cert.X509Certificate[] certs) {
//            return true;
//        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }


    private static final String[] hexDigits = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = (int) n / 16;
        int d2 = (int) n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
