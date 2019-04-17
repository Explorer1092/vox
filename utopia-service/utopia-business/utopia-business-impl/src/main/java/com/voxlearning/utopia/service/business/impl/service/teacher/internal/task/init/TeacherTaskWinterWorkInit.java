package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.support.WinterWorkConfigUtils;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.JexlContext;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * 寒假作业
 **/
@Named
@Slf4j
public class TeacherTaskWinterWorkInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        return "";
    }

    @Override
    public void initAddJexlContext(TeacherDetail teacherDetail, JexlContext jexlContext) {
        Integer cityCode = teacherDetail.getCityCode();
        if (cityCode == null) {
            jexlContext.set("allow", false);
        } else {
            boolean content = WinterWorkConfigUtils.contentCity(teacherDetail.getCityCode());
            boolean timeScope = WinterWorkConfigUtils.timeScope();
            jexlContext.set("allow", content && timeScope);
        }
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskEntry.Reward> teacherTaskEntryRewardCommon = this.createTeacherTaskEntryRewardCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardList(teacherTaskEntryRewardCommon);
        teacherTaskEntry.setCrmIsDisplay(true);
        if (teacherTask.getFinishedDate() != null) {
            String finishedDate = DateFormatUtils.format(teacherTask.getFinishedDate(), DateUtils.FORMAT_SQL_DATETIME);
            teacherTaskEntry.setFinishDate(finishedDate);
        }
        if (teacherTask.getExpireDate() != null) {
            String expireDate = DateFormatUtils.format(teacherTask.getExpireDate(), DateUtils.FORMAT_SQL_DATETIME);
            teacherTaskEntry.setExpireDate(expireDate);
        }
        if (teacherTask.getReceiveDate() != null) {
            String receiveDate = DateFormatUtils.format(teacherTask.getReceiveDate(), DateUtils.FORMAT_SQL_DATETIME);
            teacherTaskEntry.setReceiveDate(receiveDate);
        }
        return teacherTaskEntry;
    }

    @Override
    public Date calcExpireDate(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return WinterWorkConfigUtils.getEndTime();
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.WINTER_WORK;
    }

}