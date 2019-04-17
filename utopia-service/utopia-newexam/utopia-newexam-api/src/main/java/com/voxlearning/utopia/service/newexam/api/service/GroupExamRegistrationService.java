package com.voxlearning.utopia.service.newexam.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 班级报名考试loader
 * @author majianxin
 * @version V1.0
 * @date 2019/1/28
 */
@ServiceVersion(version = "20190221")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface GroupExamRegistrationService extends IPingable {

    /**
     * 查询老师所有班级当前考试报名状态
     * @param exam 考试ID
     * @param teacherId 老师ID
     * @return
     */
    MapMessage fetchExamRegistrationResult(NewExam exam, Long teacherId);

    /**
     * 老师按班级报名考试
     * @param exam 考试ID
     * @param groupIds 班组ids
     * @return
     */
    MapMessage register(TeacherDetail teacherDetail, NewExam exam, List<Long> groupIds);

    /**
     * 取消报名考试
     * @param newExamId 考试ID
     * @param groupId 班组ID
     * @return
     */
    MapMessage unRegister(String newExamId, Long groupId);

    MapMessage shareReport(TeacherDetail teacherDetail, String newExamId, Long groupId);

}
