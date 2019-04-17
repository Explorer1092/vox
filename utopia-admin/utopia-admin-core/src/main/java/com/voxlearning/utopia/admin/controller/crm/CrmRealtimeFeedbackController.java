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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.dao.feedback.UserFeedbackPersistence;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.api.constants.UserFeedbackForEmail;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;
import com.voxlearning.utopia.service.feedback.client.FeedbackLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * Created by Shuai Huan on 2014/6/7.
 */
@Controller
@RequestMapping("/crm/realtimefeedback")
public class CrmRealtimeFeedbackController extends CrmAbstractController {

    private static List<String> feedbackTypeList = Arrays.asList(
            "账号与班级",
            "数学基础作业",
            "数学同步试题",
            "PK问题",
            "通天塔问题",
            "走遍美国问题",
            "爱儿优问题",
            "进击的三国问题",
            "洛亚传说问题",
            "沃克大冒险问题",
            "宠物大乱斗问题",
            "洛亚传说问题",
            "英语基础作业",
            "学生移动端",
            "中学移动问题",
            "其他问题",
            "改进建议",
            "银币问题",
            "课外练习",
            "大爆料问题",
            "奖品中心问题",
            "英语同步试题",
            "英语阅读应用",
            "重置密码",
            "移动版问题"
    );

    @Inject private UserFeedbackPersistence userFeedbackPersistence;

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FeedbackLoaderClient feedbackLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    /**
     * ***********************查询用户反馈*************************************************************
     */
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String feedbackIndex(Model model) {

        Date startDate = null;
        Date endDate = null;
        int userType = -1;
        Long userId = null;
        int feedbackState = -1;
        String content = null;
        Long feedbackId = null;
        int deliverState = -1;
        String contactState = null;
        String feedbackType = getRequestParameter("feedbackType", "");
        boolean tagFlag = getRequestBool("tagFlag");

        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");

        try {
            String startDateStr = getRequestParameter("startDate", "").trim();
            if (StringUtils.isNotBlank(startDateStr))
                startDate = sdf.parse(startDateStr);
            else
                startDate = new Date();

            String endDateStr = getRequestParameter("endDate", "").trim();
            if (StringUtils.isNotBlank(endDateStr))
                endDate = sdf.parse(endDateStr);

            userId = getRequestLong("userId", -1L);
            userType = getRequestInt("userType", -1);
            feedbackState = getRequestInt("feedbackState", 0);
            content = getRequestParameter("content", "").trim();
            feedbackId = getRequestLong("feedbackId", -1L);
            deliverState = getRequestInt("deliverState", -1);
            contactState = getRequestParameter("contactState", "").trim();

        } catch (Exception ignored) {
            //leave it alone
        }

        if (startDate != null) {
            model.addAttribute("startDate", sdf.format(startDate).toString());
        }

        if (endDate != null) {
            model.addAttribute("endDate", sdf.format(endDate).toString());
        }

        if (userId >= 0)
            model.addAttribute("userId", userId);
        model.addAttribute("userType", userType);
        model.addAttribute("feedbackState", feedbackState);
        model.addAttribute("content", content);
        if (feedbackId > 0)
            model.addAttribute("feedbackId", feedbackId);
        if (StringUtils.isNotBlank(feedbackType))
            model.addAttribute("feedbackType", feedbackType);

        List<UserFeedback> feedbackInfoList = getFeedbackInfoList(startDate, endDate, userType, userId, feedbackState, content, feedbackId, feedbackType, deliverState, contactState, tagFlag);
        List<Map<String, Object>> feedbackWatchers = feedbackLoaderClient.getFeedbackLoader().findWatchersWithTag();

        model.addAttribute("feedbackInfoList", feedbackInfoList);
        model.addAttribute("feedbackStateMap", crmUserService.feedbackStateMap);
        model.addAttribute("feedbackTypeList", feedbackTypeList);
        model.addAttribute("feedbackQuickReplyList", getFeedbackQuickReplyList());
        model.addAttribute("today", new Date());
        model.addAttribute("feedbackWatchers", feedbackWatchers);
        model.addAttribute("urlPrefix", ProductConfig.getMainSiteBaseUrl());
        return "crm/feedback/realtimefeedbackindex";
    }

//    /**************************查询用户反馈**************************************************************/
//    @RequestMapping(value = "handle.vpage", method = [RequestMethod.GET, RequestMethod.POST])
//    String feedbackHandle(Model model) {
//
//        Date startDate
//        Date endDate
//        int userType = -1
//        Long userId = null
//        int feedbackState = -1
//        String content = null
//        Long feedbackId = null
//        int deliverState = -1
//        String feedbackType = getRequestParameter("feedbackType", "")
//
//        def sdf = new SimpleDateFormat("yyyy-MM-dd")
//
//        try {
//            def startDateStr = getRequestParameter("startDate", "").trim()
//            if (startDateStr != '')
//                startDate = sdf.parse(startDateStr)
//            else
//                startDate = new Date()
//
//            def endDateStr = getRequestParameter("endDate", "").trim()
//            if (endDateStr != '')
//                endDate = sdf.parse(endDateStr)
//
//            userId = getRequestLong("userId", -1L)
//            userType = getRequestInt("userType", -1)
//            feedbackState = getRequestInt("feedbackState", 1)
//            content = getRequestParameter("content", "").trim()
//            feedbackId = getRequestLong("feedbackId", -1L)
//            deliverState = getRequestInt("deliverState",-1)
//        } catch (Exception ignored) {
//            //leave it alone
//        }
//
//        if (startDate) {
//            model.addAttribute("startDate", sdf.format(startDate).toString())
//        }
//
//        if (endDate) {
//            model.addAttribute("endDate", sdf.format(endDate).toString())
//        }
//
//        if (userId >= 0)
//            model.addAttribute("userId", userId)
//        model.addAttribute("userType", userType)
//        model.addAttribute("feedbackState", feedbackState)
//        model.addAttribute("content", content)
//        if (feedbackId > 0)
//            model.addAttribute("feedbackId", feedbackId)
//        if (feedbackType != '')
//            model.addAttribute("feedbackType", feedbackType)
//
//        def feedbackInfoList = getFeedbackInfoList(startDate, endDate, userType, userId, feedbackState, content, feedbackId, feedbackType,deliverState,getCurrentAdminUser().getAdminUserName())
//        model.addAttribute("feedbackInfoList", feedbackInfoList)
//        model.addAttribute("feedbackStateMap", crmUserService.feedbackStateMap)
//        model.addAttribute("feedbackTypeList", feedbackTypeList)
//        model.addAttribute("feedbackQuickReplyList", getFeedbackQuickReplyList())
//        model.addAttribute("today",new Date())
//        return "crm/feedback/realtimefeedbackhandle"
//    }

    /**
     * ***********************编辑、删除用户反馈********************************************************
     */
    @RequestMapping(value = "editstate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editState(@RequestParam Long feedbackId, @RequestParam Integer state) {

        UserFeedback ufb = userFeedbackPersistence.load(feedbackId);
        String adminName = getCurrentAdminUser().getAdminUserName();
//        if(state == 0 && ufb.getConfirmUser() != null && !ufb.getConfirmUser().equals("")){
//            if(adminName.equals(ufb.getConfirmUser())){
//                return MapMessage.errorMessage("您已经领取了这条反馈，请到待处理列表中处理")
//            }
//            return MapMessage.errorMessage("这条反馈已被他人领取")
//        }

        Date current = new Date();
        Update update = new Update();
        if (state < 3) {
            update = update.set("STATE", ++state);
        }
        switch (state) {
            case 1:
                if (!StringUtils.isEmpty(ufb.getConfirmUser()) && !ufb.getConfirmUser().equals(adminName)) {
                    return MapMessage.errorMessage("更新" + feedbackId + "失败,这个feedback已被其他人处理");
                }
                update = update.set("CONFIRM_DATETIME", current)
                        .set("CONFIRM_USER", adminName);
                break;
            case 3:
                if (!StringUtils.isEmpty(ufb.getCloseUser()) && !ufb.getCloseUser().equals(adminName)) {
                    return MapMessage.errorMessage("更新" + feedbackId + "失败,这个feedback已被其他人处理");
                }
                update = update.set("CLOSE_DATETIME", current)
                        .set("CLOSE_USER", adminName);
                break;
            default:
                break;
        }
        Criteria criteria = Criteria.where("ID").is(feedbackId);
        userFeedbackPersistence.executeUpdate(update, criteria);
        return MapMessage.successMessage("更新" + feedbackId + "成功").add("state", state);
    }

    @RequestMapping(value = "replyfeedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage replyFeedback(@RequestParam String feedbackMapJson, @RequestParam String reply) {

        reply = reply.trim();
        if (StringUtils.isBlank(reply))
            return MapMessage.errorMessage("回复内容不能为空");

        ObjectMapper objectMapper = new ObjectMapper();
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Map.class, new Class[]{Long.class, Long.class});
        Map<Long, Long> feedbackMap;
        try {
            feedbackMap = objectMapper.readValue(feedbackMapJson, javaType);
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }

        List<Long> failureUserIds = new ArrayList<>();
        List<Long> successUserIds = new ArrayList<>();
        for (Long feedbackId : feedbackMap.keySet()) {
            Long userId = feedbackMap.get(feedbackId);
            User receiver = userLoaderClient.loadUser(userId);
            if (receiver != null) {
                //发送用户反馈的回复
                if (receiver.isTeacher()) {
                    teacherLoaderClient.sendTeacherMessage(receiver.getId(), reply);
                } else {
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage(receiver.getId(), reply);
                }

                //更新用户反馈
                Update update = Update.update("REPLY", reply);
                Criteria criteria = Criteria.where("ID").is(feedbackId);
                userFeedbackPersistence.executeUpdate(update, criteria);

                successUserIds.add(userId);
            } else {
                failureUserIds.add(userId);
            }

        }

        MapMessage mapMessage;
        if (!CollectionUtils.isEmpty(failureUserIds)) {
            mapMessage = MapMessage.errorMessage("回复用户" + failureUserIds.toString() + "失败");
        } else {
            mapMessage = MapMessage.successMessage("回复用户成功");
        }

        return mapMessage.add("reply", reply).add("successUserIds", successUserIds);
    }

    @RequestMapping(value = "deletefeedback.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteFeedback(@RequestParam Long feedbackId) {
        Update update = Update.update("DISABLED", true)
                .set("DELETE_DATETIME", new Date())
                .set("DELETE_USER", getCurrentAdminUser().getAdminUserName());
        Criteria criteria = Criteria.where("ID").is(feedbackId);
        userFeedbackPersistence.executeUpdate(update, criteria);
        return MapMessage.successMessage("删除成功");
    }

    /**
     * 将反馈内容通过邮件发送给指定用户
     */
    @Deprecated
    @RequestMapping(value = "sendfeedbackbyemail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFeedbackByEmail(@RequestParam(value = "textContent", required = false) String textContent) {

        // 邮件内容
        String[] ids = getRequest().getParameterValues("ids[]");
        List<Long> idList = StringUtils.toLongList(StringUtils.join(ids, ","));
        List<String> addressList = new ArrayList<>();
        if (idList.size() == 0)
            return MapMessage.errorMessage("发送邮件失败");
        Map<Long, UserFeedback> userFeedbackMap = userFeedbackPersistence.loads(idList);
        List<UserFeedback> feedbackList = new LinkedList<>(userFeedbackMap.values());
        String subject;
        if (feedbackList.size() == 0)
            return MapMessage.errorMessage("发送邮件失败");
        else if (feedbackList.size() == 1)
            subject = "【网站反馈-" + feedbackList.get(0).getFeedbackType() + "-" + feedbackList.get(0).getFeedbackSubType1() + "】";
        else
            subject = "【网站反馈】";

        addressList.add("yizhou.zhang@17zuoye.com");
        addressList.add("zhilong.hu@17zuoye.com");
        addressList.add("zhi.wang@17zuoye.com");
        List<String> cc = Arrays.asList("yizhou.zhang@17zuoye.com", "zhi.wang@17zuoye.com", "zhilong.hu@17zuoye.com");
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy年MM月dd日");
        List<UserFeedbackForEmail> feedbacks = transform(feedbackList);
        Map<String, Object> content = new HashMap<>();
        content.put("feedbackList", feedbacks);
        content.put("date", sdf.format(new Date()));
        emailServiceClient.createTemplateEmail(EmailTemplate.feedbackInform)
                .to(StringUtils.join(addressList, ";"))
                .cc(StringUtils.join(cc, ";"))
                .subject(subject)
                .content(content)
                .send();

        String operatorName = "";
        for (String address : addressList) {
//        addressList.each { String address ->
            String delegatorName = address.replaceAll("^([^@]*)@.+", "$1");
            delegatorName = operatorName.length() == 0 ? delegatorName : ("," + delegatorName);
            operatorName += delegatorName;
        }

        // 更新用户反馈状态
        Integer newFeedbackState = 1;
        Update update = Update.update("STATE", newFeedbackState)
                .set("OPERATOR", operatorName)
                .set("DELEGATOR", getCurrentAdminUser().getAdminUserName())
                .set("DELIVER_REASON", textContent);
        Criteria criteria = Criteria.where("ID").in(idList);
        userFeedbackPersistence.executeUpdate(update, criteria);
        return MapMessage.successMessage("发送邮件成功").add("state", newFeedbackState);
    }

    @RequestMapping(value = "setcontactuser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setContactUser(@RequestParam(value = "feedbackId") Long feedbackId) {
        if (feedbackId == null) {
            return MapMessage.errorMessage();
        }
        String operator = getCurrentAdminUser().getAdminUserName();
        UserFeedback feedback = userFeedbackPersistence.load(feedbackId);
        if (feedback == null) {
            return MapMessage.errorMessage();
        }
        if (feedback.getState() != null && feedback.getState() < 2) {
            return MapMessage.errorMessage();
        }
        Update update = Update.update("CONTACT_DATETIME", new Date())
                .set("CONTACT_USER", operator)
                .set("CONTACT_STATE", "CONTACTED");
        Criteria criteria = Criteria.where("ID").is(feedbackId);
        userFeedbackPersistence.executeUpdate(update, criteria);
        logger.info("管理员{}已经更新了反馈{}状态为CONTACTED", operator, feedbackId);
        return MapMessage.successMessage();
    }

    /**
     * 给用户反馈增加评论
     */
    @RequestMapping(value = "addfeedbackcomment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addFeedbackComment() {
        String comment = getRequestParameter("comment", "");
        String contactState = getRequestParameter("contactState", "");
        String[] feedbackIds = getRequest().getParameterValues("feedbackIdList[]");
        List<Long> feedbackIdList = StringUtils.toLongList(StringUtils.join(feedbackIds, ","));
        int newFeedbackState = 2;
        Date current = new Date();
        if (feedbackIdList.size() > 0) {
            Update update = Update.update("STATE", newFeedbackState)
                    .set("COMMENT", comment)
                    .set("CONTACT_DATETIME", current)
                    .set("CONTACT_USER", getCurrentAdminUser().getAdminUserName())
                    .set("CONTACT_STATE", contactState)
                    .set("RESOLVE_DATETIME", current)
                    .set("RESOLVE_USER", getCurrentAdminUser().getAdminUserName());
            Criteria criteria = Criteria.where("ID").in(feedbackIdList);
            userFeedbackPersistence.executeUpdate(update, criteria);
            return MapMessage.successMessage("操作成功").add("state", newFeedbackState);
        } else {
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * *********************private method*****************************************************************
     */
    private List<UserFeedback> getFeedbackInfoList(Date startDate, Date endDate,
                                                   int userType, Long userId,
                                                   int feedbackState,
                                                   String content,
                                                   Long feedbackId,
                                                   String feedbackType,
                                                   int deliverState,
                                                   String contactState,
                                                   boolean tagFlag) {
        List<Criteria> list = new LinkedList<>();
        if (feedbackId > 0) {
            list.add(Criteria.where("ID").is(feedbackId));
        } else {
            if (startDate != null) {
                startDate = DayRange.newInstance(startDate.getTime()).getStartDate();
                list.add(Criteria.where("CREATE_DATETIME").gte(startDate));
            }
            if (endDate != null) {
                endDate = DayRange.newInstance(endDate.getTime()).getEndDate();
                list.add(Criteria.where("CREATE_DATETIME").lte(endDate));
            }
            if (userId >= 0) {
                list.add(Criteria.where("USER_ID").is(userId));
            }
            if (feedbackState >= 0) {
                list.add(Criteria.where("STATE").is(feedbackState));
            }
            if (deliverState == 0) {
                list.add(Criteria.where("DELIVER_REASON").is(""));
            }
            if (deliverState == 1) {
                list.add(Criteria.where("DELIVER_REASON").ne(""));
            }
            if (StringUtils.isNotBlank(contactState)) {
                list.add(Criteria.where("CONTACT_STATE").is(contactState));
            }
            if (StringUtils.isNotBlank(content)) {
                list.add(Criteria.where("CONTENT").like("%" + content + "%"));
            }
            if (userType >= 0) {
                list.add(Criteria.where("USER_TYPE").is(userType));
            }
            if (StringUtils.isNotBlank(feedbackType)) {
                list.add(Criteria.where("FEEDBACK_TYPE").is(feedbackType));
            } else {
                list.add(Criteria.where("FEEDBACK_TYPE").ne("重置密码"));
            }
            if (!tagFlag) {
                list.add(Criteria.where("TAG_ID").is(0L));
                list.add(Criteria.or(
                        Criteria.where("TAG").is(""),
                        Criteria.where("TAG").notExists()
                ));
            }
        }
        list.add(Criteria.where("DISABLED").is(false));
        list.add(Criteria.or(
                Criteria.where("CONTACT_PHONE").ne(""),
                Criteria.where("CONTACT_QQ").ne(""),
                Criteria.where("USER_TYPE").is(1)
        ));

        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        List<UserFeedback> feedbackInfoList = userFeedbackPersistence.query(Query.query(criteria).with(sort));
        for (UserFeedback userFeedback : feedbackInfoList) {
            if (StringUtils.isNotEmpty(userFeedback.getTag())) {
                UserFeedbackTag userFeedbackTag = feedbackLoaderClient.getFeedbackLoader().loadUserFeedbackTag(userFeedback.getTagId());
                if (userFeedbackTag != null)
                    userFeedback.setWatcher(userFeedbackTag.getWatcherName());
            }
            User user = userLoaderClient.loadUser(userFeedback.getUserId());
            if (user != null) {
                userFeedback.setFeedbackUser(user);
            }
        }

        return feedbackInfoList;
    }

    @RequestMapping(value = "processcheck.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage processcheck(Model model) {
        FastDateFormat sdf = FastDateFormat.getInstance(DateUtils.FORMAT_SQL_DATETIME);
        Date startDate = null;
        try {
            //10min befor
            startDate = sdf.parse(sdf.format(new Date(new Date().getTime() - 10 * 60000)));
        } catch (Exception ex) {
        }
        List<UserFeedback> userFeedbacks = getFeedbackInfoList(0, startDate, null);

        MapMessage message = new MapMessage();
        if (userFeedbacks.size() > 0) {
            message.setSuccess(true);
            message.setInfo(String.valueOf(userFeedbacks.size()));
        } else {
            message.setSuccess(false);
        }
        return message;
    }

    private List<String> getFeedbackQuickReplyList() {
        String query = "select ad.DESCRIPTION from ADMIN_DICT ad " +
                " where ad.GROUP_NAME = '反馈快速回复' and ad.DISABLED = 0 ";
        return utopiaSqlAdmin.withSql(query).queryColumnValues(String.class);
    }

    private List<UserFeedback> getFeedbackInfoList(int feedbackState, Date startDate, Date endDate) {
        List<Criteria> list = new LinkedList<>();
        if (startDate != null) {
            startDate = DayRange.newInstance(startDate.getTime()).getStartDate();
            list.add(Criteria.where("CREATE_DATETIME").gte(startDate));
        }
        if (endDate != null) {
            endDate = DayRange.newInstance(endDate.getTime()).getEndDate();
            list.add(Criteria.where("CREATE_DATETIME").lte(endDate));
        }

        if (feedbackState == -1) {
            list.add(Criteria.where("STATE").ne(0));
        } else {
            list.add(Criteria.where("STATE").is(feedbackState));
        }

        list.add(Criteria.where("DISABLED").is(false));
        list.add(Criteria.or(
                Criteria.where("CONTACT_PHONE").ne(""),
                Criteria.where("CONTACT_QQ").ne(""),
                Criteria.where("USER_TYPE").is(1)
        ));

        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return userFeedbackPersistence.query(Query.query(criteria).with(sort));
    }
}
