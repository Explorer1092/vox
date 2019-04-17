package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.service.VacationHomeworkService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.List;

/**
 * @author tanguohong
 * @since 2016/11/30
 */
public class VacationHomeworkServiceClient implements VacationHomeworkService {

    @ImportService(interfaceClass = VacationHomeworkService.class)
    private VacationHomeworkService reference;

    @Override
    public MapMessage assignHomework(Teacher teacher, HomeworkSource source, HomeworkSourceType homeworkSourceType) {
        return reference.assignHomework(teacher, source, homeworkSourceType);
    }

    @Override
    public MapMessage deleteHomework(Long teacherId, String hid) {
        return reference.deleteHomework(teacherId, hid);
    }

    @Override
    public MapMessage crmDeleteVacationHomework(String id) {
        return reference.crmDeleteVacationHomework(id);
    }

    @Override
    public MapMessage resumeVacationHomework(String packageId) {
        return reference.resumeVacationHomework(packageId);
    }

    @Override
    public VacationHomework generateVacationHomework(String packageId, Integer weekRank, Integer dayRank, Long studentId) {
        return reference.generateVacationHomework(packageId, weekRank, dayRank, studentId);
    }

    @Override
    public MapMessage processVacationHomeworkResult(VacationHomeworkResultContext vacationHomeworkResultContext) {
        return reference.processVacationHomeworkResult(vacationHomeworkResultContext);
    }

    @Override
    public MapMessage vacationHomeworkCommentRewardIntegral(TeacherDetail teacherDetail, String homeworkId, Integer rewardIntegral) {
        return reference.vacationHomeworkCommentRewardIntegral(teacherDetail, homeworkId, rewardIntegral);
    }

    @Override
    public MapMessage vacationHomeworkComment(String homeworkId, String comment, String audioComment) {
        return reference.vacationHomeworkComment(homeworkId, comment, audioComment);
    }

    @Override
    public void removeCache(List<String> keys) {
        reference.removeCache(keys);
    }


    @Override
    public MapMessage autoAssign(Teacher teacher) {
        return reference.autoAssign(teacher);
    }

    @Override
    public MapMessage loadSubjectiveFiles(String homeworkId, ObjectiveConfigType objectiveConfigType, String questionId) {
        return reference.loadSubjectiveFiles(homeworkId, objectiveConfigType, questionId);
    }

    @Override
    public MapMessage autoSubmitVacationHomework(String homeworkId, Long userId, ObjectiveConfigType type) {
        return reference.autoSubmitVacationHomework(homeworkId, userId, type);
    }

    @Override
    public MapMessage autoSubmitDubbingHomework(String homeworkId) {
        return reference.autoSubmitDubbingHomework(homeworkId);
    }
}
