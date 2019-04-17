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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.washington.controller.open.OpenApiReturnCode.*;
import static java.lang.Long.MIN_VALUE;

/**
 * @author Ruib
 * @version 0.1
 * @since 2015/11/30
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/clazz/tinygroup")
@Slf4j
public class WechatTeacherTinyGroupController extends AbstractOpenController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    // 小组奖励页面，如果没有小组，需要跳转到选择学生创建小组的页面
    @RequestMapping(value = "awardpage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext awardPage(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        Long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long clazzId = SafeConverter.toLong(context.getParams().get("cid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || clazzId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        try {
            List<Map<String, Object>> tinyGroups = tinyGroupLoaderClient.getTinyGroupList_ap(teacher, clazzId);
            context.add("tinyGroups", tinyGroups);
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 创建小组页面
    @RequestMapping(value = "create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext createTinyGroupPage(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        Long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long clazzId = SafeConverter.toLong(context.getParams().get("cid"), MIN_VALUE);
        if (teacherId == null || teacherId == MIN_VALUE || clazzId == null || clazzId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        try {
            Map<String, Object> map = tinyGroupServiceClient.getGroupStudentsExcludeTinyGroupLeader(teacher, clazzId);
            context.add("info", map);
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 任命小组长，创建小组
    @RequestMapping(value = "ctg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext createTinyGroup(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        String studentIds = SafeConverter.toString(context.getParams().get("sids"));
        if (teacherId == MIN_VALUE || groupId == MIN_VALUE || StringUtils.isBlank(studentIds)) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("功能升级中，请稍后再试");
            return context;
        }

        try {
            Set<Long> sids = Arrays.asList(StringUtils.split(studentIds, ",")).stream()
                    .map(ConversionUtils::toLong).collect(Collectors.toSet());
            MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .createTinyGroup(teacher, groupId, sids);
            if (mesg.isSuccess()) {
                String names = StringUtils.join(userLoaderClient.loadUsers(sids)
                        .values()
                        .stream()
                        .map(user -> StringUtils.isBlank(user.fetchRealname()) ? user.getId().toString() : user.fetchRealname())
                        .collect(Collectors.toList()), ",");
                String content = teacher.getSubject().getValue() + "老师创建了" +
                        teacher.getSubject().getValue() + "小组，小组长是：" + names +
                        "，快去首页 “班级卡片” 加入老师为你指定的小组吧！";
                GroupMapper mapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
                if (mapper != null && mapper.getClazzId() != null) {
                    zoneQueueServiceClient.createClazzJournal(mapper.getClazzId())
                            .withGroup(groupId)
                            .withUser(SystemRobot.getInstance().getId())
                            .withUser(SystemRobot.getInstance().fetchUserType())
                            .withClazzJournalType(ClazzJournalType.CREATE_TINY_GROUP)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                            .commit();
                }
            }
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 奖励小组成员弹窗
    @RequestMapping(value = "rtgmwip.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext rewardTinyGroupMemberWithIntegralPopup(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long tinyGroupId = SafeConverter.toLong(context.getParams().get("tgid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || tinyGroupId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            MapMessage mesg = tinyGroupLoaderClient.getTinyGroupMembers(teacher, tinyGroupId);
            if (mesg.isSuccess()) {
                context.add("students", mesg.get("students"));
                context.setCode(SUCCESS_CODE);
            } else {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError(mesg.getInfo());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 批量奖励小组成员
    @RequestMapping(value = "brtgmwi.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext batchRewardTinyGroupMembersWithIntegral(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Integer count = SafeConverter.toInt(context.getParams().get("count"), Integer.MIN_VALUE);
        Long tinyGroupId = SafeConverter.toLong(context.getParams().get("tgid"), MIN_VALUE);
        String studentIds = SafeConverter.toString(context.getParams().get("sids"));

        if (teacherId == MIN_VALUE || tinyGroupId == MIN_VALUE || count == Integer.MIN_VALUE
                || StringUtils.isBlank(studentIds) || count % 5 != 0) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        if (teacher.fetchCertificationState() != SUCCESS) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("只有认证老师才可以奖励学豆");
            return context;
        }

        try {
            Set<Long> sids = Arrays.asList(StringUtils.split(studentIds, ",")).stream()
                    .map(ConversionUtils::toLong).collect(Collectors.toSet());
            MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TEACHER_REWARD_TINY_GROUP_MEMBERS")
                    .keys(teacher.getId(), tinyGroupId)
                    .proxy()
                    .rewardTinyGroupMembersWithIntegral(teacher, sids, tinyGroupId, count);
            if (mesg.isSuccess()) {
                for (Long sid : sids) {
                    String payload = "因为你们组本周的优秀表现，老师奖励你" + count + "学豆！请再接再厉哦~";
                    userPopupServiceClient.createPopup(sid)
                            .content(payload)
                            .type(PopupType.TINY_GROUP)
                            .category(PopupCategory.LOWER_RIGHT)
                            .create();
                }
                context.setCode(SUCCESS_CODE);
            } else {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError(mesg.getInfo());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 调整小组页面
    @RequestMapping(value = "atgp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetTinyGroupPage(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long teacherId = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        Long clazzId = SafeConverter.toLong(context.getParams().get("cid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || clazzId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            Map<String, Object> info = tinyGroupServiceClient.resetTinyGroupPage_wechat(teacher, clazzId);
            context.add("info", info);
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 更改组名
    @RequestMapping(value = "rtgn.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetTinyGroupName(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long tinyGroupId = SafeConverter.toLong(context.getParams().get("tgid"), MIN_VALUE);
        String tinyGroupName = StringHelper.filterEmojiForMysql(SafeConverter.toString(context.getParams().get("tgn")));
        if (teacherId == MIN_VALUE || tinyGroupId == MIN_VALUE || StringUtils.isBlank(tinyGroupName)
                || tinyGroupName.length() > 6) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TEACHER_RESET_TINY_GROUP_NAME")
                    .keys(teacher.getId(), tinyGroupId)
                    .proxy()
                    .resetTinyGroupName(teacher, tinyGroupId, tinyGroupName);
            if (mesg.isSuccess()) {
                context.setCode(SUCCESS_CODE);
            } else {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError(mesg.getInfo());
            }
        } catch (CannotAcquireLockException ex) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("正在处理，请不要重复提交");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 删除小组
    @RequestMapping(value = "deltg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext deleteTinyGroup(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long tinyGroupId = SafeConverter.toLong(context.getParams().get("tgid"), MIN_VALUE);
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || groupId == MIN_VALUE || tinyGroupId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("功能升级中，请稍后再试");
            return context;
        }

        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .deleteTinyGroup(teacher, groupId, tinyGroupId);
            if (mesg.isSuccess()) {
                context.setCode(SUCCESS_CODE);
            } else {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError(mesg.getInfo());
            }
        } catch (CannotAcquireLockException ex) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("正在处理，请不要重复提交");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 获取小组组员
    @RequestMapping(value = "stgm.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext showTinyGroupMembers(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long tinyGroupId = SafeConverter.toLong(context.getParams().get("tgid"), MIN_VALUE);
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || groupId == MIN_VALUE || tinyGroupId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            context.add("members", tinyGroupServiceClient.getTinyGroupMembers_wechat(tinyGroupId));
            context.add("groupId", groupId);
            context.add("tinyGroupId", tinyGroupId);
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 获取组中没有小组的学生
    @RequestMapping(value = "smwtg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext showMembersWithoutTinyGroup(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || groupId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            context.add("members", tinyGroupServiceClient.getMembersWithoutTinyGroup_wechat(groupId));
            context.add("groupId", groupId);
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 获取所有小组
    @RequestMapping(value = "atgs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext allTinyGroups(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        if (teacherId == MIN_VALUE || groupId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            context.add("tinyGroups", tinyGroupServiceClient.allTinyGroups_wechat(groupId));
            context.add("groupId", groupId);
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 调整组员
    @RequestMapping(value = "rtgm.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetTinyGroupMember(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        Long to = SafeConverter.toLong(context.getParams().get("to"), MIN_VALUE);
        String studentIds = SafeConverter.toString(context.getParams().get("sids"));

        if (teacherId == MIN_VALUE || to == MIN_VALUE || groupId == MIN_VALUE
                || StringUtils.isBlank(studentIds)) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("功能升级中，请稍后再试");
            return context;
        }

        try {
            Set<Long> sids = Arrays.asList(StringUtils.split(studentIds, ",")).stream()
                    .map(ConversionUtils::toLong).collect(Collectors.toSet());

            MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .resetTinyGroupMember(teacher, sids, groupId, to);
            if (mesg.isSuccess()) {
                context.setCode(SUCCESS_CODE);
            } else {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError(mesg.getInfo());
            }
        } catch (CannotAcquireLockException ex) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("正在处理，请不要重复提交");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    // 调整小组长
    @RequestMapping(value = "rtgl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetTinyGroupLeader(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long uid = SafeConverter.toLong(context.getParams().get("uid"), MIN_VALUE);
        String subject = SafeConverter.toString(context.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(uid, Subject.of(subject));
        Long groupId = SafeConverter.toLong(context.getParams().get("gid"), MIN_VALUE);
        Long tinyGroupId = SafeConverter.toLong(context.getParams().get("tgid"), MIN_VALUE);
        Long leaderId = SafeConverter.toLong(context.getParams().get("lid"), MIN_VALUE);

        if (teacherId == MIN_VALUE || tinyGroupId == MIN_VALUE || groupId == MIN_VALUE || leaderId == MIN_VALUE) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("功能升级中，请稍后再试");
            return context;
        }

        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .resetTinyGroupLeader(teacher, groupId, tinyGroupId, leaderId, "");
            if (mesg.isSuccess()) {
                User leader = raikouSystem.loadUser(leaderId);
                String name = StringUtils.isBlank(leader.fetchRealname()) ? "学号为" + leader.getId() + "的同学" : " " + leader.fetchRealname() + " ";
                String content = teacher.getSubject().getValue() + "老师任命" + name + "为小组长。";
                GroupMapper mapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
                if (mapper != null && mapper.getClazzId() != null) {
                    zoneQueueServiceClient.createClazzJournal(mapper.getClazzId())
                            .withGroup(groupId)
                            .withUser(SystemRobot.getInstance().getId())
                            .withUser(SystemRobot.getInstance().fetchUserType())
                            .withClazzJournalType(ClazzJournalType.RESET_TINY_GROUP_LEADER)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                            .commit();
                }
                context.setCode(SUCCESS_CODE);
            } else {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError(mesg.getInfo());
            }
        } catch (CannotAcquireLockException ex) {
            context.setCode(BUSINESS_ERROR_CODE);
            context.setError("正在处理，请不要重复提交");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }
}
