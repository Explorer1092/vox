package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.task2019;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.AbstractTeacherTaskInit;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Named
public class TeacherTaskAssignHomeworkInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.ONGOING.name())) {
            teacherTask.setStatus(TeacherTask.Status.CANCEL.name());
            teacherTaskProgress.setStatus(TeacherTask.Status.CANCEL.name());
            teacherTask.setCancelDate(new Date());
            return true;
        }
        return false;
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        return "";
    }

    @Override
    public String rewardProgressComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.ProgressReward reward) {
        return "";
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = super.getTeacherTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.INIT.name())) {
            return teacherTaskEntry;
        }

        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);

        TeacherTaskEntry.CrmProgress crmProgress = new TeacherTaskEntry.CrmProgress();
        TeacherTaskProgress.SubTaskProgress firstSubTask = teacherTaskProgress.getSubTaskProgresses().get(0);
        crmProgress.setTarget(firstSubTask.getProgress().getTarget());
        crmProgress.setCurr(firstSubTask.getProgress().getCurr());
        crmProgressList.add(crmProgress);

        if (StringUtils.isNotEmpty(teacherTaskProgress.getReceiveDate())) {
            teacherTaskEntry.setReceiveDate(teacherTaskProgress.getReceiveDate());
        }

        return teacherTaskEntry;
    }

    @Override
    public boolean isDisplay(TeacherDetail teacherDetail, List<TeacherTask> teacherTaskList, List<TeacherTaskProgress> teacherTaskProgressList, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return false;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.ASSIGN_HOMEWORK_2019;
    }
}
