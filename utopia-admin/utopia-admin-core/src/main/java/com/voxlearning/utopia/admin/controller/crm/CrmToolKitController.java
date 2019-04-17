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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientCategory;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2015/9/21.
 */

@Controller
@RequestMapping(value = "/crm/toolkit")
public class CrmToolKitController extends CrmAbstractController {

    /**
     * 跳转到工具箱操作页面
     */
    @RequestMapping(value = "toolkit.vpage", method = RequestMethod.GET)
    String toolkit(Model model) {
        return "crm/teachernew/toolkit";
    }

    /**
     * 清除手机号
     * 这里的逻辑跟阿娟工具箱是一样的 By Wyc 2016-07-05
     */
    @RequestMapping(value = "cleanupBindedMobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cleanupBindedMobile(@RequestParam(value = "mobile", required = false) String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("手机号不能为空");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的手机号码");
        }
        String reason = getRequestString("reason");
        if (StringUtils.isBlank(reason)) {
            return MapMessage.errorMessage("原因不能为空");
        }
        String[] roles = getRequest().getParameterValues("cleanup_role");
        if (roles == null || roles.length == 0) {
            return MapMessage.errorMessage("请选择角色");
        }
        //因为这里是按前台选择的角色列表来清除。
        // 故adminLog会有多条。但CustomerLog只会是有绑定的角色才有
        StringBuilder info = new StringBuilder();
        for (String role : roles) {
            UserType userType;
            RecordType recordType;
            switch (role) {
                case "teacher":
                    userType = UserType.TEACHER;
                    recordType = RecordType.老师操作;
                    break;
                case "student":
                    userType = UserType.STUDENT;
                    recordType = RecordType.学生操作;
                    break;
                case "parent":
                    userType = UserType.PARENT;
                    recordType = RecordType.家长操作;
                    break;
                default:
                    continue;
            }
            MapMessage message;
            try {
                // 检查手机号
                UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, userType);
                if (ua == null) {
                    info.append("\n").append(userType.getDescription()).append("手机号").append(mobile).append("错误或不存在，请核实后再操作");
                    message = MapMessage.errorMessage();
                } else {
                    message = userServiceClient.cleanupBindedMobile(getCurrentAdminUser().getAdminUserName(), mobile, userType);
                }
            } catch (Exception ex) {
                logger.error("Failed to cleanup binded mobile '{}/{}'", mobile, userType, ex);
                message = MapMessage.errorMessage();
            }
            if (message.isSuccess()) {
                Long targetId = (Long) message.get("userId");
                String desc = "清除角色" + role + "，原因：" + reason + "，结果：" + message.isSuccess();
                addAdminLog("cleanupBindedMobile", targetId, StringUtils.mobileObscure(mobile), desc, null);
                if (targetId != null) {
                    String operation = "管理员清除了用户的手机号：" + StringUtils.mobileObscure(mobile);

                    // 记录 UserServiceRecord
                    UserServiceRecord userServiceRecord = new UserServiceRecord();
                    userServiceRecord.setUserId(targetId);
                    userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                    userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                    userServiceRecord.setOperationContent("解绑手机号");
                    userServiceRecord.setComments(operation);
                    userServiceClient.saveUserServiceRecord(userServiceRecord);

                    User user = userLoaderClient.loadUser(targetId, userType);
                    if (user != null && !user.isDisabledTrue()) {
                        info.append("\n").append("已解除").append(userType.getDescription()).append("手机号").append(StringUtils.mobileObscure(mobile)).append("与").append(user.getProfile().getRealname()).append("(").append(targetId).append(")的绑定关系。");
                    } else {
                        info.append("\n").append(userType.getDescription()).append("手机号").append(StringUtils.mobileObscure(mobile)).append("错误或不存在，请核实后再操作。");
                    }
                } else {
                    info.append("\n").append(userType.getDescription()).append("手机号").append(StringUtils.mobileObscure(mobile)).append("错误或不存在，请核实后再操作。");
                }
            }
        }
        return MapMessage.successMessage(info.toString());
    }

    /**
     * 清除邮箱
     */
    @RequestMapping(value = "cleanupBindedEmail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cleanupBindedEmail(@RequestParam(value = "email", required = false) String email) {
        if (StringUtils.isBlank(email)) {
            return MapMessage.errorMessage("清楚绑定邮箱:邮箱不能为空");
        }
        if (!getAlertMessageManager().hasMessageError()) {
            MapMessage message;
            try {
                message = userServiceClient.cleanupBindedEmail(getCurrentAdminUser().getAdminUserName(), email);
            } catch (Exception ex) {
                logger.error("Failed to cleanup binded email '{}'", email, ex);
                message = MapMessage.errorMessage();
            }
            if (!message.isSuccess()) {
                return message.setInfo("哈哈，阿娟在清除绑定邮箱" + email + "的时候出错了！");
            }
            addAdminLog("cleanupBindedMobile", email);
            return message.setInfo("阿娟应该是清除了绑定的邮箱" + email + "。");
        }
        return MapMessage.errorMessage("清楚绑定邮箱:参数错误");
    }

    /**
     * 根据手机号查询发送的信息
     */
    @RequestMapping(value = "findMobileMessage.vpage", method = RequestMethod.POST)
    public String findSmsMessage(@RequestParam(value = "mobile", required = false) String mobile, Model model) {
        if (StringUtils.isBlank(mobile)) {
            model.addAttribute("error", "查询短信：手机号码不能为空");
            return "crm/teachernew/toolkit";
        }
        if (!getAlertMessageManager().hasMessageError()) {
            List<Map<String, Object>> userSmsMessages = smsLoaderClient.getSmsLoader().loadUserSmsMessage(mobile, 10)
                    .stream()
                    .map(sms -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("createTime", sms.getCreateTime());
                        info.put("smsType", SmsType.of(sms.getSmsType()));
                        info.put("smsContent", sms.getSmsContent());
                        info.put("status", sms.getStatus());
                        info.put("errorCode", sms.getErrorCode());
                        info.put("errorDesc", sms.getErrorDesc());
                        SmsClientType channel = null;
                        try {
                            channel = SmsClientType.valueOf(sms.getSmsChannel());
                        } catch (Exception ignored) {
                        }
                        if (channel == null) {
                            info.put("smsChannel", null);
                            info.put("verification", false);
                        } else {
                            info.put("smsChannel", channel.name());
                            info.put("verification", SmsClientCategory.verification_code == channel.getCategory() || SmsClientCategory.voice_verification_code == channel.getCategory());
                        }
                        info.put("consumed", Boolean.TRUE.equals(sms.getConsumed()));
                        info.put("receiveTime", sms.getReceiveTime());
                        return info;
                    }).collect(Collectors.toList());

            addAdminLog("findSmsMessageByMobile", StringUtils.mobileObscure(mobile), (userSmsMessages.size() > 0) ? "查询出" + userSmsMessages.size() + "条" : "无结果集");

            model.addAttribute("smsMessageList", userSmsMessages);
            model.addAttribute("mobile", mobile);
            return "crm/teachernew/toolkit";
        }
        model.addAttribute("error", "查询短信：参数错误");
        return "crm/teachernew/toolkit";
    }

//    @RequestMapping(value = "setVitality.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage setVitality(@RequestParam(value = "userIds", required = false) String userIds,
//                                  @RequestParam(value = "vitality", required = false) Integer vitality) {
//        if (StringUtils.isEmpty(userIds) || vitality == null) {
//            return MapMessage.errorMessage("参数不全！添加活力失败");
//        }
//        String[] ids = StringUtils.split(userIds, ",");
//        if (vitality > 0) {
//            vitality = vitality > 5 ? 5 : vitality;
//        }
//
//        for (String userId : ids) {
//            Long uid = conversionService.convert(userId, Long.class);
//            if (vitality > 0) {
//                String description = "CRM增加通天塔活力,操作者" + getCurrentAdminUser().getAdminUserName();
//                babelVitalityServiceClient.increaseVitality(uid, vitality, description);
//            } else {
//                int nowBalance = babelVitalityServiceClient.getCurrentBalance(uid).getBalance();
//                if (-vitality > nowBalance) {
//                    vitality = -nowBalance;
//                }
//                String description = "CRM减少通天塔活力,操作者" + getCurrentAdminUser().getAdminUserName();
//                babelVitalityServiceClient.decreaseVitality(uid, -vitality, description);
//            }
//        }
//        return MapMessage.errorMessage("添加活力成功");
//    }
}
