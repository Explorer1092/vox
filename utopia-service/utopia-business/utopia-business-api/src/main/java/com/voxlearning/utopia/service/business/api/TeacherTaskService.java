package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.constant.TeacherTaskCalType;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181026")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeacherTaskService {

    /**
     * 更新老师任务的进度
     *
     * @param teacherId
     * @param tplEvaluatorEvent
     * @param newVarMap
     */
    void updateProgress(Long teacherId, TeacherTaskTpl.TplEvaluatorEvent tplEvaluatorEvent, Map<String, Object> newVarMap);

    /**
     * 领取限时任务
     *
     * @return
     */
    MapMessage receiveTask(Long teacherId, Long taskId);

    /**
     * 判断用户是否具备领取任务的条件
     * @param td
     * @param tpl
     * @return
     */
    boolean isPutOn(TeacherDetail td, TeacherTaskTpl tpl);

}
