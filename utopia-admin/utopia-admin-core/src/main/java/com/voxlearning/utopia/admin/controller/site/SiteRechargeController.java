/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-4-15.
 */
@Controller
@RequestMapping("/site/recharge")
public class SiteRechargeController extends AbstractAdminSystemController {

    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;

    @Inject SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @RequestMapping(value = "rechargehome.vpage", method = RequestMethod.GET)
    String batchAwardHomepage(Model model) {
        return "site/recharge/rechargehome";
    }

    @RequestMapping(value = "genconfirmdata.vpage", method = RequestMethod.POST)
    String genConfirmData(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("内容不能为空");
        }
        String[] recharges = content.split("\\n");
        List<WirelessCharging> chargings = new ArrayList<>();
        List<String> wrongContent = new ArrayList<>();
        int totalAmount = 0;
        for (String m : recharges) {
            try {
                String[] info = m.split("\\t");
                String charegeType = StringUtils.deleteWhitespace(info[0]);
                String charegeDesc = StringUtils.deleteWhitespace(info[1]);
                String userId = StringUtils.deleteWhitespace(info[2]);
                String mobile = StringUtils.deleteWhitespace(info[3]);
                String amount = StringUtils.deleteWhitespace(info[4]);
                String smsContent = StringUtils.deleteWhitespace(info[5]);
                String status = StringUtils.deleteWhitespace(info[6]);

                String realMobile = mobile.substring(mobile.indexOf("1"), mobile.indexOf("1") + 11);
                if (!MobileRule.isMobile(realMobile)) {
                    wrongContent.add(m);
                    continue;
                }

                if (StringUtils.isBlank(charegeType) ||
                        StringUtils.isBlank(charegeDesc) ||
                        StringUtils.isBlank(userId) ||
                        StringUtils.isBlank(realMobile) ||
                        StringUtils.isBlank(amount)) {
                    wrongContent.add(m);
                    continue;
                }

                WirelessCharging charging = new WirelessCharging();
                charging.setChargeType(Integer.parseInt(charegeType));
                charging.setChargeDesc(charegeDesc);
                charging.setUserId(Long.parseLong(userId));
                charging.setTargetSensitiveMobile(realMobile);
                charging.setAmount(Integer.parseInt(amount));
                if (StringUtils.isNotBlank(smsContent)) {
                    charging.setNotifySmsMessage(smsContent);
                }
                if (StringUtils.isNotBlank(status)) {
                    charging.setStatus(Integer.parseInt(status));
                } else {
                    charging.setStatus(0);
                }
                chargings.add(charging);
                totalAmount = totalAmount + charging.getAmount();
            } catch (Exception ex) {
                wrongContent.add(m);
                continue;
            }
        }
        model.addAttribute("recharges", chargings);
        model.addAttribute("wrongList", wrongContent);
        model.addAttribute("totalAmount", totalAmount / 100);
        model.addAttribute("dataJson", JsonUtils.toJson(chargings));
        return "site/recharge/rechargeconfirm";
    }

    @RequestMapping(value = "recharge.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recharge(@RequestBody Map<String, Object> map) {
        List<WirelessCharging> dataList = JsonUtils.fromJsonToList(JsonUtils.toJson(map.get("recharges")), WirelessCharging.class);
        if (CollectionUtils.isEmpty(dataList)) {
            return MapMessage.errorMessage("参数错误");
        }
        for (WirelessCharging charging : dataList) {
            charging.setTargetSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(charging.getTargetSensitiveMobile()));
        }
        AlpsFutureBuilder.<WirelessCharging, Boolean>newBuilder()
                .ids(dataList)
                .generator(id -> wirelessChargingServiceClient.getWirelessChargingService()
                        .createWirelessChargingOrder(id))
                .buildList()
                .awaitUninterruptibly();
        addAdminLog("管理员批量充值话费，共充值" + dataList.size() + "条记录");
        return MapMessage.successMessage("成功发送充值请求");
    }
}
