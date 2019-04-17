package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.service.business.api.TeacherTaskService;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Map;

public class TeacherTaskServiceClient {

    @ImportService(interfaceClass = TeacherTaskService.class)
    private TeacherTaskService remoteReference;

    public MapMessage receiveTask(Long teacherId, Long taskId) {
        return remoteReference.receiveTask(teacherId, taskId);
    }

    public boolean isPutOn(TeacherDetail td, TeacherTaskTpl tpl) {
        return remoteReference.isPutOn(td, tpl);
    }
}
