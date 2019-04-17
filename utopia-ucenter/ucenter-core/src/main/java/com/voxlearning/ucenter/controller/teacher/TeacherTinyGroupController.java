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

package com.voxlearning.ucenter.controller.teacher;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.TinyGroup;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

/**
 * @author RuiBao
 * @version 0.1
 * @since 7/27/2015
 */
@Controller
@RequestMapping("/teacher/clazz/tinygroup")
public class TeacherTinyGroupController extends AbstractWebController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private UserPopupServiceClient userPopupServiceClient;

    // 创建小组页面
    @RequestMapping(value = "create.vpage", method = RequestMethod.GET)
    public String createTinyGroupPage(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }
        if (teacher.isJuniorTeacher()) {// 中学老师跳班级管理首页
            return "redirect:/teacher/clazz/managedclazzlist.vpage";
        }

        Long userId = teacher.getId();
        Long clazzId = getRequestLong("clazzId");
        if (!teacher.hasValidSubject()) {
            return "redirect:/teacher/index.vpage";
        }
        if (clazzId <= 0) {
            return "redirect:/teacher/index.vpage";
        }

        try {
            Map<String, Object> map = tinyGroupServiceClient.getGroupStudentsExcludeTinyGroupLeader(teacher, clazzId);
            model.addAttribute("students", map.containsKey("students") ? map.get("students") : new ArrayList<>());
            model.addAttribute("size", map.containsKey("size") ? map.get("size") : 0);
            model.addAttribute("groupId", map.containsKey("groupId") ? map.get("groupId") : -1);
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            model.addAttribute("clazzName", clazz == null ? "" : clazz.formalizeClazzName());
            model.addAttribute("clazzId", clazzId);
        } catch (Exception ex) {

            logger.error("Teacher {} load create tiny group page failed", teacher.getId(), ex);
            model.addAttribute("students", new ArrayList<>());
            model.addAttribute("size", 0);
        }

        //UserAuthentication ua = userLoaderClient.loadUserAuthentication(userId);
        String phone = sensitiveUserDataServiceClient.showUserMobile(userId, "teacherv3/systemclazz/tinygroup/create", SafeConverter.toString(userId));
        model.addAttribute("mobile", (phone != null) ? phone : "");

        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());

        return "teacherv3/systemclazz/tinygroup/create";
    }

    // 创建小组，任命小组长
    @RequestMapping(value = "ctg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createTinyGroup() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3)
            return MapMessage.errorMessage("功能升级中，请稍后再试");

        Teacher teacher = getSubjectSpecifiedTeacher();
        Long groupId = getRequestLong("groupId");
        String studentIds = getRequestString("studentIds");
        if (groupId <= 0 || StringUtils.isBlank(studentIds)) return MapMessage.errorMessage("操作失败，请重试");

        try {
            Set<Long> sids = Arrays.stream(StringUtils.split(studentIds, ","))
                    .map(ConversionUtils::toLong).collect(Collectors.toSet());
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
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
                GroupMapper mapper = groupLoaderClient.loadGroup(groupId, false);
                if (mapper != null && mapper.getClazzId() != null) {
                    ucenterMessageSender.sendZoneMessage(groupId, mapper.getClazzId(),
                            SystemRobot.getInstance(), ClazzJournalType.CREATE_TINY_GROUP.getId(),
                            ClazzJournalCategory.MISC.getId(), content);
                }
            }
            return mesg;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} create tiny groups for group {} failed", teacher.getId(), groupId, ex);
            return MapMessage.errorMessage("创建小组失败，请重试");
        }
    }

    // 小组长首页，如果没有小组显示当前不是组长的学生，如果有小组显示奖励页面
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String awardPage(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }
        if (teacher.isJuniorTeacher()) {// 中学老师跳班级管理首页
            return "redirect:/teacher/clazz/managedclazzlist.vpage";
        }

        Long clazzId = getRequestLong("clazzId");
        if (clazzId <= 0) return "redirect:/teacher/index.vpage";

        try {
            List<Map<String, Object>> tinyGroups = tinyGroupLoaderClient.getTinyGroupList_ap(teacher, clazzId);
            if (CollectionUtils.isNotEmpty(tinyGroups)) {
                model.addAttribute("tinyGroups", tinyGroups);
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(clazzId);
                model.addAttribute("clazzId", clazzId);
                model.addAttribute("clazzName", clazz == null ? "" : clazz.formalizeClazzName());

                //多学科支持
                model.addAttribute("curSubject", teacher.getSubject());
                model.addAttribute("curSubjectText", teacher.getSubject().getValue());

                return "teacherv3/systemclazz/tinygroup/index";
            } else {
                return "redirect:/teacher/clazz/tinygroup/create.vpage?clazzId=" + clazzId + "&subject=" + teacher.getSubject();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "redirect:/teacher/index.vpage";
        }
    }

    // 更改组名
    @RequestMapping(value = "rtgn.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetTinyGroupName() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        Long tinyGroupId = getRequestLong("tinyGroupId");
        String tinyGroupName = StringUtils.filterEmojiForMysql(getRequestString("tinyGroupName"));
        if (tinyGroupId <= 0 || StringUtils.isBlank(tinyGroupName) || tinyGroupName.length() > 6)
            return MapMessage.errorMessage("操作失败，请重试");

        try {
            return AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TEACHER_RESET_TINY_GROUP_NAME")
                    .keys(teacher.getId(), tinyGroupId)
                    .proxy()
                    .resetTinyGroupName(teacher, tinyGroupId, tinyGroupName);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} reset tiny group {} name failed", teacher.getId(), tinyGroupId, ex);
            return MapMessage.errorMessage("更换小组名称失败，请重试");
        }
    }

    // 奖励小组成员弹窗
    @RequestMapping(value = "rtgmwip.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rewardTinyGroupMemberWithIntegralPopup() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        Long tinyGroupId = getRequestLong("tinyGroupId");
        try {
            return tinyGroupLoaderClient.getTinyGroupMembers(teacher, tinyGroupId);
        } catch (Exception ex) {
            logger.error("Teacher {} reward tiny group {} member with integral popup page error", teacher.getId(), tinyGroupId, ex);
            return MapMessage.errorMessage("操作失败，请重试");
        }
    }

    // 批量奖励小组成员
    @RequestMapping(value = "brtgmwi.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchRewardTinyGroupMembersWithIntegral() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        String studentIds = getRequestString("studentIds");
        Integer count = getRequestInt("count");
        Long tinyGroupId = getRequestLong("tinyGroupId");
        if (tinyGroupId <= 0 || count <= 0 || count % 5 != 0 || StringUtils.isBlank(studentIds))
            return MapMessage.errorMessage("操作失败，请重试");
        if (teacher.fetchCertificationState() != SUCCESS)
            return MapMessage.errorMessage("只有认证老师才可以奖励学豆");
        Set<Long> sids = Arrays.stream(StringUtils.split(studentIds, ","))
                .map(ConversionUtils::toLong).collect(Collectors.toSet());
        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
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
            }
            return mesg;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} reward tiny group {} leader failed", teacher.getId(), tinyGroupId, ex);
            return MapMessage.errorMessage("奖励组长学豆失败，请重试");
        }
    }

    // 奖励组长
    @RequestMapping(value = "rtglwi.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rewardTinyGroupLeaderWithIntegral() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        Long tinyGroupId = getRequestLong("tinyGroupId");
        Integer count = getRequestInt("count");
        if (tinyGroupId <= 0 || count <= 0 || count % 5 != 0) return MapMessage.errorMessage("操作失败，请重试");
        if (teacher.fetchCertificationState() != SUCCESS) return MapMessage.errorMessage("只有认证老师才可以奖励学豆");

        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TEACHER_REWARD_TINY_GROUP_LEADER")
                    .keys(teacher.getId(), tinyGroupId)
                    .proxy()
                    .rewardTinyGroupLeaderWithIntegral(teacher, tinyGroupId, count);
            if (mesg.isSuccess()) {
                Long studentId = (Long) mesg.remove("studentId");
                String payload = "老师通知：<br/>你在担任小组长期间，组内同学作业完成情况很好，"
                        + teacher.getSubject().getValue() + "老师奖励你" + count + "学豆，继续努力哦！";
                userPopupServiceClient.createPopup(studentId)
                        .content(payload)
                        .type(PopupType.TINY_GROUP)
                        .category(PopupCategory.LOWER_RIGHT)
                        .create();
            }
            return mesg;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} reward tiny group {} leader failed", teacher.getId(), tinyGroupId, ex);
            return MapMessage.errorMessage("奖励组长学豆失败，请重试");
        }
    }

    // 更换最佳小组
    @RequestMapping(value = "rbtg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetBestTinyGroup() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        Long tinyGroupId = getRequestLong("tinyGroupId");
        if (tinyGroupId <= 0) return MapMessage.errorMessage("操作失败，请重试");

        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TEACHER_RESET_BEST_TINY_GROUP")
                    .keys(teacher.getId())
                    .proxy()
                    .resetBestTinyGroup(teacher, tinyGroupId);
            if (mesg.isSuccess() && mesg.containsKey("studentIds")) {
                // noinspection unchecked
                Set<Long> studentIds = (Set<Long>) mesg.remove("studentIds");
                String payload = "老师通知：<br/>你们组" + teacher.getSubject().getValue() + "作业完成情况很好，"
                        + "老师奖励“最佳" + teacher.getSubject().getValue() + "小组”班级空间气泡！快去签到炫一下吧！";
                for (Long studentId : studentIds) {
                    userPopupServiceClient.createPopup(studentId)
                            .content(payload)
                            .type(PopupType.TINY_GROUP)
                            .category(PopupCategory.LOWER_RIGHT)
                            .create();
                }
                TinyGroup tinyGroup = (TinyGroup) mesg.remove("tinyGroup");
                String content = tinyGroup.getTinyGroupName() + "前段时间作业完成情况非常好，" +
                        teacher.getSubject().getValue() + "老师奖励称号：“最佳" +
                        teacher.getSubject().getValue() + "小组”，获得专属气泡奖励！";
                GroupMapper mapper = groupLoaderClient.loadGroup(tinyGroup.getGroupId(), false);
                if (mapper != null && mapper.getClazzId() != null) {
                    ucenterMessageSender.sendZoneMessage(tinyGroup.getGroupId(), mapper.getClazzId(),
                            SystemRobot.getInstance(), ClazzJournalType.BEST_TINY_GROUP.getId(),
                            ClazzJournalCategory.MISC.getId(), content);
                }
            }
            return mesg;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} set tiny group {} best group failed", teacher.getId(), tinyGroupId, ex);
            return MapMessage.errorMessage("更换最佳小组失败，请重试");
        }
    }

    // 调整小组页面
    @RequestMapping(value = "editcrew.vpage", method = RequestMethod.GET)
    public String resetTinyGroupPage(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher != null && teacher.isJuniorTeacher()) {// 中学老师跳班级管理首页
            return "redirect:/teacher/clazz/managedclazzlist.vpage";
        }

        Long clazzId = getRequestLong("clazzId");
        if (clazzId <= 0) return "redirect:/teacher/index.vpage";

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("clazzName", clazz.formalizeClazzName());

        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());

        return "teacherv3/systemclazz/tinygroup/editcrew";
    }

    // 调整小组页面 -- 获取数据
    @RequestMapping(value = "getcrew.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadResetTinyGroupPageData() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        Long clazzId = getRequestLong("clazzId");
        if (clazzId <= 0) return MapMessage.errorMessage("班级ID错误！");
        Map<String, Object> info = new HashMap<>();
        try {
            info.putAll(tinyGroupServiceClient.resetTinyGroupPage(teacher, clazzId));
        } catch (Exception ex) {
            logger.error("Teacher {} load reset tiny group member page failed", teacher.getId(), ex);
        }
        return MapMessage.successMessage().add("groups", info);
    }

    // 调整组员
    @RequestMapping(value = "rtgm.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetTinyGroupMember() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3)
            return MapMessage.errorMessage("功能升级中，请稍后再试");

        Teacher teacher = getSubjectSpecifiedTeacher();
        Long groupId = getRequestLong("groupId");
        Long to = getRequestLong("to");
        String studentIds = getRequestString("studentIds");
        if (groupId <= 0 || StringUtils.isBlank(studentIds)) return MapMessage.errorMessage("操作失败，请重试");

        try {
            Set<Long> sids = Arrays.stream(StringUtils.split(studentIds, ","))
                    .map(ConversionUtils::toLong).collect(Collectors.toSet());

            return AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .resetTinyGroupMember(teacher, sids, groupId, to);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} reset tiny group members failed", teacher.getId(), groupId, ex);
            return MapMessage.errorMessage("调整组长失败，请重试");
        }
    }

    // 调整小组长
    @RequestMapping(value = "rtgl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetTinyGroupLeader() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3)
            return MapMessage.errorMessage("功能升级中，请稍后再试");

        Teacher teacher = getSubjectSpecifiedTeacher();
        Long groupId = getRequestLong("groupId");
        Long tinyGroupId = getRequestLong("tinyGroupId");
        Long leaderId = getRequestLong("leaderId");
        String message = getRequestString("message");

        if (groupId <= 0 || tinyGroupId <= 0 || leaderId <= 0) return MapMessage.errorMessage("操作失败，请重试");

        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .resetTinyGroupLeader(teacher, groupId, tinyGroupId, leaderId, message);
            if (mesg.isSuccess()) {
                User leader = raikouSystem.loadUser(leaderId);
                String name = StringUtils.isBlank(leader.fetchRealname()) ? "学号为" + leader.getId() + "的同学" : " " + leader.fetchRealname() + " ";
                String content = teacher.getSubject().getValue() + "老师任命" + name + "为小组长。";
                GroupMapper mapper = groupLoaderClient.loadGroup(groupId, false);
                if (mapper != null && mapper.getClazzId() != null) {
                    ucenterMessageSender.sendZoneMessage(groupId, mapper.getClazzId(), SystemRobot.getInstance(),
                            ClazzJournalType.RESET_TINY_GROUP_LEADER.getId(), ClazzJournalCategory.MISC.getId(), content);
                }
            }
            return mesg;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} reset tiny group leaders failed", teacher.getId(), groupId, ex);
            return MapMessage.errorMessage("调整组长失败，请重试");
        }
    }

    // 删除小组
    @RequestMapping(value = "deltg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTinyGroup() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3)
            return MapMessage.errorMessage("功能升级中，请稍后再试");

        Teacher teacher = getSubjectSpecifiedTeacher();
        Long groupId = getRequestLong("groupId");
        Long tinyGroupId = getRequestLong("tinyGroupId");

        if (groupId <= 0 || tinyGroupId <= 0) return MapMessage.errorMessage("操作失败，请重试");

        try {
            return AtomicLockManager.instance().wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .deleteTinyGroup(teacher, groupId, tinyGroupId);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Teacher {} delete tiny groupfailed", teacher.getId(), groupId, ex);
            return MapMessage.errorMessage("删除小组失败，请重试");
        }
    }
}