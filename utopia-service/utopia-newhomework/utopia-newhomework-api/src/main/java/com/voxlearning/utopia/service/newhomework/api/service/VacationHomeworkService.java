package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author tanguohong
 * @since 2016/11/29
 */
@ServiceVersion(version = "20180727")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface VacationHomeworkService extends IPingable {

    MapMessage assignHomework(Teacher teacher, HomeworkSource context, HomeworkSourceType homeworkSourceType);

    MapMessage deleteHomework(Long teacherId, String id);

    MapMessage crmDeleteVacationHomework(String id);

    MapMessage resumeVacationHomework(String packageId);

    /**
     * 返回数据
     * {
     * "success":true,
     * "vhid":"假期作业id"
     * }
     */
    VacationHomework generateVacationHomework(String packageId, Integer weekRank, Integer dayRank, Long studentId);

    MapMessage processVacationHomeworkResult(VacationHomeworkResultContext vacationHomeworkResultContext);

    MapMessage vacationHomeworkCommentRewardIntegral(TeacherDetail teacherDetail, String homeworkId, Integer rewardIntegral);

    MapMessage vacationHomeworkComment(String homeworkId, String comment, String audioComment);

    void removeCache(List<String> keys);

    MapMessage autoAssign(Teacher teacher);

    MapMessage loadSubjectiveFiles(String homeworkId, ObjectiveConfigType objectiveConfigType, String questionId);

    MapMessage autoSubmitVacationHomework(String homeworkId, Long userId, ObjectiveConfigType type);

    MapMessage autoSubmitDubbingHomework(String homeworkId);
}
