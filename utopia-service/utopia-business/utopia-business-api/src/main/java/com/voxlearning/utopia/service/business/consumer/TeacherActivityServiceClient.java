/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.business.api.TeacherActivityService;
import com.voxlearning.utopia.entity.activity.TeacherNewTermActivityProgress;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class TeacherActivityServiceClient {

    @Getter
    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService remoteReference;

    public MapMessage participate(Long schoolId, Long teacherId) {
        if (teacherId == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .expirationInSeconds(30)
                    .keyPrefix("TeacherActivityService:participate")
                    .keys(teacherId)
                    .callback(() -> remoteReference.participate(schoolId, teacherId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复操作");
        }
    }

    public MapMessage participateActivity(Long activityId, Long schoolId, Long teacherId) {
        if (teacherId == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .expirationInSeconds(30)
                    .keyPrefix("TeacherActivityService:participateActivity")
                    .keys(activityId, teacherId)
                    .callback(() -> remoteReference.participateActivity(activityId, schoolId, teacherId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复操作");
        }
    }

    public MapMessage participateTuckerActivity(Long schoolId, Long teacherId) {
        if (teacherId == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .expirationInSeconds(30)
                    .keyPrefix("TeacherActivityService:participateTuckerActivity")
                    .keys(teacherId)
                    .callback(() -> remoteReference.participateTuckerActivity(schoolId, teacherId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复操作");
        }
    }


    //==========================================================
    //===============         七巧板活动        =================
    //==========================================================

    public MapMessage addTangramStudent(Long teacherId,
                                        String studentName,
                                        String studentCode,
                                        String className,
                                        List<String> masterpieces) {
        if (teacherId == null || teacherId <= 0L
                || StringUtils.isAnyBlank(studentName, studentCode, className)
                || CollectionUtils.isEmpty(masterpieces)) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Tangram:addStudent")
                    .keys(teacherId)
                    .callback(() -> remoteReference.addTangramStudent(teacherId, studentName, studentCode, className, masterpieces))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public MapMessage modifyTangramStudent(Long studentId,
                                           String studentName,
                                           String studentCode,
                                           String className,
                                           List<String> masterpieces) {
        if (studentId == null || studentId <= 0L ||
                StringUtils.isAnyBlank(studentName, studentCode, className)
                || CollectionUtils.isEmpty(masterpieces)) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Tangram:modifyStudent")
                    .keys(studentId)
                    .callback(() -> remoteReference.modifyTangramStudent(studentId, studentName, studentCode, className, masterpieces))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public MapMessage deleteTangramStudent(Long studentId) {
        if (studentId == null || studentId <= 0L) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Tangram:deleteStudent")
                    .keys(studentId)
                    .callback(() -> remoteReference.deleteTangramStudent(studentId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public MapMessage judgeTangramStudent(Long studentId, String auditor, String score, String comment) {
        if (studentId == null || studentId <= 0L || StringUtils.isAnyBlank(auditor, score, comment)) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Tangram:judgeStudent")
                    .keys(studentId)
                    .callback(() -> remoteReference.judgeTangramStudent(studentId, auditor, score, comment))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public TeacherScholarshipRecord loadTeacherScholarshipRecord(Long teacherId) {
        return remoteReference.loadTeacherScholarshipRecord(teacherId);
    }

    public List<Map<String, Object>> loadScholarshipDailyList() {
        return remoteReference.loadScholarshipDailyList();
    }

    public MapMessage applyDailyScholarshipEntrance(Long teacherId) {
        return remoteReference.applyDailyScholarshipEntrance(teacherId);
    }

    public MapMessage applyFinalScholarshipEntrance(Long teacherId) {
        return remoteReference.applyFinalScholarshipEntrance(teacherId);
    }

    public MapMessage applyDailyScholarship(Long teacherId){
        return remoteReference.applyDailyScholarship(teacherId);
    }

    public MapMessage applyWeekScholarShip(Long teacherId){
        return remoteReference.applyWeekScholarship(teacherId);
    }

    public MapMessage applyFinalScholarShip(Long teacherId){
        return remoteReference.applyFinalScholarship(teacherId);
    }

    public Long loadScholarshipFinalAttendNum() {
        return remoteReference.loadScholarshipFinalAttendNum();
    }

    public TeacherNewTermActivityProgress loadParticipateActivityInfo(Long activityId, Long teacherId){
        return remoteReference.findTeacherActivityProgress(activityId,teacherId);
    }

    public boolean updateDailyLottery() {
        return remoteReference.updateDailyLottery();
    }

    public boolean updateWeekLottery() {
        return remoteReference.updateWeekLottery();
    }

}
