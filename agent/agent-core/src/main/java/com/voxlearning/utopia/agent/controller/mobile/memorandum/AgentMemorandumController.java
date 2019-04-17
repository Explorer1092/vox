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

package com.voxlearning.utopia.agent.controller.mobile.memorandum;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.memorandum.MemorandumPage;
import com.voxlearning.utopia.agent.constants.MemorandumType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang
 * on 2017/5/10.
 */
@Controller
@RequestMapping("/mobile/memorandum")
public class AgentMemorandumController extends AbstractAgentController {
    @Inject private AgentMemorandumService agentMemorandumService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    private static final String FORMAT_TIME = "yyyy-MM-dd HH:mm";

    @Inject private SchoolLoaderClient schoolLoaderClient;

    // 照片库
    @RequestMapping(value = "photo_library.vpage", method = RequestMethod.GET)
    public String photoLibrary(Model model) {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("teacherId", teacherId);
        return "rebuildViewDir/mobile/memorandum/photoLibrary";
    }

    // 学校备忘录 老师备忘录
    @RequestMapping(value = "school_memorandum_page.vpage", method = RequestMethod.GET)
    public String schoolMemorandumPage(Model model) {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("teacherId", teacherId);
        return "rebuildViewDir/mobile/memorandum/school_memorandum";
    }

    // 用户备忘录
    @RequestMapping(value = "user_memorandum_page.vpage", method = RequestMethod.GET)
    public String userMemorandumPage() {
        return "rebuildViewDir/mobile/memorandum/user_memorandum";
    }

    // 分页查询
    @RequestMapping(value = "find_school_memorandum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findSchoolMemorandum() {
        Integer page = getRequestInt("page");
        Long schoolId = getRequestLong("schoolId");
        if (page == 0) {
            page = 1;
        }
        MemorandumType memorandumType = translateType();
        MapMessage msg = MapMessage.successMessage();
        msg.add("page", page);
        List<AgentMemorandum> agentMemorandums = agentMemorandumService.loadMemorandumBySchoolIdPage(schoolId, page, memorandumType);
        if (CollectionUtils.isEmpty(agentMemorandums)) {
            msg.add("isOver", true);
        } else {
            msg.add("list", createSchoolMemorandumPage(agentMemorandums, memorandumType));
        }
        return msg;
    }

    @RequestMapping(value = "find_teacher_memorandum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findTeacherMemorandum() {
        Long teacherId = getRequestLong("teacherId");
        Integer page = getRequestInt("page");
        if (page == 0) {
            page = 1;
        }
        MemorandumType memorandumType = translateType();
        List<AgentMemorandum> agentMemorandums = agentMemorandumService.loadMemorandumByTeacherIdPage(teacherId, page, memorandumType);
        MapMessage msg = MapMessage.successMessage();
        msg.add("page", page);
        if (CollectionUtils.isEmpty(agentMemorandums)) {
            msg.add("isOver", true);
        } else {
            msg.add("list", createSchoolMemorandumPage(agentMemorandums, MemorandumType.TEXT));
        }
        return msg;
    }


    // 分页查询
    @RequestMapping(value = "find_user_memorandum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findUserMemorandum() {
        Integer page = getRequestInt("page");
        if (page == 0) {
            page = 1;
        }
        String month = getRequestString("month");
        Date startTime = null;
        Date endTime = null;
        if (StringUtils.isNotBlank(month)) {
            Date selectedTime = DateUtils.stringToDate(month, DateUtils.FORMAT_YEAR_MONTH);
            startTime = DayUtils.getFirstDayOfMonth(selectedTime);
            Date endDate = DayUtils.getLastDayOfMonth(selectedTime);
            endTime = endDate == null ? null : DateUtils.getDayEnd(endDate);
        }
        MemorandumType memorandumType = translateType();
        List<AgentMemorandum> agentMemorandums = agentMemorandumService.loadMemorandumByUserIdPage(getCurrentUserId(), page, memorandumType, startTime, endTime);
        MapMessage msg = MapMessage.successMessage();
        msg.add("page", page);
        if (CollectionUtils.isEmpty(agentMemorandums)) {
            msg.add("isOver", true);
        } else {
            msg.add("list", createUserMemorandumPage(agentMemorandums));
        }
        return msg;
    }

    // 添加备忘录页
    @RequestMapping(value = "add_memorandum_page.vpage", method = RequestMethod.GET)
    public String addMemorandumPage(Model model) {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("teacherId", teacherId);
        return "rebuildViewDir/mobile/memorandum/add_memorandum";
    }

    // 添加备忘录
    @RequestMapping(value = "add_memorandum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addMemorandum() {
        MemorandumType memorandumType = translateType();
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        String url = getRequestString("url");
        String content = StringUtils.filterEmojiForMysql(getRequestString("content"));
        return agentMemorandumService.addMemorandum(getCurrentUserId(), schoolId, teacherId, content, memorandumType,url);
    }

    // 删除备忘录
    @RequestMapping(value = "delete_memorandum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMemorandum() {
        String id = getRequestString("id");
        return agentMemorandumService.deleteMemorandum(getCurrentUserId(), id);
    }

    // 修改备忘录页
    @RequestMapping(value = "update_memorandum_page.vpage", method = RequestMethod.GET)
    public String updateMemorandumPage(Model model) {
        String id = getRequestString("id");
        model.addAttribute("memorandum", agentMemorandumService.load(id));
        return "rebuildViewDir/mobile/memorandum/memorandum_detail";
    }

    // 修改备忘录
    @RequestMapping(value = "update_memorandum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateMemorandum() {
        String id = getRequestString("id");
        String content = StringUtils.filterEmojiForMysql(getRequestString("content"));
        return agentMemorandumService.updateMemorandum(getCurrentUserId(), id, content);
    }

    private List<MemorandumPage> createSchoolMemorandumPage(List<AgentMemorandum> memorandums, MemorandumType type) {
        List<MemorandumPage> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(memorandums) || type == null) {
            return result;
        }
        Map<String, List<AgentMemorandum>> memorandumsMap = memorandums.stream().filter(p -> p.getWriteTime() != null).collect(Collectors.groupingBy(p -> DateUtils.dateToString(p.getWriteTime(), DateUtils.FORMAT_YEAR_MONTH), Collectors.toList()));
        Set<Long> userIds = memorandums.stream().map(AgentMemorandum::getCreateUserId).filter(p -> !Objects.equals(getCurrentUserId(), p)).collect(Collectors.toSet());
        Map<Long, AgentUser> agentUserMap = agentUserLoaderClient.findByIds(userIds);
        memorandumsMap.forEach((k, v) -> {
            MemorandumPage page = new MemorandumPage();
            page.setOrderFiled(k);
            List<Map<String, String>> infos = new ArrayList<>();
            v.forEach(p -> {
                Map<String, String> info = new HashMap<>();
                info.put("id", p.getId());
                if (type == MemorandumType.TEXT) {
                    if (agentUserMap.containsKey(p.getCreateUserId())) {
                        info.put("userName", agentUserMap.get(p.getCreateUserId()).getRealName());
                    }
                }
                info.put("content", p.getContent());
                info.put("time", DateUtils.dateToString(p.getWriteTime(), FORMAT_TIME));
                info.put("isIntoSchool", p.getIntoSchoolRecordId());
                infos.add(info);
            });
            infos.sort((o1, o2) -> DateUtils.stringToDate(o2.get("time"), FORMAT_TIME).compareTo(DateUtils.stringToDate(o1.get("time"), FORMAT_TIME)));
            page.setInfo(infos);
            result.add(page);
        });
        result.sort((o1, o2) -> o2.getOrderFiled().compareTo(o1.getOrderFiled()));
        return result;
    }

    private List<MemorandumPage> createUserMemorandumPage(List<AgentMemorandum> memorandums) {
        List<MemorandumPage> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(memorandums)) {
            return result;
        }
        Set<Long> schoolId = memorandums.stream().map(AgentMemorandum::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolId)
                .getUninterruptibly();
        Set<Long> teacherId = memorandums.stream().map(AgentMemorandum::getTeacherId).collect(Collectors.toSet());
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherId);
        Map<String, List<AgentMemorandum>> memorandumsMap = memorandums.stream().filter(p -> p.getWriteTime() != null).collect(Collectors.groupingBy(p -> DateUtils.dateToString(p.getWriteTime(), DateUtils.FORMAT_YEAR_MONTH), Collectors.toList()));
        memorandumsMap.forEach((k, v) -> {
            MemorandumPage page = new MemorandumPage();
            page.setOrderFiled(k);
            List<Map<String, String>> infos = new ArrayList<>();
            v.forEach(p -> {
                Map<String, String> info = new HashMap<>();
                info.put("id", p.getId());
                if (p.getTeacherId() != null && teacherMap.containsKey(p.getTeacherId())) {
                    info.put("target", teacherMap.get(p.getTeacherId()).getProfile().getRealname());
                    info.put("targetType", "teacher");
                    info.put("targetId", SafeConverter.toString(p.getTeacherId()));
                } else if (p.getSchoolId() != null && schoolMap.containsKey(p.getSchoolId())) {
                    info.put("target", schoolMap.get(p.getSchoolId()).loadSchoolFullName());
                    info.put("targetType", "school");
                    info.put("targetId", SafeConverter.toString(p.getSchoolId()));
                }
                info.put("content", p.getContent());
                info.put("time", DateUtils.dateToString(p.getWriteTime(), FORMAT_TIME));
                infos.add(info);
            });
            infos.sort((o1, o2) -> o2.get("time").compareTo(o1.get("time")));
            page.setInfo(infos);
            result.add(page);
        });
        result.sort((o1, o2) -> o2.getOrderFiled().compareTo(o1.getOrderFiled()));
        return result;
    }

    private MemorandumType translateType() {
        String type = getRequestString("type");
        if (Objects.equals(type, "text")) {
            return MemorandumType.TEXT;
        }
        if (Objects.equals(type, "picture")) {
            return MemorandumType.PICTURE;
        }
        if (Objects.equals(type, "image_text")) {
            return MemorandumType.IMAGE_TEXT;
        }
        return null;
    }
}
