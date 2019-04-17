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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.clazz.client.AsyncTinyGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TinyGroup;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RuiBao
 * @version 0.1
 * @since 7/30/2015
 */
@Controller
@RequestMapping("/student/tinygroup")
public class StudentTinyGroupController extends AbstractController {

    @Inject private AsyncTinyGroupServiceClient asyncTinyGroupServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    // 小助手任命书
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        if (subject == Subject.UNKNOWN) return "redirect:/student/index.vpage";

        try {
            GroupMapper group = deprecatedGroupLoaderClient.loadStudentGroups(currentUserId(), false)
                    .stream()
                    .filter(source -> source.getSubject() == subject)
                    .findFirst()
                    .orElse(null);

            if (group != null) {
                Map<Long, List<Teacher>> map = teacherLoaderClient.loadGroupTeacher(Collections.singleton(group.getId()));
                Teacher teacher = MiscUtils.firstElement(map.get(group.getId()));
                if (teacher != null) {
                    String name = teacher.fetchRealname();
                    model.addAttribute("name", StringUtils.isBlank(name) ? "" : StringUtils.substring(name, 0, 1));
                    model.addAttribute("subject", subject.getValue());
                }
            }

            asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TinyGroupLeaderCardCacheManager_turnOff(currentUserId(), subject)
                    .awaitUninterruptibly();
        } catch (Exception ex) {
            logger.error("Student {} tiny group index page error.", ex);
        }

        return "studentv3/activity/tinygroup";
    }

    // 查看小组详情
    @RequestMapping(value = "tgd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage tinyGroupDetail() {
        User student = currentStudent();
        try {
            return tinyGroupLoaderClient.studentLoadTinyGroupDetail(student);
        } catch (Exception ex) {
            logger.error("Student {} load tiny group detail failed", student.getId(), ex);
            return MapMessage.errorMessage();
        }
    }

    // 学生加入小组弹窗
    @RequestMapping(value = "sjtgpp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentJoinTinyGroupPopupPage() {
        User student = currentStudent();
        List<Map<String, Object>> info = new ArrayList<>();
        try {
            info = tinyGroupServiceClient.studentJoinTinyGroupPopupPage(student);
        } catch (Exception ex) {
            logger.error("Student {} join tiny group popup page failed.", student.getId(), ex);
        }
        return MapMessage.successMessage().add("info", info);
    }

    // 学生加入小组
    @RequestMapping(value = "sjtg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentJoinTinyGroup() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3)
            return MapMessage.errorMessage("功能升级中，请稍后再试");

        User student = currentStudent();
        String tgids = getRequestString("tgids");
        if (StringUtils.isEmpty(tgids)) return MapMessage.errorMessage("加入小组失败");

        Set<Long> tinyGroupIds = Arrays.asList(StringUtils.split(tgids, ",")).stream()
                .map(ConversionUtils::toLong).collect(Collectors.toSet());
        Map<Long, TinyGroup> tinyGroups = asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                .loadTinyGroups(tinyGroupIds)
                .getUninterruptibly();

        List<String> result = new ArrayList<>();
        for (Long tinyGroupId : tinyGroupIds) {
            TinyGroup tinyGroup = tinyGroups.get(tinyGroupId);
            if (tinyGroup == null) {
                result.add("您选择的小组不存在，请重新选择");
                continue;
            }

            try {
                MapMessage mesg = atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                        .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                        .keys(tinyGroup.getGroupId())
                        .proxy()
                        .studentJoinTinyGroup(student, tinyGroup);
                if (mesg.isSuccess()) {
                    result.add("加入" + tinyGroup.getSubject().getValue() + "小组成功！可在班级空间查看小组详情~");
                } else {
                    result.add(mesg.getInfo());
                }
            } catch (CannotAcquireLockException ex) {
                result.add("正在处理，请不要重复提交");
            } catch (Exception ex) {
                logger.error("Student {} join tiny group {} failed.", student.getId(), tinyGroupId, ex);
                result.add("加入" + tinyGroup.getSubject().getValue() + "小组失败");
            }
        }
        return MapMessage.successMessage().add("result", result);
    }

    // 学生创建小组
    @RequestMapping(value = "sctg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentCreateTinyGroup() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.HOUR_OF_DAY) == 3)
            return MapMessage.errorMessage("功能升级中，请稍后再试");

        User student = currentStudent();
        Long groupId = getRequestLong("groupId");
        if (groupId < 0) return MapMessage.errorMessage("操作失败，请重试");

        try {
            return atomicLockManager.wrapAtomic(tinyGroupServiceClient)
                    .keyPrefix("TINY_GROUP_MEMBER_CHANGE")
                    .keys(groupId)
                    .proxy()
                    .studentCreateTinyGroup(student, groupId);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Student {} create tiny group in group {} failed.", student.getId(), groupId, ex);
            return MapMessage.errorMessage("操作失败，请重试");
        }
    }

    // 学生班级空间小组之星
    @RequestMapping(value = "tgs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage tinyGroupStar() {
        Long studentId = currentUserId();
        List<Map<String, Object>> stars = new ArrayList<>();
        try {
            stars = tinyGroupServiceClient.tinyGroupStar(studentId);
        } catch (Exception ex) {
            logger.error("Load Student {} Tiny Group Star failed.", studentId, ex);
        }
        return MapMessage.successMessage().add("stars", stars);
    }
}
