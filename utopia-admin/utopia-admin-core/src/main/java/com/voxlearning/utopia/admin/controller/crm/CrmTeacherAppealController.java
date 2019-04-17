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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.*;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.dao.feedback.UserAppealPersistence;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherFakeService;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherSummaryService;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.misc.UserAppeal;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingTeacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Summer Yang on 2016/7/22.
 */
@Controller
@RequestMapping("/crm/teacher_appeal")
public class CrmTeacherAppealController extends CrmAbstractController {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private UserManagementClient userManagementClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;

    @Inject private UserAppealPersistence userAppealPersistence;

    @Inject private CrmTeacherSummaryService crmTeacherSummaryService;
    @Inject private CrmTeacherFakeService crmTeacherFakeService;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;

    // 老师申诉首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        UserAppeal.Type type = UserAppeal.Type.valueOf(requestString("type", UserAppeal.Type.FAKE.name()));
        UserAppeal.Status status = UserAppeal.Status.valueOf(requestString("status", UserAppeal.Status.WAIT.name()));
        Long teacherId = requestLong("teacherId");
        Long schoolId = requestLong("schoolId");
        Pageable pageable = buildPageRequest(25);
        Page<UserAppeal> appealPage = loadByPage(status, type, teacherId, schoolId, pageable);
        model.addAttribute("appealPage", appealPage);
        model.addAttribute("types", UserAppeal.Type.values());
        model.addAttribute("statusList", UserAppeal.Status.values());
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);
        return "crm/teacher_appeal/index";
    }

    // 审核申诉
    @RequestMapping(value = "audit.vpage", method = RequestMethod.GET)
    public String audit(Model model) {
        Long appealId = getRequestLong("appealId");
        UserAppeal appeal = userAppealPersistence.load(appealId);
        model.addAttribute("appeal", appeal);
        model.addAttribute("prePath", getPrePath());
        return "crm/teacher_appeal/audit";
    }

    // 处理申诉 （通过）
    @RequestMapping(value = "auditappealpass.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage auditAppealPass() {
        Long appealId = getRequestLong("appealId");
        String comment = getRequestString("comment");
        if (appealId == 0L || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("请输入正确的参数");
        }
        UserAppeal appeal = userAppealPersistence.load(appealId);
        if (appeal == null) {
            return MapMessage.errorMessage("申诉不存在");
        }
        try {
            AuthCurrentAdminUser adminUser = getCurrentAdminUser();
            if (appeal.getType() == UserAppeal.Type.CHEATING) {
                // 作弊老师的处理 取消老师作弊标签
                PossibleCheatingTeacher possibleCheatingTeacher = newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(appeal.getUserId());
                if (possibleCheatingTeacher != null) {
                    newHomeworkServiceClient.disabledPossibleCheatingTeacherById(possibleCheatingTeacher.getId());
                }
            } else if (appeal.getType() == UserAppeal.Type.FAKE) {
                // 人工判假的老师处理
                // set teacher fake in teacher ext attribute
                Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(appeal.getUserId());
                for (Long relId : teacherMainSubIds) {
                    userManagementClient.setTeacherFake(relId, false, null);

                    CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(relId);
                    if (teacherSummary != null && Boolean.TRUE.equals(teacherSummary.getFakeTeacher())) {
                        crmSummaryServiceClient.removeTeacherFakeType(relId);
                        crmTeacherFakeService.defakeTeacher(relId, adminUser);

                        // 记录 UserServiceRecord
                        UserServiceRecord userServiceRecord = new UserServiceRecord();
                        userServiceRecord.setUserId(relId);
                        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
                        userServiceRecord.setOperationContent("解除假老师判定");
                        userServiceRecord.setComments("老师申诉通过,解除假老师判定");
                        userServiceClient.saveUserServiceRecord(userServiceRecord);
                    }
                }
            }
            Date date = new Date();
            appeal.setAuditId(adminUser.getAdminUserName());
            appeal.setAuditTime(date);
            appeal.setComment(comment);
            appeal.setStatus(UserAppeal.Status.PASS);
            appeal.setUpdateDatetime(date);
            userAppealPersistence.replace(appeal);
            // 给用户发三端消息
            return sendUserAppealAuditPassMessage(appeal);
        } catch (Exception ex) {
            logger.error("audit user appeal error, error is {}", ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    // 处理申诉 （驳回）
    @RequestMapping(value = "auditappealunpass.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage auditAppealUnPass() {
        Long appealId = getRequestLong("appealId");
        String comment = getRequestString("comment");
        if (appealId == 0L || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("请输入正确的参数");
        }
        UserAppeal appeal = userAppealPersistence.load(appealId);
        if (appeal == null) {
            return MapMessage.errorMessage("申诉不存在");
        }
        try {
            AuthCurrentAdminUser adminUser = getCurrentAdminUser();
            Date date = new Date();
            appeal.setAuditId(adminUser.getAdminUserName());
            appeal.setAuditTime(date);
            appeal.setComment(comment);
            appeal.setStatus(UserAppeal.Status.UNPASS);
            appeal.setUpdateDatetime(date);
            userAppealPersistence.replace(appeal);
            // 给用户发三端消息
            return sendUserAppealAuditPassMessage(appeal);
        } catch (Exception ex) {
            logger.error("audit user appeal error, error is {}", ex.getMessage());
            return MapMessage.errorMessage();
        }
    }


    private MapMessage sendUserAppealAuditPassMessage(UserAppeal appeal) {
        try {
            String comment = "";
            if (appeal.getType() == UserAppeal.Type.CHEATING) {
                if (appeal.getStatus() == UserAppeal.Status.PASS) {
                    comment = "您的作业异常申诉已被受理，经过人工审核后认定：作业正常。" +
                            "现已取消本次系统给出的违规记录，相应被冻结的园丁豆也已补发至您的个人账户。感谢您对一起作业的理解和支持！";
                } else if (appeal.getStatus() == UserAppeal.Status.UNPASS) {
                    comment = "很抱歉，您的作业异常申诉未通过审核，异常原因为：" + appeal.getComment() + "。请您今后规范使用一起作业，感谢您的理解和支持！";
                }
            } else if (appeal.getType() == UserAppeal.Type.FAKE) {
                if (appeal.getStatus() == UserAppeal.Status.PASS) {
                    comment = "很高兴地通知您，您的账号异常申诉已通过，账号使用也已恢复正常。感谢您对一起作业的理解和支持！";
                } else if (appeal.getStatus() == UserAppeal.Status.UNPASS) {
                    comment = "您的账号异常申诉已被受理，但遗憾的是，您的老师资质未通过人工审核，异常原因为：" + appeal.getComment() + "。如有疑议，请拨打客服电话进行咨询。";
                }
            }
            if (StringUtils.isBlank(comment)) {
                return MapMessage.errorMessage("消息发送失败");
            }
            // 系统消息
            messageCommandServiceClient.getMessageCommandService().sendUserMessage(appeal.getUserId(), comment);
            // 微信模板消息
            Map<Long, List<UserWechatRef>> refMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(appeal.getUserId()), WechatType.TEACHER);
            if (refMap != null && CollectionUtils.isNotEmpty(refMap.get(appeal.getUserId()))) {
                for (UserWechatRef ref : refMap.get(appeal.getUserId()))
                    sendWechatNotice(appeal.getUserId(), comment, ref.getOpenId(), false);
            }
            // app小铃铛消息
            //新消息中心
            AppMessage message = new AppMessage();
            message.setUserId(appeal.getUserId());
            message.setMessageType(TeacherMessageType.ACTIVIY.getType());
            message.setTitle("系统通知");
            message.setContent(comment);
            message.setImageUrl("");
            message.setLinkUrl(""); // 这里写相对地址
            message.setLinkType(1);
            message.setIsTop(false);
            message.setTopEndTime(0L);
            message.setExtInfo(new HashMap<>());
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("send appeal message error! tid is {}", appeal.getUserId(), ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    private void sendWechatNotice(Long teacherId, String content, String openId, Boolean hasUrl) {
        Date date = new Date();
        WechatNotice notice = new WechatNotice();
        notice.setMessageType(WechatNoticeType.TEACHER_FAKE_NOTICE.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(date);
        notice.setExpireTime(DateUtils.calculateDateDay(date, 1));
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("content", content);
        if (hasUrl) {
            contentMap.put("url", contentMap.put("url", ProductConfig.getMainSiteBaseUrl() + "/ucenter/appeal.vpage?type=FAKE"));
        } else {
            contentMap.put("url", "");
        }
        notice.setMessage(JsonUtils.toJson(contentMap));
        notice.setUserId(teacherId);
        notice.setOpenId(openId);
        wechatServiceClient.persistWechatNotice(notice);
    }

    private Page<UserAppeal> loadByPage(final UserAppeal.Status status,
                                        final UserAppeal.Type type,
                                        final Long teacherId,
                                        final Long schoolId,
                                        final Pageable pageable) {
        List<Criteria> list = new LinkedList<>();
        if (teacherId != null && teacherId != 0) {
            list.add(Criteria.where("USER_ID").is(teacherId));
        }
        if (schoolId != null && schoolId != 0) {
            list.add(Criteria.where("SCHOOL_ID").is(schoolId));
        }
        if (status != null) {
            list.add(Criteria.where("STATUS").is(status));
        }
        if (type != null) {
            list.add(Criteria.where("TYPE").is(type));
        }
        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        long total = userAppealPersistence.count(Query.query(criteria));

        Pageable request = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.Direction.DESC, "CREATE_DATETIME");
        Query query = Query.query(criteria).with(request);
        List<UserAppeal> content = userAppealPersistence.query(query);

        return new PageImpl<>(content, request, total);
    }
}
