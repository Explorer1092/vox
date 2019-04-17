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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.message.api.constant.UserMessageType;
import com.voxlearning.utopia.service.message.api.entity.AdminMessageSendLog;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
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
 * @since 下午2:17,13-11-7.
 */
@Controller
@RequestMapping("/site/message")
public class SiteMessageController extends SiteAbstractController {

    private static final int MESSAGE_PAGE_SIZE = 20;

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private MessageLoaderClient messageLoaderClient;
    @Inject
    private MessageServiceClient messageServiceClient;
    @Inject
    private ZoneQueueServiceClient zoneQueueServiceClient;

    @RequestMapping(value = "messagelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String messageList(Model model) {

        if (isRequestGet())
            return "site/message/messagelist";

        String adminId = getRequestParameter("adminId", null);
        model.addAttribute("adminId", adminId);
        int pageNum = getRequestInt("pageNum", 0);
        String allAdminUsers = getRequestParameter("allAdminUsers", null);
        if (pageNum < 0) pageNum = 0;
        Page<AdminMessageSendLog> messagePagination;

        if ("on".equals(allAdminUsers)) {
            model.addAttribute("allAdminUsers", allAdminUsers);
            messagePagination = messageLoaderClient.getMessageLoader().getAdminMessageSendLogs(pageNum, MESSAGE_PAGE_SIZE);
            if ((messagePagination != null) && messagePagination.getNumber() >= messagePagination.getTotalPages()
                    && messagePagination.getNumber() != 0)
                messagePagination = messageLoaderClient.getMessageLoader().getAdminMessageSendLogs(pageNum - 1, MESSAGE_PAGE_SIZE);
        } else {
            if (StringUtils.isBlank(adminId))
                return "site/message/messagelist";

            adminId = adminId.replaceAll("\\s", "");
            // FIXME COMMENT BY ZHAO REX for adminIdLong may not have been initialized.
            Long adminIdLong = null;
            try {
                adminIdLong = Long.valueOf(adminId);
            } catch (Exception ignored) {
                AuthCurrentAdminUser adminUser = new AuthCurrentAdminUser();
                adminUser.setAdminUserName(adminId);
                adminId = String.valueOf(adminUser.getFakeUserId());
            }

            // FIXME COMMENT BY ZHAO REX for adminIdLong may not have been initialized.
            messagePagination = messageLoaderClient.getMessageLoader().getAdminMessageSendLogs(adminIdLong, pageNum, MESSAGE_PAGE_SIZE);
            if ((messagePagination != null) && messagePagination.getNumber() >= messagePagination.getTotalPages()
                    && messagePagination.getNumber() != 0)
                messagePagination = messageLoaderClient.getMessageLoader().getAdminMessageSendLogs(adminIdLong, pageNum - 1, MESSAGE_PAGE_SIZE);
        }

        model.addAttribute("messageJournalList", messagePagination == null ? null : messagePagination.getContent());
        model.addAttribute("pageNum", messagePagination == null ? null : messagePagination.getNumber());
        model.addAttribute("totalPageNum", (messagePagination == null) ? null : messagePagination.getTotalPages());
        return "site/message/messagelist";
    }


    @SuppressWarnings("GrMethodMayBeStatic")
    @RequestMapping(value = "messagehomepage.vpage", method = RequestMethod.GET)
    public String messageHomepage() {
        return "site/message/messagehomepage";
    }

    @RequestMapping(value = "clazzmessageindex.vpage", method = RequestMethod.GET)
    public String clazzMessageIndex() {
        return "site/message/clazzmessageindex";
    }

    @RequestMapping(value = "messagehomepage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage messageHomepagePost() {

        String userIdSetStr = getRequestParameter("receiveUserId", "");
        String messageContent = getRequestParameter("messageContent", "").trim();
        Boolean globalMessage = getRequestBool("globalMessage");
        Boolean teacherMessage = getRequestBool("teacherMessage");
        Boolean parentMessage = getRequestBool("parentMessage");
        Boolean studentMessage = getRequestBool("studentMessage");
        Boolean ambassadorMessage = getRequestBool("ambassadorMessage");

        if (StringUtils.isBlank(messageContent))
            return MapMessage.errorMessage("系统消息内容不能为空");

        User sender = new User();
        if (getCurrentAdminUser().getAdminUserName().matches("[\\d]+"))
            sender.setId(Long.valueOf(getCurrentAdminUser().getAdminUserName()));
        else {
            sender.setId(getCurrentAdminUser().getFakeUserId());
            sender.getProfile().setRealname(getCurrentAdminUser().getAdminUserName());
            sender.setUserType(UserType.EMPLOYEE.getType());
        }

        if (globalMessage) {
            messageCommandServiceClient.getMessageCommandService().sendGlobalMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), messageContent);
            // admin log
            addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "发送全局系统消息",
                    "", null, messageContent);
            return MapMessage.successMessage("发送全局系统消息成功");
        }

        if (teacherMessage || parentMessage || studentMessage) {
            List<String> userTypeList = new ArrayList<>();
            if (teacherMessage) {
                messageCommandServiceClient.getMessageCommandService().sendGroupMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), UserType.TEACHER.getType(), messageContent);
                userTypeList.add("teacher");
            }

            if (parentMessage) {
                messageCommandServiceClient.getMessageCommandService().sendGroupMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), UserType.PARENT.getType(), messageContent);
                userTypeList.add("parent");
            }

            if (studentMessage) {
                messageCommandServiceClient.getMessageCommandService().sendGroupMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), UserType.STUDENT.getType(), messageContent);
                userTypeList.add("student");
            }
            // admin log
            addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "发送" + userTypeList.toString() + "消息",
                    "", null, messageContent);
            return MapMessage.successMessage("发送系统消息成功");
        }

        //大使消息
        if (ambassadorMessage) {
            if (StringUtils.isNotBlank(userIdSetStr)) {
                //给指定大使发消息
                Set<Long> userIdSet = new HashSet<>();
                try {
                    String[] userIdStrList = userIdSetStr.split("[,，\\s]+");
                    for (String it : userIdStrList) {
                        if (StringUtils.isNotBlank(it))
                            userIdSet.add(Long.valueOf(it));
                    }
                } catch (Exception ignored) {
                    return MapMessage.errorMessage("存在不符合规范的用户名");
                }
                if (userIdSet.size() == 0)
                    return MapMessage.errorMessage("未输入用户ID");

                List<Long> failureUserId = new ArrayList<>();
                for (Long it : userIdSet) {
                    AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(it)
                            .stream().findFirst().orElse(null);
                    if (ref != null) {
                        teacherLoaderClient.sendTeacherMessage(sender.getId(),
                                sender.fetchUserType(),
                                sender.fetchRealname(),
                                ref.getAmbassadorId(),
                                messageContent,
                                UserMessageType.AMBASSADOR_MSG);
                    } else {
                        failureUserId.add(it);
                    }
                }
                // admin log
                addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "发送校园大使消息",
                        "", null, messageContent);
                if (CollectionUtils.isEmpty(failureUserId)) {
                    return MapMessage.successMessage("发送系统消息成功");
                } else {
                    return MapMessage.errorMessage("未成功发送系统消息的用户ID:" + StringUtils.join(failureUserId, ','));
                }
            } else {
                //给所有大使发信息
                String sql = "SELECT DISTINCT AMBASSADOR_ID FROM VOX_AMBASSADOR_SCHOOL_REF WHERE DISABLED = FALSE";
                List<Long> ambassadorIds = utopiaSql.withSql(sql).queryColumnValues(Long.class);
                for (Long ambassadorId : ambassadorIds) {
                    teacherLoaderClient.sendTeacherMessage(sender.getId(),
                            sender.fetchUserType(),
                            sender.fetchRealname(),
                            ambassadorId,
                            messageContent,
                            UserMessageType.AMBASSADOR_MSG);
                }
                // admin log
                addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "发送校园大使消息",
                        "", null, messageContent);
                return MapMessage.successMessage("发送校园大使消息成功");
            }
        }
        Set<Long> userIdSet = new HashSet<>();
        try {
            String[] userIdStrList = userIdSetStr.split("[,，\\s]+");
            for (String it : userIdStrList) {
                if (StringUtils.isNotBlank(it))
                    userIdSet.add(Long.valueOf(it));
            }
        } catch (Exception ignored) {
            return MapMessage.errorMessage("存在不符合规范的用户名");
        }

        if (userIdSet.size() == 0)
            return MapMessage.errorMessage("未输入用户ID");

        List<Long> failureUserId = new ArrayList<>();
        for (Long it : userIdSet) {
            User user = userLoaderClient.loadUser(it);
            if (user != null) {
                if (user.isTeacher()) {
                    teacherLoaderClient.sendTeacherMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), user.getId(), messageContent);
                } else {
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), user.getId(), messageContent);
                }
            } else {
                failureUserId.add(it);
            }
        }

        // admin log
        addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "批量发送系统消息",
                "", null, "IDs:[" + StringUtils.join(userIdSet, ',') + "], content:" + messageContent);

        if (CollectionUtils.isEmpty(failureUserId))
            return MapMessage.successMessage("发送系统消息成功");
        else
            return MapMessage.errorMessage("未成功发送系统消息的用户ID:" + StringUtils.join(failureUserId, ','));
    }

    @RequestMapping(value = "deletemessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage deleteMessage() {
        String sendLogId = getRequestParameter("sendLogId", "");
        MapMessage response = messageServiceClient.getMessageService().deleteAdminMessageSendLog(sendLogId);
        if (response.isSuccess()) {
            response.setInfo("删除系统消息" + sendLogId + "成功");
        } else {
            response.setInfo("删除系统消息失败");
        }
        return response;
    }

    @RequestMapping(value = "sendclazzmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage sendClazzMsg() {
        String clazzId = getRequestParameter("clazzId", "");
        String content = getRequestParameter("content", "");
        if (clazzId.equals("") || content.equals("")) {
            return MapMessage.errorMessage("发送失败，参数错误");
        }
        String[] clazzIds = clazzId.split("\\n");
        String[] messages = content.split("\\n");
        String errmsg = "";
        String successmsg = "";
        for (int i = 0; i < clazzIds.length; i++) {
            try {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(Long.parseLong(clazzIds[i]));
                if (clazz == null) {
                    errmsg = errmsg + clazzIds[i] + ",";
                    continue;
                }
                zoneQueueServiceClient.createClazzJournal(clazz.getId())
                        .withClazzJournalType(ClazzJournalType.SYSTEM_NOTICE)
                        .withClazzJournalCategory(ClazzJournalCategory.MISC)
                        .withUser(SystemRobot.getInstance().getId())
                        .withUser(SystemRobot.getInstance().fetchUserType())
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", messages[i])))
                        .commit();
                successmsg = successmsg + clazzIds[i] + ",";
            } catch (Exception ignored) {
                errmsg = errmsg + clazzIds[i] + ",";
            }
        }
        User sender = new User();
        if (getCurrentAdminUser().getAdminUserName().matches("[\\d]+"))
            sender.setId(Long.valueOf(getCurrentAdminUser().getAdminUserName()));
        else
            sender.setId(getCurrentAdminUser().getFakeUserId());
        sender.getProfile().setRealname(getCurrentAdminUser().getAdminUserName());
        sender.setUserType(UserType.EMPLOYEE.getType());
        addAdminLog("message-管理员${currentAdminUser.adminUserName}发送班级空间系统通知 ",
                "", null, "CLAZZ_ID:" + successmsg + ", content:" + content);

        return MapMessage.successMessage("发送完毕，成功班级：" + successmsg + "失败班级：" + errmsg);
    }


    @RequestMapping(value = "batchmessagehomepage.vpage", method = RequestMethod.GET)
    public String batchMessageHomepage() {
        return "site/message/batchmessagehomepage";
    }

    @RequestMapping(value = "batchmessagesend.vpage", method = RequestMethod.POST)
    public String batchSendMessageHomepage(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("系统消息内容不能为空");
        }

        User sender = new User();
        if (getCurrentAdminUser().getAdminUserName().matches("[\\d]+"))
            sender.setId(Long.valueOf(getCurrentAdminUser().getAdminUserName()));
        else
            sender.setId(getCurrentAdminUser().getFakeUserId());
        sender.getProfile().setRealname(getCurrentAdminUser().getAdminUserName());
        sender.setUserType(UserType.EMPLOYEE.getType());

        String[] messages = content.split("\\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();

        for (String m : messages) {
            String[] info = m.split("\\t");
            if (info.length < 2) {
                lstFailed.add(m);
                continue;
            }

            String userId = StringUtils.deleteWhitespace(info[0]);
            String text = StringUtils.deleteWhitespace(m.substring(userId.length(), m.length()));

            if (text.length() == 0) {
                lstFailed.add(m);
                continue;
            }

            try {
                User user = userLoaderClient.loadUser(Long.parseLong(userId));
                if (user != null) {
                    if (user.isTeacher()) {
                        teacherLoaderClient.sendTeacherMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), user.getId(), text);
                    } else {
                        messageCommandServiceClient.getMessageCommandService().sendUserMessage(sender.getId(), sender.fetchUserType(), sender.fetchRealname(), user.getId(), text);

                        //给移动端发系统消息
                        AppMessage message = new AppMessage();
                        message.setUserId(user.getId());
                        message.setMessageType(ParentMessageType.REMINDER.type);
                        message.setTitle(text);
                        message.setContent(text);
                        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                        //发送push
                        appMessageServiceClient.sendAppJpushMessageByIds(text, AppMessageSource.PARENT, Collections.singletonList(user.getId()), new HashMap<>());
                    }
                    lstSuccess.add(m);
                } else {
                    lstFailed.add(m);
                }
            } catch (Exception ex) {
                logger.error("msg:{}", m, ex.getMessage());
                lstFailed.add(m);
            }
            // admin log
            addAdminLog("message-管理员${currentAdminUser.adminUserName}批量发送系统消息",
                    "", null, "ID:" + userId + ", content:" + text);
        }

        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        return "/site/message/batchmessagehomepage";
    }
}
