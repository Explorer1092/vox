/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupClazzMapper;
import com.voxlearning.utopia.service.user.api.mappers.TeacherApplicationMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Rui Bao
 * @since 2013-03-23 01:40
 */
@Controller
@RequestMapping("/teacher/task")
public class TeacherTaskController extends AbstractTeacherController {

    @RequestMapping(value = "execute.vpage", method = RequestMethod.POST)
    public String executetask(@RequestBody String taskType) {
        return null;
    }

    /**
     * 待办事项列表优先
     */
    @RequestMapping(value = "/list.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getTaskList() {

        String ver = getRequestString("ver");
        String sys = getRequestString("sys");

        // 待办事项
        List<Map<String, Object>> teacherApplications = getApplicationList(currentTeacher());
        if (CollectionUtils.isNotEmpty(teacherApplications)) {
            MapMessage message = MapMessage.successMessage();
            message.add("application_title", "你有" + teacherApplications.size() + "个班级事情待处理");
            message.add("application_content", "马上处理");
            return message;
        }
        return MapMessage.errorMessage();
    }


    private List<Map<String, Object>> getApplicationList(Teacher teacher) {
        List<Map<String, Object>> retApplications = new ArrayList<>();

        if (teacher == null) return retApplications;
        TeacherSystemClazzService.TeacherToDoList data = teacherSystemClazzServiceClient.loadTeacherToDoList(teacher.getId());
        if (data == null) {
            return retApplications;
        }

        // receive applications
        Map<String, List<TeacherApplicationMapper>> recvApplications = data.getReceivedApplications();
        if (MapUtils.isNotEmpty(recvApplications)) {
            List<Map<String, Object>> applicationsList = new ArrayList<>();
            recvApplications.keySet().forEach(key -> {
                List<TeacherApplicationMapper> appList = recvApplications.get(key);
                if (CollectionUtils.isNotEmpty(appList)) {
                    appList.forEach(app -> {
                        Map<String, Object> appMap = new LinkedHashMap<>();
                        appMap.put(RES_APPLICATION_ID, app.getId());
                        appMap.put(RES_APPLICATION_TYPE, app.getType());
                        appMap.put(RES_APPLICATION_TITLE, generateApplicationTitle(app.getApplicantName(), app.getApplicantSubject()));
                        appMap.put(RES_APPLICATION_CONTENT, generateApplicationContent(app));
                        appMap.put(RES_APPLICATION_APP_DATE, app.getDate().getTime());
                        applicationsList.add(appMap);
                    });
                }
            });
            retApplications.addAll(applicationsList.stream().
                    sorted((o1, o2) -> Long.compare(SafeConverter.toLong(o2.get(RES_APPLICATION_APP_DATE)), SafeConverter.toLong(o1.get(RES_APPLICATION_APP_DATE)))).
                    collect(Collectors.toList()));
        }

        final Map<String, Map<String, List<TeacherApplicationMapper>>> clazzId2ApplicationMap = data.getSendApplications() == null ? new HashMap<>() : data.getSendApplications();

        List<GroupClazzMapper> exitClazzsList = data.getToBeTransferedClazzs();
        if (CollectionUtils.isNotEmpty(exitClazzsList)) {
            exitClazzsList.forEach(exitClazz -> {
                if (!clazzId2ApplicationMap.containsKey(String.valueOf(exitClazz.getClazzId()))) {
                    Map<String, Object> appMap = new LinkedHashMap<>();
                    appMap.put(RES_APPLICATION_ID, 0);
                    appMap.put(RES_APPLICATION_TYPE, "EXITCLAZZ");
                    appMap.put(RES_APPLICATION_TITLE, generateApplicationTitle(teacher.getProfile().getRealname(), teacher.getSubject()));
                    appMap.put(RES_APPLICATION_APP_DATE, 0L);
                    appMap.put(RES_EXITS_CLAZZ_ID, exitClazz.getClazzId());
                    appMap.put(RES_EXITS_GROUP_ID, exitClazz.getGroupId());
                    appMap.put(RES_EXITS_CLAZZ_NAME, exitClazz.getClazzName());
                    appMap.put(RES_SUBJECT, exitClazz.getGroupSubject().name());
                    appMap.put(RES_SUBJECT_NAME, exitClazz.getGroupSubject().getValue());
                    appMap.put(RES_APPLICATION_CONFIRM_TEXT, "");
                    retApplications.add(appMap);
                }
            });
        }

        return retApplications;
    }

    // 根据申请类型生成申请文案
    private String generateApplicationTitle(String teacherName, Subject subject) {
        if (StringUtils.isBlank(teacherName)) {
            return "未知";
        }

        String title = teacherName;

        if (!title.endsWith("老师")) {
            title = title + "老师";
        }

        if (subject != null) {
            title = title + "(" + subject.getValue() + ")";
        }

        return title;
    }

    // 根据申请类型生成申请文案
    private String generateApplicationContent(TeacherApplicationMapper app) {
        String templateLink = "申请一起教{0}，给学生布置作业";
        String templateTransfer = "申请转让{0}给你";
        String templateReplace = "申请接管{0}，若允许，你将离开班级";

        String content = "";
        switch (app.getType()) {
            case "LINK":
                content = MessageFormat.format(templateLink, app.getClazzName());
                break;
            case "TRANSFER":
                content = MessageFormat.format(templateTransfer, app.getClazzName());
                break;
            case "REPLACE":
                content = MessageFormat.format(templateReplace, app.getClazzName());
                break;
        }
        return content;
    }
}
