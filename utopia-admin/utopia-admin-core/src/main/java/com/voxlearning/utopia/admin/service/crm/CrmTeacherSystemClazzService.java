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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetition;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.content.consumer.ContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.ContentServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService.TeacherGroupChangeRecord;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSystemClazzServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;

/**
 * @author changyuan.liu
 * @since 2015.12.13
 */
@Named
public class CrmTeacherSystemClazzService extends AbstractAdminService {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject private ContentLoaderClient contentLoaderClient;
    @Inject private ContentServiceClient contentServiceClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;

    public MapMessage changeTeacherClazzLevel(Long groupId, ClazzLevel clazzLevel) {
        MapMessage mapMessage = teacherSystemClazzServiceClient.changeTeacherClazzLevel(groupId, clazzLevel);
        if (mapMessage.isSuccess()) {
            List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupId);
            if (CollectionUtils.isEmpty(teachers)) {
                return mapMessage;
            }
            // 更换班组关系成功,此时需要更新作业等clazz id
            // 即使中间有错误,也保证作业等数据一致
            @SuppressWarnings("unchecked")
            List<TeacherGroupChangeRecord> records = (List<TeacherGroupChangeRecord>) mapMessage.remove("records");
            if (records == null) {
                return mapMessage;
            }
            List<Long> teacherIds = records.stream().map(TeacherSystemClazzService.TeacherGroupChangeRecord::getTeacherId).collect(Collectors.toList());
            Map<Long, Teacher> recordTeachers = teacherLoaderClient.loadTeachers(teacherIds);
            for (TeacherGroupChangeRecord record : records) {
                Teacher t = recordTeachers.get(record.getTeacherId());
                if (t == null) {
                    continue;
                }
                updateBusinessForTeacherMoveClazz(t.getId(), t.getSubject(),
                        record.getOldClazzId(), record.getNewClazzId(), record.getGroupId());
            }
        }
        return mapMessage;
    }

    /**
     * 修改老师学校
     *
     * @param teacherId
     * @param teacherGroupIds
     * @param schoolId
     * @return
     */
    @SuppressWarnings("unchecked")
    public MapMessage changeTeacherSchool(Long teacherId, Collection<Long> teacherGroupIds, Long schoolId, String operator) {
        MapMessage mapMessage = teacherSystemClazzServiceClient.changeTeacherSchool(teacherId, teacherGroupIds, schoolId, operator, OperationSourceType.crm);
        // 无论成功失败,都要根据已有的records信息,更新作业等业务数据的班级id
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return mapMessage;
        }
        // 更换班组关系成功,此时需要更新作业等clazz id
        // 即使中间有错误,也保证作业等数据一致
        List<TeacherGroupChangeRecord> records = (List<TeacherGroupChangeRecord>) mapMessage.remove("records");
        if (CollectionUtils.isNotEmpty(records)) {
            for (TeacherGroupChangeRecord record : records) {
                updateBusinessForTeacherMoveClazz(teacherId, teacher.getSubject(),
                        record.getOldClazzId(), record.getNewClazzId(), record.getGroupId());
            }
        }
        return mapMessage;
    }

    /**
     * 修改老师学科
     *
     * @param teacherId
     * @param subject
     * @param operator
     * @return
     */
    public MapMessage changeTeacherSubject(Long teacherId, Subject subject, String operator) {

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("不存在此老师");
        }

        // 自动检查作业
        // 否则是原有科目作业，不合逻辑
        deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false).forEach(g -> {
            List<String> homeworkIds = newHomeworkLoaderClient.loadGroupHomeworks(g.getId(), g.getSubject())
                    .unchecked()
                    .originalLocationsAsList()
                    .stream()
                    .map(NewHomework.Location::getId)
                    .collect(Collectors.toList());
            homeworkIds.forEach(id -> {
                newHomeworkServiceClient.checkHomework(teacher, id, HomeworkSourceType.CRM);
            });
        });

        // 修改学科
        MapMessage message = teacherSystemClazzServiceClient.changeTeacherSubject(teacherId, subject, operator, OperationSourceType.crm);
        if (!message.isSuccess()) {
            return message;
        }

        // 处理大使
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacherId)
                .stream().findFirst().orElse(null);
        if (ref != null) {
            // 如果是大使 直接辞任
            MapMessage msg = businessTeacherServiceClient.resignationAmbassador(teacher);
            if (!msg.isSuccess()) {
                return message;
            }
            // 给大使发右下角弹窗
            String content = "您已经换学科，系统自动取消了您的大使身份。";
            userPopupServiceClient.createPopup(teacherId).content(content).type(PopupType.AMBASSADOR_NOTICE).category(LOWER_RIGHT).create();
        }

        // 如果有预备大使记录  删除
        AmbassadorCompetition competition = ambassadorLoaderClient.getAmbassadorLoader().loadTeacherAmbassadorCompetition(teacherId);
        if (competition != null) {
            ambassadorServiceClient.getAmbassadorService().$disableAmbassadorCompetition(competition.getId());
        }

        //转换学科后重新初始化教材
        List<GroupTeacherMapper> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false);
        for (GroupMapper teacherGroup : teacherGroups) {
            Long clazzId = teacherGroup.getClazzId();
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                continue;
            }
            ExRegion region = userLoaderClient.loadUserRegion(teacher);
            Long bookId = contentLoaderClient.getExtension().initializeClazzBook(
                    teacher.getSubject(), clazz.getClazzLevel().getLevel(), region.getCode(),
                    new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
            ChangeBookMapper cbm = new ChangeBookMapper();
            cbm.setType(0);
            cbm.setBooks(String.valueOf(bookId));
            cbm.setClazzs(String.valueOf(clazzId));
            try {
                contentServiceClient.setClazzBook(teacher, cbm);
            } catch (Exception ignored) {
                logger.warn("Failed to set clazz books [bookIds={},clazzIds={}]", cbm.getBooks(), cbm.getClazzs(), ignored);
            }
        }

        return message;
    }

    /**
     * 合并学校
     *
     * @param sourceSchoolId
     * @param targetSchoolId
     * @param operatorId
     * @return
     */
    public MapMessage mergeSchool(Long sourceSchoolId, Long targetSchoolId, String operatorId, String desc) {
        MapMessage message = teacherSystemClazzServiceClient.mergeSchoolWithDesc(sourceSchoolId, targetSchoolId, operatorId, desc);
        // 更换班组关系成功,此时需要更新作业等clazz id
        // 即使中间有错误,也保证作业等数据一致
        @SuppressWarnings("unchecked")
        List<TeacherGroupChangeRecord> records = (List<TeacherGroupChangeRecord>) message.remove("records");
        if (records != null) {
            Set<Long> teacherIds = records.stream().map(TeacherSystemClazzService.TeacherGroupChangeRecord::getTeacherId).collect(Collectors.toSet());
            Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
            for (TeacherGroupChangeRecord record : records) {
                Teacher teacher = teachers.get(record.getTeacherId());
                if (teacher == null) {
                    continue;
                }
                updateBusinessForTeacherMoveClazz(teacher.getId(), teacher.getSubject(),
                        record.getOldClazzId(), record.getNewClazzId(), record.getGroupId());
            }
        }
        if (message.isSuccess()) {
            // 处理校园大使部分
            List<AmbassadorSchoolRef> sourceRefList = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorRefs(sourceSchoolId);
            if (CollectionUtils.isNotEmpty(sourceRefList)) {
                for (AmbassadorSchoolRef sourceRef : sourceRefList) {
                    // 根据学科处理
                    Teacher sourceAmbassador = teacherLoaderClient.loadTeacher(sourceRef.getAmbassadorId());
                    AmbassadorSchoolRef targetRef = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(sourceAmbassador.getSubject(), targetSchoolId);
                    // 目标学校不存在同科大使 直接将原学校大使 迁移过来
                    if (targetRef == null) {
                        try {
                            message = ambassadorServiceClient.getAmbassadorService().changeAmbassadorSchool(sourceAmbassador.getSubject(), sourceRef, targetSchoolId);
                        } catch (Exception ex) {
                            logger.error("Failed to change ambassador school", ex);
                            message = MapMessage.errorMessage("修改大使学校失败");
                        }
                        if (!message.isSuccess()) {
                            return message;
                        }
                    } else {
                        // 目标学校有大使 直接取消原来学校的大使
                        try {
                            message = ambassadorServiceClient.getAmbassadorService().disableAmbassador(sourceAmbassador.getSubject(), sourceRef, targetSchoolId);
                        } catch (Exception ex) {
                            logger.error("Failed to change ambassador school", ex);
                            message = MapMessage.errorMessage("修改大使学校失败");
                        }
                        if (!message.isSuccess()) {
                            return message;
                        } else {
                            // 给大使发右下角弹窗
                            String content = "由于合并学校需要，系统自动取消了您的大使身份，您还可以申请成为预备大使。";
                            userPopupServiceClient.createPopup(sourceRef.getAmbassadorId()).content(content).type(PopupType.AMBASSADOR_NOTICE).category(LOWER_RIGHT).create();
                        }
                    }
                }
            }
        }
        return message;
    }

    /**
     * 导入学生
     *
     * @param groupId 导入老师分组
     * @param content 导入学生明细
     */
    public MapMessage importStudents(Long teacherId, Long groupId, String content) {
        // 读取分组所在班级
        GroupMapper group = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        if (group == null) {
            return MapMessage.errorMessage("分组不存在");
        }

        // 分组班级
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(group.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }

        // 失败结果集
        List<Map<String, Object>> failedResults = new LinkedList<>();

        // 学生id集合
        Set<Long> studentIds = new HashSet<>();

        for (String studentStr : content.split("\\n")) {
            Long studentId = SafeConverter.toLong(studentStr);
            if (studentId > 0L) {
                studentIds.add(studentId);
            } else {
                failedResults.add(MapUtils.m("id", studentStr, "reason", "学生ID不合法"));
            }
        }

        // 读取学生
        Map<Long, Student> students = studentLoaderClient.loadStudents(studentIds);

        // 读取学生班级
        Map<Long, Clazz> studentClazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazzs(studentIds);

        // 转入成功学生数
        int successCount = 0;

        // 循环
        for (Long studentId : studentIds) {
            Student student = students.get(studentId);
            if (student == null) {
                failedResults.add(MapUtils.m("id", studentId, "reason", "学生ID不存在"));
                continue;
            }

            Clazz oldClazz = studentClazzMap.get(student.getId());
            MapMessage msg;
            if (oldClazz != null) {
                if (!Objects.equals(oldClazz.getSchoolId(), clazz.getSchoolId())) {
                    failedResults.add(MapUtils.m("id", studentId, "reason", "不允许跨校换班"));
                    continue;
                }

                if (oldClazz.getClazzLevel() != clazz.getClazzLevel()) {
                    failedResults.add(MapUtils.m("id", studentId, "reason", "不允许跨年级换班"));
                    continue;
                }

                // 班级相同
                // 判断是添加老师还是更换老师
                if (Objects.equals(oldClazz.getId(), clazz.getId())) {
                    // 读取学生当前的分组
                    List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);

                    // 判断是否有相同学科，没有执行添加操作
                    boolean subjectMatch = groupMappers.stream().anyMatch(g -> g.getSubject() == group.getSubject());
                    if (subjectMatch) {
                        // 如果有相同学科，退出原分组，加入到当前组
                        msg = groupServiceClient.moveStudentsBetweenGroup(groupMappers.get(0).getId(), groupId, Collections.singleton(studentId));
                    } else {
                        // 添加分组关联老师
                        msg = groupServiceClient.linkStudentTeacher(studentId, teacherId, clazz.getId(), true, ClazzConstants.MAX_CLAZZ_CAPACITY, OperationSourceType.crm);
                    }
                } else {
                    // 读取学生当前的分组
                    List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);

                    // 班级不同,直接更换操作
                    msg = groupServiceClient.moveStudentsBetweenGroup(groupMappers.get(0).getId(), groupId, Collections.singleton(studentId));
                }
            } else {// 班级不存在,直接进班
                msg = clazzServiceClient.studentJoinSystemClazz(studentId, clazz.getId(), teacherId, true, OperationSourceType.crm);
            }

            if (!msg.isSuccess()) {
                failedResults.add(MapUtils.m("id", studentId, "reason", "导入错误"));
            } else {
                successCount++;
            }

        }

        return MapMessage.successMessage()
                .add("successCount", successCount)
                .add("clazzName", clazz.formalizeClazzName())
                .add("failedResults", failedResults);
    }

    /**
     * 更新除用户班组关系外的其他业务
     *
     * @param teacherId  老师ID
     * @param subject    学科
     * @param oldClazzId 旧班级ID
     * @param newClazzId 新班级ID
     * @param groupId    班组ID
     */
    public void updateBusinessForTeacherMoveClazz(Long teacherId, Subject subject, Long oldClazzId, Long newClazzId, Long groupId) {
        // TODO 将所有作业按分组查询，更新作业数据
//        homeworkServiceClient.updateHomeworkDataForSystemClazzMove(teacherId, subject, oldClazzId, newClazzId, groupId);//旧作业体系
        String sql;
        if (teacherId != null) {
            // TODO 其他数据
            sql = "UPDATE IGNORE VOX_STUDENT_HOMEWORK_STAT a SET UPDATE_DATETIME=NOW(), CLAZZ_ID=? WHERE TEACHER_ID=? AND CLAZZ_ID=?";
            utopiaSqlHomework.withSql(sql).useParamsArgs(newClazzId, teacherId, oldClazzId).executeUpdate();

            sql = "UPDATE VOX_FLOWER a SET UPDATE_DATETIME=NOW(), CLAZZ_ID=? WHERE RECEIVER_ID=? AND CLAZZ_ID=?";
            utopiaSql.withSql(sql).useParamsArgs(newClazzId, teacherId, oldClazzId).executeUpdate();
        }
        sql = "UPDATE VOX_CLAZZ_INTEGRAL_POOL a SET UPDATE_DATETIME=NOW(), CLAZZ_ID=? WHERE CLAZZ_GROUP_ID=?";
        utopiaSql.withSql(sql).useParamsArgs(newClazzId, groupId).executeUpdate();

        sql = "UPDATE VOX_CLAZZ_INTEGRAL_POOL_HISTORY a SET UPDATE_DATETIME=NOW(), CLAZZ_ID=? WHERE CLAZZ_GROUP_ID=?";
        utopiaSql.withSql(sql).useParamsArgs(newClazzId, groupId).executeUpdate();

        sql = "UPDATE VOX_SMARTCLAZZ_INTRGRAL_HISTORY a SET UPDATE_DATETIME=NOW(), CLAZZ_ID=? WHERE CLAZZ_GROUP_ID=?";
        utopiaSql.withSql(sql).useParamsArgs(newClazzId, groupId).executeUpdate();

        sql = "UPDATE VOX_SMARTCLAZZ_QUESTION_REF a SET UPDATE_DATETIME=NOW(), CLAZZ_ID=? WHERE CLAZZ_GROUP_ID=?";
        utopiaSql.withSql(sql).useParamsArgs(newClazzId, groupId).executeUpdate();
    }

    /**
     * 修改老师学校，老师不带走原先班级
     *
     * @param teacherId 老师ID
     * @param schoolId  学校ID
     */
    public MapMessage changeTeacherSchoolNotCarryOldClazz(Long teacherId, Long schoolId) {
        return teacherSystemClazzServiceClient.changeTeacherSchoolNotCarryOldClazz(teacherId, schoolId);
    }
}
