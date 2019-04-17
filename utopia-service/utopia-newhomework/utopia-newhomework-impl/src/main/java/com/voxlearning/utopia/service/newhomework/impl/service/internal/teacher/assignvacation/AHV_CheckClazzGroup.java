package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by tanguohong on 2016/11/29.
 */
@Named
public class AHV_CheckClazzGroup extends SpringContainerSupport implements AssignVacationHomeworkTask {

    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Override
    public void execute(AssignVacationHomeworkContext context) {
        Long teacherId = context.getTeacher().getId();
        String groupBookMapText = JsonUtils.toJson(context.getSource().get("clazzBookMap"));
        if (StringUtils.isBlank(groupBookMapText)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST,
                    "mod3", groupBookMapText,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("clazzIds is blank");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }
        Map<String, Object> groupBookMap = JsonUtils.fromJson(groupBookMapText);

        if (groupBookMap == null || groupBookMap.isEmpty()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST,
                    "mod3", groupBookMapText,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("请选择要布置的班级");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }

        Map<Long, String> groupBookIdMap = new HashMap<>();
        Map<Long, String> groupSubjectMap = new HashMap<>();
        Map<Long, List<Long>> clazzGroupsMap = new LinkedHashMap<>();

        for (String idText : groupBookMap.keySet()) {
            String[] text = StringUtils.split(idText, "_");
            if (text.length == 2) {
                Long clazzId = ConversionUtils.toLong(text[0]);
                Long groupId = ConversionUtils.toLong(text[1]);
                List<Long> groups = clazzGroupsMap.computeIfAbsent(clazzId, k -> new ArrayList<>());
                if (groupId != 0) {
                    groups.add(groupId);
                }
                Map<String, Object> groupMap = JsonUtils.fromJson(JsonUtils.toJson(groupBookMap.get(idText)));
                if (groupMap != null) {
                    if (!groupMap.containsKey("bookId")) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getTeacher().getId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_BOOK_ID_IS_NULL,
                                "mod3", context.getSource(),
                                "op", "assign vacation homework"
                        ));
                        context.errorResponse("Teacher {} has no group {}", teacherId, groupId);
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_ID_IS_NULL);
                        context.setTerminateTask(true);
                        return;
                    }
                    if (!groupMap.containsKey("subject") || StringUtils.isBlank(SafeConverter.toString(groupMap.get("subject")))) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getTeacher().getId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
                                "mod3", context.getSource(),
                                "op", "assign vacation homework"
                        ));
                        context.errorResponse("Teacher {} has no subject {}", teacherId, context.getSource());
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
                        context.setTerminateTask(true);
                        return;
                    }

                    groupBookIdMap.put(groupId, groupMap.get("bookId").toString());
                    groupSubjectMap.put(groupId, groupMap.get("subject").toString());
                }
            } else if (text.length == 1) {
                Long clazzId = ConversionUtils.toLong(text[0]);
                clazzGroupsMap.put(clazzId, null);
            }
        }

        Set<ClazzGroup> clazzGroups = ClazzGroup.newSet(clazzGroupsMap);
        context.getClazzGroups().addAll(clazzGroups);
        context.setGroupBookIdMap(groupBookIdMap);
        context.setGroupSubjectMap(groupSubjectMap);

        //验证每个班组是否属于老师
        Map<Long, Long> groupTeacherMap = new HashMap<>();
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadGroupSingleTeacher(groupBookIdMap.keySet());
        for (ClazzGroup clazzGroup : clazzGroups) {
            Long groupId = clazzGroup.getGroupId();
            Teacher teacher = teacherMap.get(clazzGroup.getGroupId());
            Boolean hasRelTeacher = teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, groupId);

            if (!hasRelTeacher) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION,
                        "mod3", context.getSource(),
                        "op", "assign vacation homework"
                ));
                context.errorResponse("Teacher {} has no group {}", teacherId, groupId);
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION);
                context.setTerminateTask(true);
                return;
            }
            if (teacher == null) {
                context.errorResponse("group {} has no Teacher {}", groupId, teacherId);
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST);
                context.setTerminateTask(true);
                return;
            }
            groupTeacherMap.put(groupId, teacher.getId());
        }
        context.setGroupTeacherMap(groupTeacherMap);

        // 验证每个分组是否有学生
        for (ClazzGroup clazzGroup : clazzGroups) {
            Long clazzId = clazzGroup.getClazzId();
            Long groupId = clazzGroup.getGroupId();
            List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(groupId);
            if (CollectionUtils.isEmpty(studentIds)) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_STUDENT_NOT_EXIST,
                        "mod3", context.getSource(),
                        "op", "assign vacation homework"
                ));
                context.errorResponse("Clazz/group {}/{} has no students, non-assignable", clazzId, groupId);
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_STUDENT_NOT_EXIST);
                context.setTerminateTask(true);
                return;
            }
        }
    }
}
