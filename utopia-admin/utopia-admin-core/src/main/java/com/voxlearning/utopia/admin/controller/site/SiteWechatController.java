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

package com.voxlearning.utopia.admin.controller.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringEscapeUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatService;
import com.voxlearning.galaxy.service.wechat.api.util.OAuthUrlConverter;
import com.voxlearning.galaxy.service.wechat.api.util.ParentWechatInfoProvider;
import com.voxlearning.galaxy.service.wechat.api.util.StudyTogetherWechatInfoProvider;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.api.mapper.RedPackMapper;
import com.voxlearning.utopia.service.business.consumer.BusinessManagementClient;
import com.voxlearning.utopia.service.wechat.api.constants.RedPackCategory;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xiaopeng.yang on 2015/6/8.
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/wechat")
public class SiteWechatController extends SiteAbstractController {
    private final String WECHAT_MENU_QUERY_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=";
    private final String WECHAT_MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=";
    private final String WECHAT_MENU_CREATE_INDIVIDUAL_URL = "https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token=";
    private final String WECHAT_MENU_DEL_INDIVIDUAL_URL = "https://api.weixin.qq.com/cgi-bin/menu/delconditional?access_token=";
    private final String WECHAT_MENU_TEST_INDIVIDUAL_URL = "https://api.weixin.qq.com/cgi-bin/menu/trymatch?access_token=";
    private final String WECHAT_CALLBACK_SERVER_QUERY_URL = "https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=";

    @Inject
    private WechatCodeServiceClient wechatCodeServiceClient;

    @Inject
    private BusinessManagementClient businessManagementClient;
    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;
    @ImportService(interfaceClass = DPWechatService.class)
    private DPWechatService dpWechatService;

    @RequestMapping(value = "sendredpack.vpage", method = RequestMethod.GET)
    String sendRedPack(Model model) {
        return "site/wechat/sendredpack";
    }

    @RequestMapping(value = "genconfirmdata.vpage", method = RequestMethod.POST)
    String genConfirmData(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("内容不能为空");
        }
        String[] recharges = content.split("\\n");
        List<RedPackMapper> chargings = new ArrayList<>();
        List<String> wrongContent = new ArrayList<>();
        int totalAmount = 0;
        for (String m : recharges) {
            try {
                String[] info = m.split("\\t");
                String userId = StringUtils.deleteWhitespace(info[0]);
                String amount = StringUtils.deleteWhitespace(info[1]);
                String redPackType = StringUtils.deleteWhitespace(info[2]);

                if (StringUtils.isBlank(redPackType) ||
                        StringUtils.isBlank(userId) ||
                        StringUtils.isBlank(amount)) {
                    wrongContent.add(m);
                    continue;
                }
                RedPackMapper mapper = new RedPackMapper();
                mapper.setRedPackType(Integer.parseInt(redPackType));
                mapper.setUserId(Long.parseLong(userId));
                mapper.setAmount(Integer.parseInt(amount));
                //类型不正确的， 过滤
                if (RedPackCategory.get(mapper.getRedPackType()) == null) {
                    wrongContent.add(m);
                    continue;
                }
                //没有绑定校园大使微信的老师账号  直接过滤
                Map<Long, List<UserWechatRef>> refMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(mapper.getUserId()), WechatType.AMBASSADOR);
                if (refMap == null || CollectionUtils.isEmpty(refMap.get(mapper.getUserId()))) {
                    wrongContent.add(m);
                    continue;
                }
                List<UserWechatRef> refs = refMap.get(mapper.getUserId()).stream()
                        .filter(u -> !u.getDisabled())
                        .sorted((o1, o2) -> {
                            long time1 = o1.getCreateDatetime().getTime();
                            long time2 = o2.getCreateDatetime().getTime();
                            return Long.compare(time2, time1);
                        }).collect(Collectors.toList());
                UserWechatRef ref = MiscUtils.firstElement(refs);
                String openId = ref.getOpenId();
                mapper.setOpenId(openId);
                chargings.add(mapper);
                totalAmount = totalAmount + Integer.parseInt(amount);
            } catch (Exception ex) {
                log.error("gen red pack error, user is {}, error is {}", m, ex.getMessage());
                wrongContent.add(m);
            }
        }
        model.addAttribute("datas", chargings);
        model.addAttribute("wrongList", wrongContent);
        model.addAttribute("totalAmount", totalAmount / 100);
        model.addAttribute("dataJson", JsonUtils.toJson(chargings));
        return "site/wechat/dataconfirm";
    }


    @RequestMapping(value = "send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recharge(@RequestBody Map<String, Object> map) {
        List<RedPackMapper> dataList = JsonUtils.fromJsonToList(JsonUtils.toJson(map.get("recharges")), RedPackMapper.class);
        if (CollectionUtils.isEmpty(dataList)) {
            return MapMessage.errorMessage("参数错误");
        }
        int errorCount = businessManagementClient.sendRedPacks(dataList);
        addAdminLog("管理员批量发送微信红包，校园大使微信号，共发送" + dataList.size() + "个红包");
        return MapMessage.successMessage("成功发送红包").add("errorCount", errorCount);
    }


    ////////////////////////
    /// 家长微信菜单管理  ////
    ////////////////////////

    //家长微信菜单管理主页
    @RequestMapping(value = "/menu.vpage", method = RequestMethod.GET)
    public String parentMenu(Model model) {
        String type = "parent";
        if (StringUtils.isNotBlank(getRequestString("type"))) {
            type = getRequestString("type");
        }
        if ("parent".equals(type)) {
            model.addAttribute("serverip", getWechatServerIp(dpWechatLoader.getAccessToken(ParentWechatInfoProvider.INSTANCE.wechatInfoContext())));
            model.addAttribute("token", dpWechatLoader.getAccessToken(ParentWechatInfoProvider.INSTANCE.wechatInfoContext()));
        } else if ("studytogether".equals(type)) {
            model.addAttribute("serverip", getWechatServerIp(dpWechatLoader.getAccessToken(StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext())));
            model.addAttribute("token", dpWechatLoader.getAccessToken(StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext()));
        } else {
            model.addAttribute("serverip", getWechatServerIp(accessToken(getWechatType(type))));
            model.addAttribute("token", accessToken(getWechatType(type)));
        }
        model.addAttribute("type", type);
        return "/site/wechat/parentmenu";
    }

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "/site/wechat/menuurl";
    }

    @RequestMapping(value = "/menuUrl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage convertAuthUrl() {
        String url = getRequestString("url");
        if (StringUtils.isBlank(url)) {
            return MapMessage.errorMessage("跳转链接不能为空");
        }
        String menuUrl = OAuthUrlConverter.convertOAuthUrlForBase(studyTogetherAppId(), studyTogetherBaseUrl() + "/auth.vpage", url, "NLG");
        return MapMessage.successMessage().add("menuUrl", menuUrl);
    }

    public String studyTogetherAppId() {
        return ProductConfig.get("wechat.studytogether.appid");
    }

    public String studyTogetherBaseUrl() {
        String url = ProductConfig.get("wechat.studytogether.base_site_url");
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    @RequestMapping(value = "/getmenu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getParentMenu(@RequestParam String type) throws IOException {
        String accessToken = accessToken(getWechatType(type));
        String r = HttpRequestExecutor.defaultInstance().get(WECHAT_MENU_QUERY_URL + accessToken).execute().getResponseString();
        if (null != r) {
            String parentMenu = StringEscapeUtils.unescapeJava(r);
            ObjectMapper parentMapper = JsonObjectMapper.OBJECT_MAPPER;
            Map<String, ObjectMapper> menu = parentMapper.readValue(parentMenu, Map.class);
            return MapMessage.successMessage().add("menu", menu);
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "/setdefaultmenu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setParentDefaultMenu(@RequestParam String type, @RequestParam String content) {
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }

        String accessToken = accessToken(getWechatType(type));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(WECHAT_MENU_CREATE_URL + accessToken)
                .json(content).execute();
        if (response.hasHttpClientException() || null == response.getResponseString()) {
            return MapMessage.errorMessage("调用微信接口超时");
        } else {
            return MapMessage.successMessage("操作完成,返回结果:" + response.getResponseString());
        }
    }

    @RequestMapping(value = "/setindividualparentmenu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setIndividualizationMenu(@RequestParam String type, @RequestParam String groupId, @RequestParam String country, @RequestParam String province, @RequestParam String city, @RequestParam String gender, @RequestParam String os, @RequestParam String content) {
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }

        Map<String, String> ruleMap = new HashMap<>();
        if (StringUtils.isNotBlank(groupId) && NumberUtils.isNumber(groupId)) {
            ruleMap.put("group_id", groupId);
        }
        if (StringUtils.isNotBlank(country)) {
            ruleMap.put("country", country);
        }
        if (StringUtils.isNotBlank(province)) {
            ruleMap.put("province", province);
        }
        if (StringUtils.isNotBlank(city)) {
            ruleMap.put("city", city);
        }
        if (StringUtils.isNotBlank(gender)) {
            ruleMap.put("sex", gender);
        }
        if (StringUtils.isNotBlank(os)) {
            ruleMap.put("client_platform_type", os);
        }

        Map<String, Object> contentMap = JsonUtils.fromJson(content);
        contentMap.put("matchrule", ruleMap);

        String accessToken = accessToken(getWechatType(type));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(WECHAT_MENU_CREATE_INDIVIDUAL_URL + accessToken)
                .json(JsonUtils.toJson(contentMap)).execute();
        if (response.hasHttpClientException() || null == response.getResponseString()) {
            return MapMessage.errorMessage("调用微信接口超时");
        } else {
            return MapMessage.successMessage("操作完成,返回结果:" + response.getResponseString());
        }
    }

    @RequestMapping(value = "/delindividualparentmenu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteIndividualizationMenu(@RequestParam String type, @RequestParam String menuId) {
        if (StringUtils.isBlank(menuId)) {
            return MapMessage.errorMessage("无效menuId");
        }

        Map<String, String> delMap = new HashMap<>();
        delMap.put("menuid", menuId);

        String accessToken = accessToken(getWechatType(type));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(WECHAT_MENU_DEL_INDIVIDUAL_URL + accessToken)
                .json(JsonUtils.toJson(delMap)).execute();
        if (response.hasHttpClientException() || null == response.getResponseString()) {
            return MapMessage.errorMessage("调用微信接口超时");
        } else {
            return MapMessage.successMessage("操作完成,返回结果:" + response.getResponseString());
        }
    }

    @RequestMapping(value = "/testindividualparentmenu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage testIndividualParentMenu(@RequestParam String type, @RequestParam String openId) {
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("请输入openId或微信号");
        }

        Map<String, String> map = new HashMap<>();
        map.put("user_id", openId);

        String accessToken = accessToken(getWechatType(type));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(WECHAT_MENU_TEST_INDIVIDUAL_URL + accessToken)
                .json(JsonUtils.toJson(map)).execute();
        if (response.hasHttpClientException() || null == response.getResponseString()) {
            return MapMessage.errorMessage("调用微信接口超时");
        } else {
            return MapMessage.successMessage("操作完成").add("menu", JsonUtils.fromJson(response.getResponseString()));
        }
    }

    private String getWechatServerIp(String accessToken) {
        String r = HttpRequestExecutor.defaultInstance().get(WECHAT_CALLBACK_SERVER_QUERY_URL + accessToken).execute().getResponseString();
        if (null != r) {
            String serverIp = StringEscapeUtils.unescapeJava(r);
            return serverIp;
        }
        return null;
    }

    private WechatType getWechatType(String type) {
        if (type.toLowerCase().equals("teacher")) {
            return WechatType.TEACHER;
        } else if (type.toLowerCase().equals("chips")) {
            return WechatType.CHIPS;
        } else if (type.toLowerCase().equals("studytogether")) {
            return WechatType.STUDY_TOGETHER;
        }

        return WechatType.PARENT;
    }

    private String accessToken(WechatType type) {
        String accessToken;
        if (type == WechatType.STUDY_TOGETHER) {
            accessToken = dpWechatLoader.getAccessToken(StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext());
        } else if (type == WechatType.PARENT) {
            accessToken = dpWechatLoader.getAccessToken(ParentWechatInfoProvider.INSTANCE.wechatInfoContext());
        } else {
            accessToken = wechatCodeServiceClient.getWechatCodeService()
                    .generateAccessToken(type)
                    .getUninterruptibly();
        }

        return accessToken;
    }
}
