/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.controller;

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.consumer.MiscLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.WxConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.nio.charset.Charset;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2016/4/29
 */
@Controller
@RequestMapping(value = "/others")
public class OthersController extends AbstractTeacherWebController {
    @Inject private MiscLoaderClient miscLoaderClient;

    @RequestMapping(value = "/activity/dgmeetingshare.vpage", method = RequestMethod.GET)
    public String dgMeetingShare(Model model) throws Exception {
        return "redirect:/index.vpage";
    }

    // 获取jsapi wx.config的具体内容:appId,timestamp,nonceStr,signature,jsApiList
    // url: window.location.href
    @RequestMapping(value = "getjsapiconfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getJsapiConfig() {
        String url = getRequestString("url");
        int t = getRequestInt("t");
        WechatType type = WechatType.of(t);
        WxConfig wxConfig = new WxConfig(url, tokenHelper.getJsApiTicket(type));
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("config_signature", wxConfig.sha1Sign());
        mapMessage.add("appid", ProductConfig.get(type.getAppId()));
        mapMessage.add("config_timestamp", wxConfig.getTimestamp());
        mapMessage.add("config_nonceStr", wxConfig.getNonce());
        return mapMessage;
    }

    public static void main(String[] args) {
        String url = "http://wechat.17zuoye.com/others/getjsapiconfig.vpage";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url)
                .contentCharset(Charset.forName("UTF-8"))
                .addParameter("url", "http://www.17zuoye.com/static/project/thebrain/index.html")
                .addParameter("t", "0")
                .execute();

        MapMessage result = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);

        System.out.println(result);

        System.out.println(response.getResponseString());
    }
}
