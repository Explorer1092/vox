/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.dao.feedback.UserFeedbackPersistence;
import com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext;
import com.voxlearning.utopia.admin.service.crm.CrmUserService;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.api.constants.UserFeedbackForEmail;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;
import com.voxlearning.utopia.service.feedback.client.FeedbackLoaderClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author Longlong Yu
 * @since 上午12:48,13-8-30.
 */
@Controller
@RequestMapping("/crm/feedback")
public class CrmFeedbackController extends CrmAbstractController {

    private static final List<String> feedbackTypeList;

    static {
        List<String> lst = new ArrayList<>();
        lst.add("账号与班级");
        lst.add("数学基础作业");
        lst.add("数学同步试题");
        lst.add("PK问题");
        lst.add("通天塔问题");
        lst.add("走遍美国问题");
        lst.add("趣味数学问题");
        lst.add("爱儿优问题");
        lst.add("进击的三国问题");
        lst.add("沃克大冒险问题");
        lst.add("宠物大乱斗问题");
        lst.add("洛亚传说问题");
        lst.add("英语基础作业");
        lst.add("学生移动端");
        lst.add("中学移动问题");
        lst.add("其他问题");
        lst.add("改进建议");
        lst.add("银币问题");
        lst.add("课外练习");
        lst.add("大爆料问题");
        lst.add("奖品中心问题");
        lst.add("英语同步试题");
        lst.add("英语阅读应用");
        lst.add("重置密码");
        lst.add("微信技术支持公众号");
        lst.add("移动版问题");
        lst.add("缺失教材反馈");
        lst.add("Goal");
        lst.add("成长世界反馈");
        lst.add("移动家长通问题");
        lst.add("一起学KOL反馈问题");
        lst.add("佩叔学英语");
        feedbackTypeList = Collections.unmodifiableList(lst);
    }

    private static final String REPLY_SPLIT = "@!=@";

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserFeedbackPersistence userFeedbackPersistence;

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FeedbackLoaderClient feedbackLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    /**
     * ***********************查询用户反馈*************************************************************
     */
    @RequestMapping(value = "feedbackindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String feedbackIndex(Model model) {

        // FIXME COMMENT BY ZHAO REX for startDate, endDate may not have been initialized.
        Date startDate = null;
        Date endDate = null;
        int userType = -1;
        Long userId = null;
        int feedbackState = -1;
        String content = null;
        Long feedbackId = null;
        int deliverState = -1;
        String tag;
        String watcher;
        String feedbackType = getRequestParameter("feedbackType", "");
        String feedbackSubType1 = getRequestParameter("feedbackSubType1", "");
        List<String> tags = new ArrayList<>();

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
            tag = getRequestParameter("tag", "");
            watcher = getRequestParameter("watcher", "");
            if (!watcher.equals("") && (tag.equals("-1") || tag.equals(""))) {
                List<UserFeedbackTag> userFeedbackTags = feedbackLoaderClient.getFeedbackLoader().findByWatcherName(watcher);
                for (UserFeedbackTag userFeedbackTag : userFeedbackTags) {
                    tags.add(userFeedbackTag.getName());
                }
            } else if (!tag.equals("")) {
                tags.add(tag);
            }
        } catch (Exception ignored) {
            //leave it alone
        }

        // FIXME COMMENT BY ZHAO REX for startDate may not have been initialized.
        if (startDate != null) {
            model.addAttribute("startDate", sdf.format(startDate));
        }

        // FIXME COMMENT BY ZHAO REX for endDate may not have been initialized.
        if (endDate != null) {
            model.addAttribute("endDate", sdf.format(endDate));
        }

        if (userId != null && userId >= 0)
            model.addAttribute("userId", userId);
        model.addAttribute("userType", userType);
        model.addAttribute("feedbackState", feedbackState);
        model.addAttribute("content", content);
        if (feedbackId != null && feedbackId > 0)
            model.addAttribute("feedbackId", feedbackId);
        if (StringUtils.isNotBlank(feedbackType))
            model.addAttribute("feedbackType", feedbackType);
        if (StringUtils.isNotBlank(feedbackSubType1)) {
            model.addAttribute("feedbackSubType1", feedbackSubType1);
        }

        // todo: 这个方法要优化下，改到现在，有点丑了
        List<UserFeedback> feedbackInfoList = getFeedbackInfoList(startDate, endDate, userType, userId, feedbackState, content, feedbackId, feedbackType, deliverState, tags, feedbackSubType1);
        List<Map<String, Object>> feedbackWatchers = feedbackLoaderClient.getFeedbackLoader().findWatchersWithTag();
        model.addAttribute("feedbackInfoList", feedbackInfoList);
        model.addAttribute("feedbackStateMap", CrmUserService.feedbackStateMap);
        model.addAttribute("feedbackTypeList", feedbackTypeList);
        model.addAttribute("feedbackQuickReplyList", getFeedbackQuickReplyList());
        model.addAttribute("today", new Date());
        model.addAttribute("feedbackWatchers", feedbackWatchers);
        model.addAttribute("urlPrefix", ProductConfig.getMainSiteBaseUrl());

        return "crm/feedback/feedbackindex";
    }

    /**
     * ***********************导出用户反馈Excel********************************************************
     */
    @RequestMapping(value = "downloadexcel.vpage", method = RequestMethod.POST)
    public void downloadExcel(HttpServletResponse response) {
        Date startDate = null;
        Date endDate = null;
        int userType = -1;
        Long userId = null;
        int feedbackState = -1;
        String content = null;
        Long feedbackId = null;
        int deliverState = -1;
        String feedbackType = getRequestParameter("feedbackType", "");
        String feedbackSubType1 = getRequestParameter("feedbackSubType1", "");
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");

        try {
            String startDateStr = getRequestParameter("startDate", "").trim();
            if (StringUtils.isNotBlank(startDateStr))
                startDate = sdf.parse(startDateStr);

            String endDateStr = getRequestParameter("endDate", "").trim();
            if (StringUtils.isNotBlank(endDateStr))
                endDate = sdf.parse(endDateStr);

            userId = getRequestLong("userId", -1);
            userType = getRequestInt("userType", -1);
            feedbackState = getRequestInt("feedbackState");
            content = getRequestParameter("content", "").trim();
            feedbackId = getRequestLong("feedbackId", -1L);
            deliverState = getRequestInt("deliverState", -1);
        } catch (Exception ignored) {
            //leave it alone
        }

        List<UserFeedback> feedbackInfoList = getFeedbackInfoList(startDate, endDate, userType, userId, feedbackState, content, feedbackId, feedbackType, deliverState, null, feedbackSubType1);
        HSSFWorkbook hssfWorkbook = convertToHSSfWorkbook(feedbackInfoList);

        AdminHttpRequestContext context = (AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            context.downloadFile("feedback.xls", "application/vnd.ms-excel", outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                throw new RuntimeException("output error.", e);
            }
        }
    }

    /**
     * ***********************编辑、删除用户反馈********************************************************
     */
    @RequestMapping(value = "editstate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editState(@RequestParam Long feedbackId, @RequestParam Integer state) {
        UserFeedback ufb = userFeedbackPersistence.load(feedbackId);
        String adminName = getCurrentAdminUser().getAdminUserName();
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
                        .set("CONFIRM_USER", getCurrentAdminUser().getAdminUserName());
                break;
            case 3:
                if (!StringUtils.isEmpty(ufb.getCloseUser()) && !ufb.getCloseUser().equals(adminName)) {
                    return MapMessage.errorMessage("更新" + feedbackId + "失败,这个feedback已被其他人处理");
                }
                update = update.set("CLOSE_DATETIME", current)
                        .set("CLOSE_USER", getCurrentAdminUser().getAdminUserName());
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
    public MapMessage replyFeedback(@RequestParam String feedbackMapJson, @RequestParam String reply) throws IOException {

        reply = reply.trim();
        if (StringUtils.isEmpty(reply))
            return MapMessage.errorMessage("回复内容不能为空");

        ObjectMapper objectMapper = JsonObjectMapper.OBJECT_MAPPER;
        MapType mt = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, Long.class, Long.class);
        Map<Long, Long> feedbackMap = objectMapper.readValue(feedbackMapJson, mt);

        List<Long> failureUserIds = new ArrayList<>();
        List<Long> successUserIds = new ArrayList<>();

        Set<Long> feedbackIds = feedbackMap.keySet();
        Set<Long> successTeacherId = new HashSet<>();
        for (Long feedbackId : feedbackIds) {
            Long userId = feedbackMap.get(feedbackId);
            User receiver = userLoaderClient.loadUser(userId);
            if (receiver != null) {
                //发送用户反馈的回复
                if (receiver.isTeacher()) {
                    teacherLoaderClient.sendTeacherMessage(receiver.getId(), reply);
                } else {
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage(receiver.getId(), reply);
                }
                //发弹窗
                savePopup(reply, userId);

                //更新用户反馈
                UserFeedback userFeedback = userFeedbackPersistence.load(feedbackId);
                if (userFeedback != null) {
                    userFeedback.setReply(mergeReply(userFeedback.getReply(), reply));
                    Update update = Update.update("REPLY", mergeReply(userFeedback.getReply(), reply));
                    Criteria criteria = Criteria.where("ID").is(feedbackId);
                    userFeedbackPersistence.executeUpdate(update, criteria);
                    //加入admin log
                    addAdminLog("replyFeedback", userId, null, "crm", "ReceiverID:" + userId + ", reply:" + reply);
                }
                if (receiver.getUserType().equals(UserType.TEACHER.getType()))
                    successTeacherId.add(userId);
                successUserIds.add(userId);
            } else {
                failureUserIds.add(userId);
            }
        }
        if (!CollectionUtils.isEmpty(successTeacherId)) {
            sendTeacherAppMessageForCrmReply(successTeacherId, reply);
        }

        MapMessage mapMessage;
        if (!CollectionUtils.isEmpty(failureUserIds)) {
            mapMessage = MapMessage.errorMessage("回复用户" + failureUserIds + "失败");
        } else {
            mapMessage = MapMessage.successMessage("回复用户成功");
        }

        return mapMessage.add("reply", reply).add("successUserIds", successUserIds);
    }

    private void sendTeacherAppMessageForCrmReply(Set<Long> successTeacherId, String reply) {
        List<AppMessage> messageList = new ArrayList<>();

        Map<String, Object> extroMap = new LinkedHashMap<>();
        Map<String, Object> h5ParamMap = new LinkedHashMap<>();
        extroMap.put("h5_param", h5ParamMap);
        h5ParamMap.put("ct", formatDate(new Date(), "yyyy/MM/dd HH:mm"));
        h5ParamMap.put("content", reply);

        String subReply = StringUtils.substring(reply, 0, 30);
        if (subReply.length() >= 30)
            subReply = subReply + "...";
        final String a = subReply;
        successTeacherId.forEach(p -> {
            AppMessage appMessage = new AppMessage();
            appMessage.setUserId(p);

            Long mainId = teacherLoaderClient.loadMainTeacherId(p);
            if (mainId != null && mainId > 0L) {
                appMessage.setUserId(mainId);
            }

            appMessage.setMessageType(TeacherMessageType.CRMREPLY.getType());
            appMessage.setLinkUrl(TeacherMessageType.getFeedbackUrlTemplate());
            appMessage.setContent(a);
            appMessage.setTitle(TeacherMessageType.CRMREPLY.getDescription());
            appMessage.setCreateTime(new Date().getTime());
            appMessage.setExtInfo(extroMap);
            messageList.add(appMessage);
        });
        messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);

        List<Long> userIdList = new ArrayList<>();
        userIdList.addAll(successTeacherId);
        String key = "m";//目前只有中学老师,没有管小学老师.此处可能有坑,如果需要解决,需要分辨这些用户的中小学身份发两次.
        Map<String, Object> extroInfo = MiscUtils.m("s", TeacherMessageType.CRMREPLY.getType(), "key", key, "t", "msg_list");
        appMessageServiceClient.sendAppJpushMessageByIds(subReply, AppMessageSource.JUNIOR_TEACHER, userIdList, extroInfo);

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

    @RequestMapping(value = "batchstate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchState(@RequestParam Integer state) {
        String[] ids = getRequest().getParameterValues("ids[]");
        List<Long> idList = StringUtils.toLongList(StringUtils.join(ids, ","));
        if (idList.isEmpty()) {
            return MapMessage.errorMessage();
        }
        Date current = new Date();
        Update update = Update.update("STATE", state);
        switch (state) {
            case 1:
                update = update.set("CONFIRM_DATETIME", current).set("CONFIRM_USER", getCurrentAdminUser().getAdminUserName());
                break;
            case 3:
                update = update.set("CLOSE_DATETIME", current).set("CLOSE_USER", getCurrentAdminUser().getAdminUserName());
                break;
            default:
                break;
        }
        Criteria criteria = Criteria.where("ID").in(idList);
        userFeedbackPersistence.executeUpdate(update, criteria);

        return MapMessage.successMessage("更新状态成功");
    }

    /**
     * 批量改变用户反馈类型
     */
    /*
    @RequestMapping(value = "batchtagfeedbacktype.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchTagFeedbackType(@RequestParam(value = "ids[]") List<Long> ids, @RequestParam String tagFeedbackType) {

        ids.each { Long it ->
            def userFeedback = userFeedbackPersistence.get(it)
            if (userFeedback) {
                def tagMap = JsonUtils.fromJson(userFeedback.tag)
                tagMap.feedback = tagFeedbackType
                userFeedback.tag = JsonUtils.toJson(tagMap)
                try {
                    userFeedbackPersistence.update(it, userFeedback)
                } catch (Exception ignored) {
                    return MapMessage.errorMessage("更新至feedbackId:${it}失败")
                }
            }
        }

        return MapMessage.successMessage("更新状态成功")
    }
*/
    @RequestMapping(value = "batchdelete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchDelete() {
        String[] ids = getRequest().getParameterValues("ids[]");
        List<Long> idList = StringUtils.toLongList(StringUtils.join(ids, ","));
        if (!idList.isEmpty()) {
            Update update = Update.update("DISABLED", true);
            Criteria criteria = Criteria.where("ID").in(idList);
            userFeedbackPersistence.executeUpdate(update, criteria);
        }
        return MapMessage.successMessage("删除成功");
    }

    /**
     * 将反馈内容通过邮件发送给指定用户
     */
    @Deprecated
    @RequestMapping(value = "sendfeedbackbyemail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFeedbackByEmail(@RequestParam(value = "textContent", required = false) String textContent) {

        String[] ids = getRequest().getParameterValues("ids[]");
        List<Long> idList = StringUtils.toLongList(StringUtils.join(ids, ","));
        // 邮件内容
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
                .to(StringUtils.join(addressList, ';'))
                .cc(StringUtils.join(cc, ';'))
                .subject(subject)
                .content(content)
                .send();

        String operatorName = "";
        for (String address : addressList) {
//        addressList.each { String address ->
            String delegatorName = address.replaceAll("^([^@]+)@.+", "$1");
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

    /**
     * 给用户反馈增加评论
     */
    @RequestMapping(value = "addfeedbackcomment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addFeedbackComment() {

        String comment = getRequestParameter("comment", "");
        String[] ids = getRequest().getParameterValues("feedbackIdList[]");
        List<Long> feedbackIdList = StringUtils.toLongList(StringUtils.join(ids, ","));
        int newFeedbackState = 2;
        if (feedbackIdList.size() > 0) {
            Update update = Update.update("STATE", newFeedbackState)
                    .set("COMMENT", comment)
                    .set("RESOLVE_DATETIME", new Date())
                    .set("RESOLVE_USER", getCurrentAdminUser().getAdminUserName());
            Criteria criteria = Criteria.where("ID").in(feedbackIdList);
            userFeedbackPersistence.executeUpdate(update, criteria);
            return MapMessage.successMessage("操作成功").add("state", newFeedbackState);
        } else {
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * loadTags
     */
    @RequestMapping(value = "loadtags.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadTags(@RequestParam(value = "watcher") String watcher) {

        try {
            List<UserFeedbackTag> tags = feedbackLoaderClient.getFeedbackLoader().findByWatcherName(watcher);
            return MapMessage.successMessage("操作成功").add("tags", tags);
        } catch (Exception ignored) {
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * addTag
     */
    @RequestMapping(value = "addtagtofeedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTag(@RequestParam(value = "feedbackId") Long feedbackId,
                             @RequestParam(value = "tagId") Long tagId) {

        try {
            String tagMessage = getRequestParameter("tagMessage", "");
            UserFeedback feedback = userFeedbackPersistence.load(feedbackId);
            if (feedback != null) {
                UserFeedbackTag tag = feedbackLoaderClient.getFeedbackLoader().loadUserFeedbackTag(tagId);
                feedback.setTagId(tagId);
                feedback.setTag(tag.getName());

                Update update = Update.update("TAG_ID", tagId).set("TAG", tag.getName());
                Criteria criteria = Criteria.where("ID").is(feedbackId);
                userFeedbackPersistence.executeUpdate(update, criteria);

                if (tag.getRedmineUserId() > 0 && tag.getPriority() >= UserFeedbackTag.PRIORITY_LEVEL_LOW) {
                    String content = feedback.getContent();
                    if (content != null && content.length() > 20) {
                        content = content.substring(0, 20);
                    }
                    String subject = content;
                    if (feedback.getFeedbackSubType1() != null) {
                        subject = feedback.getFeedbackSubType1() + "-" + subject;
                    }
                    String description = feedback.getContent() + "\n" + feedback.getRealName() + ":";
                    User user = userLoaderClient.loadUser(feedback.getUserId());
                    UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                    if (user.isStudent()) {
                        description += "\"" + feedback.getUserId() + "\":http://admin.17zuoye.net/crm/student/studenthomepage.vpage?studentId=" + feedback.getUserId() + "\n";
                        description += "绑定手机：" + ua.getSensitiveMobile() + "\n";
                    } else if (user.isTeacher()) {
                        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                                .loadTeacherSchool(user.getId())
                                .getUninterruptibly();
                        description += "\"" + feedback.getUserId() + "\":http://admin.17zuoye.net/crm/teacher/teacherhomepage.vpage?teacherId=" + feedback.getUserId() + "\n";
                        description += "手机：" + ua.getSensitiveMobile() + " 学校:" + school.getCname() + "\n";
                    }
                    description += "\"反馈历史\":http://admin.17zuoye.net/crm/student/userfeedback.vpage?userId=" + feedback.getUserId() + "\n";
                    description += "feedbackId:" + feedback.getId();
                    if (StringUtils.isNotEmpty(tagMessage)) {
                        description += "\n" + tagMessage;
                    }
                    Map<String, Object> issueMap = new HashMap<>();
                    Map<String, Object> param = new HashMap<>();
                    param.put("project_id", 1);   //17zuoye web
                    param.put("subject", subject);
                    param.put("description", description);
                    param.put("assigned_to_id", tag.getRedmineUserId());
                    //redmine 规范   1:低 2:普通 3:高 4:紧急 5:立刻。请按照redmine的标准来配置tag.priority
                    param.put("priority_id", tag.getPriority());
                    param.put("tracker_id", 5);   //redmine type 5:feedback
                    issueMap.put("issue", param);

                    URI uri = new URIBuilder().setCharset(ICharset.defaultCharset())
                            .setScheme("http")
                            .setHost("project.17zuoye.net")
                            .setPath("/redmine/issues.json")
                            .setParameter("key", getCurrentAdminUser().getRedmineApikey())
                            .build();
                    int statusCode = HttpRequestExecutor.defaultInstance().post(uri)
                            .json(issueMap)
                            .execute().getStatusCode();

                    if (statusCode != 201) {
                        logger.warn("创建redmine操作失败, response.statusCode:{}", statusCode);
                        return MapMessage.errorMessage("创建redmine操作失败");
                    }
                }
                return MapMessage.successMessage("操作成功").add("tags", tag);
            } else {
                return MapMessage.errorMessage("操作失败");
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * batchAddTag
     */
    @RequestMapping(value = "batchaddtag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchAddTag(@RequestParam(value = "tagId") Long tagId) {
        try {
            String[] ids = getRequest().getParameterValues("ids[]");
            List<Long> idList = StringUtils.toLongList(StringUtils.join(ids, ","));
            List<UserFeedback> feedbackList = new ArrayList<>();
            UserFeedbackTag tag = feedbackLoaderClient.getFeedbackLoader().loadUserFeedbackTag(tagId);

            if (!idList.isEmpty()) {
                Update update = Update.update("TAG", tag.getName()).set("TAG_ID", tagId);
                Criteria criteria = Criteria.where("ID").in(idList);
                userFeedbackPersistence.executeUpdate(update, criteria);
            }
            if (StringUtils.isNotEmpty(tag.getWatcherName()) && tag.getPriority() >= UserFeedbackTag.PRIORITY_LEVEL_HIGH) {//发邮件
                String subject = "【网站反馈】";
                List<String> addressList = new ArrayList<>();
                addressList.add(tag.getWatcherName() + "@17zuoye.com");
                FastDateFormat sdf = FastDateFormat.getInstance("yyyy年MM月dd日");
                List<String> cc = Collections.singletonList(tag.getWatcherName() + "@17zuoye.com");
                Map<String, Object> content = new HashMap<>();
                content.put("feedbackList", transform(feedbackList));
                content.put("date", sdf.format(new Date()));
                emailServiceClient.createTemplateEmail(EmailTemplate.feedbackInform)
                        .to(StringUtils.join(addressList, ";"))
                        .cc(StringUtils.join(cc, ";"))
                        .subject(subject)
                        .content(content)
                        .send();
            }
            return MapMessage.successMessage().add("tags", tag);
        } catch (Exception ignored) {
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
                                                   List<String> tags, String feedbackSubType1) {

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
            if (!Objects.equals(content, "")) {
                list.add(Criteria.where("CONTENT").like("%" + content + "%"));
            }
            if (tags != null && tags.size() > 0) {
                list.add(Criteria.where("TAG").in(tags));
            }
            if (userType >= 0) {
                list.add(Criteria.where("USER_TYPE").is(userType));
            }
            if (StringUtils.isNotBlank(feedbackType)) {
                list.add(Criteria.where("FEEDBACK_TYPE").is(feedbackType));
            } else {
                list.add(Criteria.where("FEEDBACK_TYPE").ne("重置密码"));
            }

            if (StringUtils.isNotBlank(feedbackSubType1)) {
                list.add(Criteria.where("FEEDBACK_SUB_TYPE_1").like("%" + feedbackSubType1 + "%"));
            }
        }
        list.add(Criteria.where("DISABLED").is(false));
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

    private HSSFWorkbook convertToHSSfWorkbook(List<UserFeedback> feedbackInfoList) {

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = hssfSheet.createRow(0);
        firstRow.createCell(0).setCellValue("反馈时间");
        firstRow.createCell(1).setCellValue("用户姓名");
        firstRow.createCell(2).setCellValue("用户ID");
        firstRow.createCell(3).setCellValue("用户地区");
        firstRow.createCell(4).setCellValue("反馈内容");
        firstRow.createCell(5).setCellValue("用户身份");
        firstRow.createCell(6).setCellValue("状态");
        firstRow.createCell(7).setCellValue("回复");
        firstRow.createCell(8).setCellValue("反馈类型");
        firstRow.createCell(9).setCellValue("练习类型");

        FastDateFormat sdfForExcel = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

        int rowNum = 1;
        for (UserFeedback feedbackInfo : feedbackInfoList) {

            HSSFRow hssfRow = hssfSheet.createRow(rowNum++);

            hssfRow.createCell(0).setCellValue(sdfForExcel.format(feedbackInfo.getCreateDatetime()));
            hssfRow.createCell(1).setCellValue(feedbackInfo.getRealName());
            hssfRow.createCell(2).setCellValue(feedbackInfo.getUserId());
            ExRegion exRegion = userLoaderClient.loadUserRegion(feedbackInfo.getUserId());
            if (exRegion != null) {
                String provinceName = exRegion.getProvinceName();
                hssfRow.createCell(3).setCellValue(provinceName);
            }


            hssfRow.createCell(4).setCellValue(feedbackInfo.getContent());
            if (feedbackInfo.getUserType() == 1) {
                hssfRow.createCell(5).setCellValue("老师");
            } else {
                hssfRow.createCell(5).setCellValue("学生");
            }
            hssfRow.createCell(6).setCellValue(CrmUserService.feedbackStateMap.get(feedbackInfo.getState().toString()));
            hssfRow.createCell(7).setCellValue(feedbackInfo.getReply());
            hssfRow.createCell(8).setCellValue(feedbackInfo.getFeedbackType() + "/" + feedbackInfo.getFeedbackSubType1());
            hssfRow.createCell(9).setCellValue(feedbackInfo.getPracticeType());
        }

        return hssfWorkbook;
    }

    private List<String> getFeedbackQuickReplyList() {
        String query = "select ad.DESCRIPTION from ADMIN_DICT ad " +
                " where ad.GROUP_NAME = '反馈快速回复' and ad.DISABLED = 0 ";
        return utopiaSqlAdmin.withSql(query).queryColumnValues(String.class);
    }

    private String mergeReply(String oldReply, String newReply) {
        // 检查参数是否正确
        if (StringUtils.isEmpty(oldReply)) {
            return newReply;
        }
        if (StringUtils.isEmpty(newReply)) {
            return oldReply;
        }

        Set<String> replySet = new LinkedHashSet<>();
        replySet.addAll(Arrays.asList(oldReply.split(REPLY_SPLIT)));
        replySet.addAll(Arrays.asList(newReply.split(REPLY_SPLIT)));

        StringBuilder retReply = new StringBuilder();
        for (String reply : replySet) {
            retReply.append(reply).append(REPLY_SPLIT);
        }

        retReply.delete(retReply.length() - REPLY_SPLIT.length(), retReply.length());
        return retReply.toString();
    }

    private void savePopup(String text, Long userId) {
        userPopupServiceClient.createPopup(userId)
                .content(text)
                .type(PopupType.DEFAULT_AD)
                .category(PopupCategory.LOWER_RIGHT)
                .create();
    }
}
