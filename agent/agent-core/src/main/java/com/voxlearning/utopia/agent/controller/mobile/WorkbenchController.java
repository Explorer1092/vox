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

package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentFeedbackType1;
import com.voxlearning.utopia.agent.constants.AgentFeedbackType2;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/7/7
 */
@Controller
@RequestMapping("/mobile/workbench")
public class WorkbenchController extends AbstractAgentController {

    private final String Agent_Feedback_Type = "AGENT_FEEDBACK";
    //由于Agent和主站用户id是两套体系
    //只好将Agent的所有反馈挂在彩娟的测试帐号下
    private final Long Bind_User_Id = 116404L;
    private final Integer Bind_User_Type = 1;

    @Inject private FeedbackServiceClient feedbackServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;

    @Inject
    BaseUserService baseUserService;
    @Inject
    private TeacherResourceService teacherResourceService;

    @RequestMapping(value = "feedback_list.vpage")
    public String feedbackList(Model model) {
        String userId = ConversionUtils.toString(getCurrentUserId());
        Integer secondType = getRequestInt("secondType");
        Integer firstType = getRequestInt("firstType");
        List<UserFeedback> userFeedbackList = feedbackServiceClient.getFeedbackService()
                .findUserFeedbackList(Bind_User_Id)
                .getUninterruptibly();
        userFeedbackList = userFeedbackList.stream()
                .filter(p -> StringUtils.equalsIgnoreCase(p.getFeedbackType(), Agent_Feedback_Type))
                .filter(p -> p.getRealName() != null && p.getRealName().substring(p.getRealName().indexOf("(") + 1, p.getRealName().indexOf(")")).equalsIgnoreCase(userId))
                .collect(Collectors.toList());
        //有选择分类的话再过滤分类
        if (secondType != 0) {
            AgentFeedbackType2 secondFeedbackType = AgentFeedbackType2.of(secondType);
            if (secondFeedbackType == null) {
                model.addAttribute("error", "分类错误，请重新选择分类");
            }
            userFeedbackList = userFeedbackList.stream()
                    .filter(p -> p.getFeedbackSubType2().equalsIgnoreCase(secondFeedbackType.getDesc()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("userFeedbackList", userFeedbackList);
        model.addAttribute("agentFeedbackFirstType", AgentFeedbackType1.toDescMap());
        model.addAttribute("agentFeedbackSecondType", JsonUtils.toJson(AgentFeedbackType2.toMapList()));
        model.addAttribute("firstType", firstType);
        model.addAttribute("secondType", secondType);
        return "mobile/workbench/feedback_list";

    }

    @RequestMapping(value = "feedback_save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage feedbackSave() {
        Integer firstType = getRequestInt("firstType");
        Integer secondType = getRequestInt("secondType");
        String content = getRequestString("content");
        String comment = getRequestString("comment");
        AuthCurrentUser agentUser = getCurrentUser();
        MapMessage message = validateAgentFeedback(firstType, secondType, content);
        if (!message.isSuccess()) {
            return message;
        }
        AgentFeedbackType1 type1 = AgentFeedbackType1.of(firstType);
        AgentFeedbackType2 type2 = AgentFeedbackType2.of(secondType);


        UserFeedback feedback = new UserFeedback();
        feedback.setFeedbackType(Agent_Feedback_Type);
        feedback.setFeedbackSubType1(type1.getDesc());
        feedback.setFeedbackSubType2(type2.getDesc());
        feedback.setContent(content);
        feedback.setComment(comment);
        //由于agent与主站的帐号体系独立的。userId是没法使用的。故统一绑定到一个固定的userId下。
        feedback.setUserId(Bind_User_Id);
        feedback.setUserType(Bind_User_Type);
        feedback.setRealName(agentUser.getRealName() + "(" + agentUser.getUserId() + ")");
        message = feedbackServiceClient.getFeedbackService().saveFeedback(feedback);
        if (message.isSuccess()) {
            return message.setInfo("保存成功");
        } else {
            return message.setInfo("保存失败");
        }
    }

    @RequestMapping(value = "feedback_add.vpage")
    public String feedbackInAdd(Model model) {
        model.addAttribute("agentFeedbackFirstType", AgentFeedbackType1.toDescMap());
        model.addAttribute("agentFeedbackSecondType", JsonUtils.toJson(AgentFeedbackType2.toMapList()));
        return "mobile/workbench/feedback_add";
    }

    @RequestMapping(value = "resetteacherpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("51492b4be41f47ff")
    public MapMessage resetTeacherPassword() {
        long teacherId = getRequestLong("teacherId");
        AuthCurrentUser agentUser = getCurrentUser();
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("老师不存在，请重试");
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacherId);
        if (StringUtils.isBlank(ua.getSensitiveMobile())) {
            return MapMessage.errorMessage("老师未绑定手机号，不能重置密码");
        }

        //公私海场景，判断该用户是否有权限，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(agentUser.getUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }

        String password = RandomUtils.randomString(6);
        MapMessage message = userServiceClient.setPassword(teacher, password);
        if (message.isSuccess()) {
            String payload = teacher.getProfile().getRealname() + "老师您好，您的密码已被重置为" + password + "，登录后可进行修改。如有问题，可拨打400-160-1717";
            userSmsServiceClient.buildSms().to(teacher)
                    .content(payload)
                    .type(SmsType.MARKET_RESET_TEACHER_PWD)
                    .send();

            String operation = "市场人员[" + agentUser.getRealName() + "(" + agentUser.getUserId() + ")]" + "重置用户[" + teacher.getProfile().getRealname() + "(" + teacher.getId() +")]密码。";
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(agentUser.getUserId());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("修改密码");
            userServiceRecord.setComments(operation);
            userServiceRecord.setAdditions("refer:WorkbenchController.resetteacherpassword.vpage");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            //agent log success
            asyncLogService.logResetTeacherPasswd(getCurrentUser(), getRequest().getRequestURI(), "true", "teacherId:" + teacher.getId());
        } else {
            //agent log false
            asyncLogService.logResetTeacherPasswd(
                    getCurrentUser(),
                    getRequest().getRequestURI(),
                    "false",
                    "teacherId:" + teacher.getId());
        }
        return message;
    }

    private MapMessage validateAgentFeedback(Integer firstType, Integer secondType, String content) {
        if (firstType == null || secondType == null || firstType == -1 || secondType == -1) {
            return MapMessage.errorMessage("请选择问题分类");
        }
        AgentFeedbackType1 type1 = AgentFeedbackType1.of(firstType);
        if (type1 == null) {
            return MapMessage.errorMessage("问题分类错误,请重试");
        }
        AgentFeedbackType2 type2 = AgentFeedbackType2.of(secondType);
        if (type2 == null) {
            return MapMessage.errorMessage("问题分类错误,请重试");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("请输入问题描述");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "common_tools.vpage")
    public String commonTools() {
        return "mobile/workbench/common_tools";
    }


}
