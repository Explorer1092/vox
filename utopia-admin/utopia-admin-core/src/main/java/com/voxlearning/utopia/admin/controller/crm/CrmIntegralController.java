/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * @author Longlong Yu
 * @since 下午3:58,13-6-21.
 */
@Controller
@RequestMapping("/crm/integral")
public class CrmIntegralController extends CrmAbstractController {

    private static List<IntegralType> UNSUPPORTED_INTEGRAL_TYPE = new ArrayList<>();

    static {
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.学生补作作业);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.学生完成作业);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.每个学生完成作业老师获得积分);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.学生完成假期作业);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.成功邀请其他老师);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.PK周冠军);
        UNSUPPORTED_INTEGRAL_TYPE.add(IntegralType.UNKNOWN);

    }

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;

    /**
     * ***********************查询积分*****************************************************************
     */
    @RequestMapping(value = "integraldetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String integralDetail(@RequestParam("userId") Long userId, Model model) {
        User user = userLoaderClient.loadUser(userId);

        String startDate = getRequestString("start");
        String endDate = getRequestString("end");
        Date startTime = null;
        Date endTime = null;
        try {
            if (StringUtils.isNotBlank(startDate)) {
                startTime = DateUtils.stringToDate(startDate + " 00:00:00");
            }
            if (StringUtils.isNotBlank(endDate)) {
                endTime = DateUtils.stringToDate(endDate + " 23:59:59");
            }
        } catch (Exception ignored) {
        }

        List<IntegralHistory> integralHistories = new LinkedList<>();
        // 默认查询3个月内的记录
        if (startTime == null && endTime == null) {
            integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader()
                    .loadUserIntegralHistories(userId, 3);
        } else if (startTime != null && endTime != null && startTime.before(endTime)) {
            integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader()
                    .loadUserIntegralHistories(userId, startTime, endTime);
        } else {
            model.addAttribute("error", "请填写正确的日期区间");
        }

        List<Map<String, Object>> integralHistoryList = new LinkedList<>();
        int sumUp = 0;
        for (IntegralHistory integralHistory : integralHistories) {
            Map<String, Object> integralInfo = new HashMap<>();
            integralInfo.put("id", integralHistory.getId());
            integralInfo.put("createTime", integralHistory.getCreatetime());
            //垃圾数据
            try {
                integralInfo.put("integralType", IntegralType.of(integralHistory.getIntegralType()).getDescription());
            } catch (Exception ignored) {
                //do nothing here
            }
            integralInfo.put("comment", integralHistory.getComment());
            if (user.getUserType() == UserType.TEACHER.getType()) {
                integralInfo.put("integral", integralHistory.getIntegral() / 10);
                sumUp += integralHistory.getIntegral() / 10;
            } else {
                integralInfo.put("integral", integralHistory.getIntegral());
                sumUp += integralHistory.getIntegral();
            }
            //integralInfo.uniqueKey = integralHistory.uniqueKey
            if (integralHistory.getUniqueKey() != null && integralHistory.getUniqueKey().contains("relationUserId:")) {
                integralInfo.put("relationUserId", Long.valueOf(integralHistory.getUniqueKey().substring("relationUserId:".length())));
            }

            integralInfo.put("addIntegralUserId", integralHistory.getAddIntegralUserId());

            integralHistoryList.add(integralInfo);
        }

        List<IntegralType> integralTypeList = new ArrayList<>();
        for (IntegralType type : IntegralType.values()) {
            if (!UNSUPPORTED_INTEGRAL_TYPE.contains(type)) {
                integralTypeList.add(type);
            }
        }

        model.addAttribute("integralTypeList", integralTypeList);
        model.addAttribute("integralHistoryList", integralHistoryList);
        model.addAttribute("userId", userId);
        model.addAttribute("userName", user.fetchRealname());
        model.addAttribute("userType", user.getUserType());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sumUp", sumUp);
        return "crm/integral/integraldetail";
    }

    /**
     * ***********************增加积分*****************************************************************
     */
    @RequestMapping(value = "addintegralhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> addIntegralHistory() {

        Map<String, Object> message = new HashMap<>();

        Long userId;
        Integer integralType;
        Integer integral;
        String comment;

        try {
            userId = Long.parseLong(getRequestParameter("userId", ""));
            integralType = Integer.parseInt(getRequestParameter("integralType", ""));
            integral = Integer.parseInt(getRequestParameter("integral", "").replaceAll("\\s", ""));
            comment = getRequestParameter("comment", "").replaceAll("\\s", "");
        } catch (Exception ignored) {
            message.put("success", false);
            return message;
        }

        if (StringUtils.isBlank(comment) || (integral == null)) {
            message.put("success", false);
            return message;
        }

        //增加积分
        User user = userLoaderClient.loadUser(userId);

//        if ((user.isTeacher() && ((integral > 10000)|| (integral < -10000))) || (user.isStudent() && ((integral > 5000)|| (integral < -5000)))) {
//            message.put("success", false);
//            return message;
//        }

        IntegralHistory integralHistory = new IntegralHistory(userId, integralType, integral);
        if (user.getUserType() == UserType.TEACHER.getType()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail.isPrimarySchool() || teacherDetail.isInfantTeacher()) { // 小学 or 学前老师 * 10
                integralHistory.setIntegral(integralHistory.getIntegral() * 10);
            }
        }
        integralHistory.setComment(comment);
        integralHistory.setAddIntegralUserId(getCurrentAdminUser().getFakeUserId());

        MapMessage msg = userIntegralService.changeIntegral(integralHistory);
        if (!msg.isSuccess()) {
            throw new RuntimeException("Failed change " + userId + " integral," + msg.getInfo());
        }

        String integralTypeDesc = IntegralType.of(integralHistory.getIntegralType()).getDescription();
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() +
                "增加用户积分：" + integralHistory.getIntegral() + "，类型：" + integralTypeDesc;

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.积分修改.name());
        userServiceRecord.setOperationContent("增加用户积分");
        userServiceRecord.setComments(operation + "；说明[" + comment + "]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        message.put("id", integralHistory.getId());
        message.put("userId", integralHistory.getUserId());
        try {
            message.put("integralType", IntegralType.of(integralType).getDescription());
        } catch (Exception ignored) {
            //do nothing here
        }
        message.put("comment", integralHistory.getComment());
        message.put("integral", integral);
        message.put("addIntegralUserId", integralHistory.getAddIntegralUserId());

        message.put("success", true);
        return message;
    }

}
