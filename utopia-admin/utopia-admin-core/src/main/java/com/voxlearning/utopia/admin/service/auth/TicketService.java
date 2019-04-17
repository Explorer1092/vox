package com.voxlearning.utopia.admin.service.auth;

import com.voxlearning.alps.core.config.NCS;
import com.voxlearning.alps.core.network.NetworkUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.utopia.admin.controller.AdminAuthController;
import java.net.URLEncoder;
import javax.inject.Named;

@Named
public class TicketService {

  public String validateCasTicket(String ticket) {

    try {
      String url = AdminAuthController.SSO_VALIDATE_URL + "?ticket=" + ticket + "&service=" + URLEncoder
              .encode(getDomainName() +"/auth/verify.vpage","UTF-8");
      AlpsHttpResponse httpResponse = HttpRequestExecutor.defaultInstance().get(url).turnOffLogException().socketTimeout(500).execute();
      String response = httpResponse.getResponseString();

      if (!response.startsWith("yes")) {
        return null;
      } else {
        return response.replaceAll("yes\\s+([^\\s]+)\\s*", "$1");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getDomainName() {
    String domain = NCS.getProperty("product.config.admin.domain", "");
    if (StringUtils.isEmpty(domain)) {
      return "http://"+NetworkUtils.getLocalHost()+":8085";
    } else {
      return "http://" + domain;
    }
  }

}
