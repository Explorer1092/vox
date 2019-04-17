package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.List;

/**
 *
 * 老师签到任务的初始化
 *
 * Created by zhouwei on 2018/9/3
 **/
@Named
@Slf4j
public class TeacherTaskPrimaryUserInfoFullInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        return "";
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskEntry.Reward> teacherTaskEntryRewardCommon = this.createTeacherTaskEntryRewardCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardList(teacherTaskEntryRewardCommon);
        teacherTaskEntry.setCrmIsDisplay(false);
        return teacherTaskEntry;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_USER_INFO_FULL;
    }

}