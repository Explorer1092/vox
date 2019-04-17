package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
public class SxeduyunController extends AbstractWebController {

  @RequestMapping(value = "sxedu/setCookie.vpage")
  public String setCookie(Model model) {
    String ticket = getRequest().getParameter("ticket");
    String gotoURL = "";
    try {
      gotoURL = URLDecoder.decode(getRequest().getParameter("redirectUrl"), "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "redirect:" + gotoURL + "?token=" + ticket;
  }

  public String logout(Model model) {
    return "";
  }

  @RequestMapping(value = "sxedupslogin/index.vpage", method = RequestMethod.GET)
  public String gotoPrimaryHomePage(Model model) {
    String host = "";
    if (RuntimeMode.isStaging()) {
      host = "ucenter.staging.17zuoye.net";
    } else if (RuntimeMode.isProduction()) {
      host = "ucenter.17zuoye.com";
    }
    String url = "/";
    try {
      url = "http://oauth.chaoxing.com/api/sso/preLogin?setCookieUrl="
              +URLEncoder.encode("http://"+host+"/sxedu/setCookie.vpage", "utf-8")+"&redirectUrl="
              + URLEncoder.encode("http://"+host+"/ssologin/"+SsoConnections.SxeduPs.getSource()+".vpage", "utf-8")
              + "&id=" + SsoConnections.SxeduPs.getClientId();
    } catch (UnsupportedEncodingException e) {
      logger.error("", e);
    }
    return "redirect:" + url;
  }

  @RequestMapping(value = "sxedumslogin/index.vpage", method = RequestMethod.GET)
  public String gotoSecondHomePage(Model model) {

    String host = "";
    if (RuntimeMode.isStaging()) {
      host = "ucenter.staging.17zuoye.net";
    } else if (RuntimeMode.isProduction()) {
      host = "ucenter.17zuoye.com";
    }
    String url = "/";
    try {
      url = "http://oauth.chaoxing.com/api/sso/preLogin?setCookieUrl="
              +URLEncoder.encode("http://"+host+"/sxedu/setCookie.vpage", "utf-8")+"&redirectUrl="
              + URLEncoder.encode("http://"+host+"/ssologin/"+SsoConnections.SxeduMs.getSource()+".vpage", "utf-8")
              + "&id=" + SsoConnections.SxeduMs.getClientId();
    } catch (UnsupportedEncodingException e) {
      logger.error("", e);
    }
    return "redirect:" + url;
  }

}
