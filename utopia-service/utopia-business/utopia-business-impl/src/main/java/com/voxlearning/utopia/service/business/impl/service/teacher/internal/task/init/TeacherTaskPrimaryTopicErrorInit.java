package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Date;

/**
 * Created by zhouwei on 2018/10/8
 **/
@Named
@Slf4j
public class TeacherTaskPrimaryTopicErrorInit extends AbstractTeacherTaskInit implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.addReward(SafeConverter.toString(1), TeacherTaskTpl.RewardUnit.integral.name(), "题");
        teacherTaskEntry.setCrmIsDisplay(false);
        return teacherTaskEntry;
    }

    @Override
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        Date expireDate = new Date();
        String status = TeacherTask.Status.CANCEL.name();
        teacherTask.setStatus(status);
        teacherTask.setExpireDate(expireDate);
        teacherTaskProgress.setStatus(status);
        teacherTaskProgress.setExpireTime(expireDate.getTime());
        return true;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_TOPIC_ERROR;
    }

}
