package com.voxlearning.utopia.service.newexam.consumer.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.service.GroupExamRegistrationService;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/28
 */
public class GroupExamRegistrationServiceClient implements GroupExamRegistrationService {

    @ImportService(interfaceClass = GroupExamRegistrationService.class)
    private GroupExamRegistrationService remoteReference;

    @Override
    public MapMessage fetchExamRegistrationResult(NewExam exam, Long teacherId) {
        return remoteReference.fetchExamRegistrationResult(exam, teacherId);
    }

    @Override
    public MapMessage register(TeacherDetail teacherDetail, NewExam exam, List<Long> groupIds) {
        return remoteReference.register(teacherDetail, exam, groupIds);
    }

    @Override
    public MapMessage unRegister(String newExamId, Long groupId) {
        return remoteReference.unRegister(newExamId, groupId);
    }

    public MapMessage shareReport(TeacherDetail teacherDetail, String newExamId, Long groupId){
        return remoteReference.shareReport(teacherDetail, newExamId, groupId);
    }
}
