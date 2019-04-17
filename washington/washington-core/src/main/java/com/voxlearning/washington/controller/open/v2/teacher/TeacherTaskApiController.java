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

package com.voxlearning.washington.controller.open.v2.teacher;

/**
 * Teacher Task Api Controller
 * Created by alex on 2017/1/3.
 */

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupClazzMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.api.mappers.TeacherApplicationMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@RequestMapping(value = "/v2/teacher")
@Slf4j
public class TeacherTaskApiController extends AbstractTeacherApiController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    /**
     * 老师新手任务和待办事项列表，待办事项列表优先
     *
     * @return 老师新手任务和待办事项列表
     */
    @RequestMapping(value = "/task/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getTaskList() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);

        Teacher teacher = getCurrentTeacher();

        // 待办事项
        List<Map<String, Object>> teacherApplications = getApplicationList(teacher);
        if (CollectionUtils.isNotEmpty(teacherApplications)) {
            resultMap.add(RES_APPLICATION_TITLE, "你有" + teacherApplications.size() + "个班级事情待处理");
            resultMap.add(RES_APPLICATION_CONTENT, "马上处理");
            resultMap.add(RES_APPLICATION_LIST, teacherApplications);
        }

        //新手任务  判断版本  大于等于1.5.5不显示
        if (VersionUtil.compareVersion(appVersion, "1.5.5.0") < 0) {
            List<Map<String, Object>> newUserTasks = getNewUserTaskList(teacher);
            if (CollectionUtils.isNotEmpty(newUserTasks)) {
                resultMap.add(RES_FRESHMAN_TASK_LIST, newUserTasks);
            }
        }
        return resultMap;

    }

    // 获取老师的待办任务列表
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
                        appMap.put(RES_APPLICATION_TITLE, generateApplicationTitle(app.getApplicantName()));
                        appMap.put(RES_APPLICATION_CONTENT, generateApplicationContent(app));
                        appMap.put(RES_APPLICATION_APP_DATE, app.getDate().getTime());
                        appMap.put(RES_APPLICATION_CONFIRM_TEXT, generateApplicationConfirmText(app));
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
                    appMap.put(RES_APPLICATION_TITLE, generateApplicationTitle(teacher.getProfile().getRealname()));
                    appMap.put(RES_APPLICATION_CONTENT, generateExitClazzContent(exitClazz));
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

    private List<Map<String, Object>> getNewUserTaskList(Teacher teacher) {
        List<Map<String, Object>> retTaskList = new ArrayList<>();

        Set<Long> subTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        Map<Long, List<GroupTeacherMapper>> teacherGroupsMap = deprecatedGroupLoaderClient.loadTeacherGroups(subTeacherIds, true);

        List<GroupTeacherMapper> groupTeacherMappers = new LinkedList<>();
        for (Map.Entry<Long, List<GroupTeacherMapper>> entry : teacherGroupsMap.entrySet()) {
            Long teacherId = entry.getKey();
            List<GroupTeacherMapper> groupTeacherMapperList = entry.getValue();
            //过滤掉老师不再教的分组
            groupTeacherMappers.addAll(groupTeacherMapperList.stream().filter(t -> t.isTeacherGroupRefStatusValid(teacherId)).collect(Collectors.toList()));
        }

        if (CollectionUtils.isEmpty(groupTeacherMappers)) {
            return retTaskList;
        }

        List<Long> clazzIdList = groupTeacherMappers.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toList());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdList)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        Long teacherSchoolId = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId())
                .getUninterruptibly()
                .getId();
        for (GroupTeacherMapper groupTeacherMapper : groupTeacherMappers) {
            Long clazzId = groupTeacherMapper.getClazzId();
            Clazz clazz = clazzMap.get(clazzId);
            if (clazz == null || clazz.isTerminalClazz() || !teacherSchoolId.equals(clazz.getSchoolId()))//过滤掉不属于该老师当前学校的班级.因为存在老师换学校的情况.
                continue;
            if (groupTeacherMapper.getStudents().size() <= 5) {
                Map<String, Object> taskInfo = new LinkedHashMap<>();
                taskInfo.put("title", "你的班级人数过低");
                taskInfo.put("content", "邀请学生加入");
                taskInfo.put("linkUrl", fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/invite_student");
                retTaskList.add(taskInfo);
                break;
            }
        }

        return retTaskList;
    }

    // 生成退出班级提醒文案
    private String generateExitClazzContent(GroupClazzMapper exitClazz) {
        String template = "你已经退出了{0}（{1}），{2}名学生还没有找到新老师！";
        return MessageFormat.format(template, exitClazz.getClazzName(), exitClazz.getGroupSubject().getValue(), exitClazz.getStudentCount());
    }

    // 根据申请类型生成申请文案
    private String generateApplicationTitle(String teacherName) {
        if (StringUtils.isBlank(teacherName)) {
            return "未知";
        }

        String title = teacherName;

        if (!title.endsWith("老师")) {
            title = title + "老师";
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

    // 根据申请生成确认文案
    private String generateApplicationConfirmText(TeacherApplicationMapper app) {
        String content = "";
        switch (app.getType()) {
            case "LINK":
                content = "允许此请求，你们将一起教这个班。确定？";
                break;
            case "TRANSFER":
                content = "允许此请求，你将接管这个班级。确定？";
                break;
            case "REPLACE":
                content = "允许此请求，你将离开这个班级。确定？";
                break;
        }
        return content;
    }

}
