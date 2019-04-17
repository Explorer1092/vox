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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xin.xin
 * @since 2014-04-09
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/sms")
public class SiteSmsController extends SiteAbstractController {

    @Inject private SmsServiceClient smsServiceClient;

    @RequestMapping(value = "messagesend.vpage", method = RequestMethod.GET)
    public String messagesender(Model model) {
        return "/site/sms/messagesend";
    }

    @RequestMapping(value = "messagesend.vpage", method = RequestMethod.POST)
    public String messagesend(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageInfo("请输入手机号和短信内容");
            return "/site/batch/index";
        }
        String[] messages = content.split("\\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();
        for (String m : messages) {
            String[] info = m.split("\\t");
            if (info.length != 2) {
                lstFailed.add(m);
                continue;
            }

            String mobileOrUserId = StringUtils.deleteWhitespace(info[0]);
            String text = StringUtils.deleteWhitespace(info[1]);

            if (text.length() == 0) {
                lstFailed.add(m);
                continue;
            }
            //奇葩的变量名
            String mobile = "";
            if (!MobileRule.isMobile(mobileOrUserId)) {
                mobile = fetchUserMobile(mobileOrUserId);
                if (StringUtils.isEmpty(mobile)) {
                    lstFailed.add(m);
                    continue;
                }
            } else {
                mobile = mobileOrUserId;
            }
            try {
                smsServiceClient.createSmsMessage(mobile)
                        .content(text)
                        .type(SmsType.CRM_BATCH_SEND_SMS.name())
                        .send();
                lstSuccess.add(m);
            } catch (Exception ex) {
                lstFailed.add(m);
            }
        }
        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        return "/site/batch/index";
    }

    @RequestMapping(value = "batchsendsms.vpage", method = RequestMethod.POST)
    public String batchsendsms(String content, String receivers, String smsType, Model model) {
        if (StringUtils.isEmpty(content) || StringUtils.isBlank(receivers)) {
            getAlertMessageManager().addMessageInfo("请输入短信内容和手机号/用户ID");
            return "/site/batch/index";
        }
        SmsType type = SmsType.of(smsType);
        if (type == null || SmsType.NO_CATEGORY == type) {
            getAlertMessageManager().addMessageInfo("短信类型错误");
            return "/site/batch/index";
        }
        String[] rcvrArr = receivers.split("\\n");
        Set<String> mobileSet = new HashSet<>();
        List<String> failList = new ArrayList<>();
        final boolean isMobileRcvr = MobileRule.isMobile(StringUtils.deleteWhitespace(rcvrArr[0]));
        if (isMobileRcvr) {
            for (String e : rcvrArr) {
                final String mobile = StringUtils.deleteWhitespace(e);
                if (!MobileRule.isMobile(mobile)) {
                    failList.add(mobile + "    用户手机号码无效");
                    continue;
                }
                mobileSet.add(mobile);
            }
        } else {
            for (String e : rcvrArr) {
                Long userId = null;
                try {
                    userId = Long.parseLong(StringUtils.deleteWhitespace(e));
                } catch (Exception ex) {
                }
                if (userId == null || userId <= 0) {
                    failList.add(userId + "    用户ID无效");
                    continue;
                }
                User user = userLoaderClient.loadUser(userId);
                if (user == null) {
                    failList.add(userId + "    用户无效");
                    continue;
                }
                String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(userId);
                if (authenticatedMobile == null) {
                    failList.add(userId + "    用户未通过此手机号码认证");
                    continue;
                }
                String mobile = authenticatedMobile;
                if (!MobileRule.isMobile(mobile)) {
                    failList.add(userId + "    用户手机号码无效");
                    continue;
                }
                String badMobile = badWordCheckerClient.checkMobileNumBadWord(mobile);
                if (StringUtils.isNoneBlank(badMobile)) {
                    failList.add(userId + "    用户设置了短信屏蔽");
                    continue;
                }
                mobileSet.add(mobile);
            }
        }

        //每次500条
        final int batchCount = 500;
        if (CollectionUtils.isNotEmpty(mobileSet)) {
            List<String> buf = new ArrayList<>();
            for (String mobile : mobileSet) {
                buf.add(mobile);
                if (buf.size() == batchCount) {
                    String mobiles = StringUtils.join(buf, ",");
                    smsServiceClient.createSmsMessage(mobiles).content(content).type(type.name()).send();
                    buf.clear();
                }
            }
            if (!buf.isEmpty()) {
                String mobiles = StringUtils.join(buf, ",");
                smsServiceClient.createSmsMessage(mobiles).content(content).type(type.name()).send();
            }
            addAdminLog("管理员" + getCurrentAdminUser().getAdminUserName() + "执行批量手动发送短信,类型=" + type.getDescription() + ",条数=" + mobileSet.size());
        }
        model.addAttribute("failList", failList);
        model.addAttribute("sendCount", rcvrArr.length);
        model.addAttribute("messageCount", mobileSet.size());
        getAlertMessageManager().addMessageInfo("发送完成");
        return "/site/batch/index";
    }

    private String fetchUserMobile(String source) {
        String mobile = "";
        try {
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(Long.parseLong(source));
            if (userAuthentication != null) {
                //mobile = userAuthentication.getMobile();
                String am = sensitiveUserDataServiceClient.showUserMobile(userAuthentication.getId(), "crm:fetchUserMobile", SafeConverter.toString(userAuthentication.getId()));
                mobile = am;
            }
        } catch (Exception ignore) {
        }
        return mobile;
    }
}
