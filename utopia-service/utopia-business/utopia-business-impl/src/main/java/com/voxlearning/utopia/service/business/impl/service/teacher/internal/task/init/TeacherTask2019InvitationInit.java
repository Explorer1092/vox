package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.JexlContext;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 2019 邀请老师
 **/
@Named
@Slf4j
public class TeacherTask2019InvitationInit extends AbstractTeacherTaskInit implements InitializingBean {

    private static Date END_DATE;

    static {
        try {
            END_DATE = DateUtils.parseDate("2019-02-24 23:59:59", "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            log.error("TeacherTask2019InvitationInit exception", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        return "";
    }

    @Override
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        // 2019/2/23 23:59:59 修改为 2019/2/24 23:59:59
        if (Objects.equals(teacherTask.getExpireDate().getTime(), 1550937599000L)) { // 1550937599000L 2019/2/23 23:59:59
            teacherTask.setExpireDate(END_DATE);
            teacherTaskProgress.setExpireTime(END_DATE.getTime());
            return true;
        }
        return false;
    }

    @Override
    public void initAddJexlContext(TeacherDetail teacherDetail, JexlContext jexlContext) {
        boolean before = new Date().before(END_DATE);
        jexlContext.set("allow", before);
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskEntry.Reward> teacherTaskEntryRewardCommon = this.createTeacherTaskEntryRewardCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardList(teacherTaskEntryRewardCommon);
        teacherTaskEntry.setStatus(TeacherTask.Status.ONGOING.toString()); // 假装一直是进行中(实际是 init 状态, 这样活动结束后才会被删掉)
        teacherTaskEntry.setCrmIsDisplay(true);
        int integral = 1000;
        if (teacherDetail.isPrimarySchool()) {
            teacherTaskEntry.addReward(SafeConverter.toString(integral), TeacherTaskTpl.RewardUnit.integral.name(), "人");
        } else {
            teacherTaskEntry.addReward(SafeConverter.toString(integral * 10), TeacherTaskTpl.RewardUnit.integral.name(), "人");
        }
        teacherTaskEntry.setDeadline(DateUtils.dateToString(END_DATE, "yyyy.MM.dd"));
        if (teacherTask.getReceiveDate() != null) {
            String receiveDate = DateFormatUtils.format(teacherTask.getReceiveDate(), DateUtils.FORMAT_SQL_DATETIME);
            teacherTaskEntry.setReceiveDate(receiveDate);
        }
        return teacherTaskEntry;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.INVITATION_2019;
    }

}