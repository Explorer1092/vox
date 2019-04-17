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

package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.sysconfig.RegionConfigService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.agent.service.sysconfig.RegionConfigService.TENCENT_ADVERTISEMENT_NEWS_REGIONS;
import static com.voxlearning.utopia.agent.service.sysconfig.RegionConfigService.TENCENT_ADVERTISEMENT_REGIONS;

/**
 * Created by Alex on 14-9-3.
 */
@Controller
@RequestMapping("/sysconfig/region")
@Slf4j
public class RegionConfigController extends AbstractAgentController {

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private RegionConfigService regionConfigService;

    private static final Map<String, String> grayRegionProduct = OrderProductServiceType.getAllGrayRegionProducts();

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        List<KeyValuePair<String, String>> productTypeList = new ArrayList<>();

        // 增值产品灰度地区配置
        grayRegionProduct.forEach((productName, description) -> productTypeList.add(new KeyValuePair<>(productName + RegionConstants.TAG_GRAY_REGION_SUFFIX, description)));

        // 增加展示腾讯广告联盟广告地区配置 By Wyc 2016-07-29
        productTypeList.add(new KeyValuePair<>(TENCENT_ADVERTISEMENT_REGIONS, "腾讯广告联盟闪屏广告地区"));
        productTypeList.add(new KeyValuePair<>(TENCENT_ADVERTISEMENT_NEWS_REGIONS, "腾讯广告联盟资讯广告地区"));

        // 城市级别
        for (AgentCityLevelType level : AgentCityLevelType.values()) {
            productTypeList.add(new KeyValuePair<>(level.name(), level.value));
        }

        model.addAttribute("productList", productTypeList);

        return "sysconfig/region/regionconfigindex";
    }

    @RequestMapping(value = "loadregionconfig.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String loadRegionConfig() {
        String product = getRequestParameter("product", "");
        if (StringUtils.isEmpty(product)) {
            return "无效的参数!";
        }

        try {
            return JsonUtils.toJson(regionConfigService.loadRegionProductConfig(product, getCurrentUser()));
        } catch (Exception ex) {
            log.error("查询区域设置失败", ex);
            return "查询区域设置失败" + ex.getMessage();
        }
    }

    @RequestMapping(value = "saveregionconfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage saveRegionConfig(String product, String regionList) {
        if (StringUtils.isEmpty(product)) {
            return MapMessage.errorMessage("无效的参数!");
        }

        try {
            // Task #27081 marketing设置黑名单通知邮件 By　Wyc 2016-07-25
            MapMessage retMsg = regionConfigService.saveRegionConfig(product, regionList, getCurrentUser());
            if (retMsg.isSuccess() && RuntimeMode.current().ge(Mode.STAGING)) {
                String receiver = "li.xiao@17zuoye.com;" + "jinhai.li@17zuoye.com";
                Map<String, Object> content = new HashMap<>();
                content.put("user", getCurrentUser());
                content.put("product", product);
                content.put("regions", regionList);
                content.put("time", DateUtils.getNowSqlDatetime());
                emailServiceClient.createTemplateEmail(EmailTemplate.agentgrayregionchange)
                        .to(receiver)
                        .cc("zhilong.hu@17zuoye.com")
                        .subject("Marketing灰度区域设置变更(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                        .content(content)
                        .send();
            }
            return retMsg;
        } catch (Exception e) {
            log.error("保存区域设置失败", e);
            return MapMessage.errorMessage("保存区域设置失败!" + e.getMessage());
        }
    }

}
