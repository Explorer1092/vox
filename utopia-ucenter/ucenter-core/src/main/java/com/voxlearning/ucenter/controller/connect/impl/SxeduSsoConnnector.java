package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import javax.inject.Named;

@Named
public class SxeduSsoConnnector extends AbstractSsoConnector {

  @Override
  public MapMessage validateToken(SsoConnections connectionInfo, String ticket) {

    // 验证TICKET获取登录用户信息
    String httpUrl = getTicketValidateUrl(ticket, connectionInfo.getClientId());
    return validateTicket(httpUrl, ticket);

  }

  // 给中央电教馆做得特殊处理，如果用户存在的话直接去首页
  // 如果用户不存在，那么自动生成用户绑定后再去首页
  @Override
  public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validationResult, CookieManager cookieManager) {

    return null;
  }

  private MapMessage validateTicket(String httpUrl, String ticket) {
    AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(httpUrl).execute();

    if (response.hasHttpClientException()) {
      logger.error("Failed to validate ticket:" + ticket + "@cnedu", response.getHttpClientException());
      return MapMessage.errorMessage("Failed to validate ticket");
    }

    if (response.getStatusCode() == 200) {
      Map<String, Object> apiResult = JsonUtils.fromJson(response.getResponseString());
      if (apiResult == null || !apiResult.containsKey("result") || apiResult.get("result").toString().equals("false")) {
        return MapMessage.errorMessage("Failed to validate ticket");
      }
      //返回信息:{"areaCode":"","creaTime":"","deptId":2,"deptName":"","email":"819603 628@qq.com",
      // "fid":21850,"loadName":"teacher","nickName":" 测试","password":"s65 4321","phone":"",
      // "realName":"测试测 试","roleId":1,"uid":33222636,"unitId":1,"u nitName":"超星选修课平台","area":"超星选 修课平台"}
      Map<String, Object> userInfo = (Map) apiResult.get("strUser");

      String userId = SafeConverter.toString(userInfo.get("uid"));
      String name = SafeConverter.toString(userInfo.get("realName"));
      String userType = SafeConverter.toString(userInfo.get("roleId"));
      String userMobile = SafeConverter.toString(userInfo.get("userMobile"));
      //"roleId":1, 1是老师，3是学生
      if (!"1".equals(userType) && !"3".equals(userType)) {
        userType = "1";
      }
      MapMessage result = MapMessage.successMessage();
      result.add("userId", userId);
      result.add("userName", name);
      result.add("userCode", userType);
      if (StringUtils.isNotBlank(userMobile)) {
        result.add("userMobile", userMobile);
      }

      return result;
    }

    return MapMessage.errorMessage("Failed to validate ticket");
  }

  private static String getTicketValidateUrl(String ticket, String id) {
    String host = "";
    if (RuntimeMode.isStaging()) {
      host = "ucenter.staging.17zuoye.net";
    } else if (RuntimeMode.isProduction()) {
      host = "ucenter.17zuoye.com";
    }
    String redirectUrl = "";
    try {
      redirectUrl = URLEncoder.encode("http://"+host+"/","utf-8");//+source + "login/index.vpage"
    } catch (UnsupportedEncodingException e) {
      //
    }
    return "http://oauth.chaoxing.com/api/sso/authTicket?SSOID=" + ticket + "&id=" + id + "&redirectUrl=" + redirectUrl;
  }

}
