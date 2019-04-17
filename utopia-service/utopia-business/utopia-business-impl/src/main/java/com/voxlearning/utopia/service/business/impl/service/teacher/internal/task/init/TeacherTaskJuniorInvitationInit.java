package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouwei on 2018/10/8
 **/
@Named
@Slf4j
public class TeacherTaskJuniorInvitationInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    private Date[] getDate() {
        Date now = new Date();
        String year = DateUtils.dateToString(now, "yyyy");
        String dateStringOne = year + "-01-01 00:00:01";
        String dateStringTwo = year + "-08-31 23:59:59";
        String dateStringThree = year + "-12-31 23:59:59";
        Date dateOne = DateUtils.stringToDate(dateStringOne, DateUtils.FORMAT_SQL_DATETIME);
        Date dateTwo = DateUtils.stringToDate(dateStringTwo, DateUtils.FORMAT_SQL_DATETIME);
        Date dateThree = DateUtils.stringToDate(dateStringThree, DateUtils.FORMAT_SQL_DATETIME);
        if (now.getTime() < dateTwo.getTime()) {
            return new Date[]{dateOne, dateTwo};
        } else {
            return new Date[]{dateTwo, dateThree};
        }
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskProgress.SubTaskProgress> subTaskProgresses = teacherTaskProgress.getSubTaskProgresses();
        Integer value = subTaskProgresses.get(0).getRewards().get(0).getValue();
        teacherTaskEntry.addReward(SafeConverter.toString(value), TeacherTaskTpl.RewardUnit.cash.name(), "人");
        TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();
        entryProgress.setCurr(0);
        List<InviteHistory> inviteHistoryList = asyncInvitationServiceClient.getAsyncInvitationService().queryByUserIdSuccess(teacherDetail.getId()).getUninterruptibly();
        if (CollectionUtils.isNotEmpty(inviteHistoryList)) {
            Date[] date = this.getDate();
            int count = 0;
            for (InviteHistory inviteHistory : inviteHistoryList) {
                long createTime = inviteHistory.getCreateTime().getTime();
                if (createTime < date[0].getTime() || createTime > date[1].getTime()) {
                    continue;
                }
                count++;
            }
            entryProgress.setCurr(count);
        }

        entryProgress.setQ("人");
        teacherTaskEntry.setProgress(entryProgress);
        teacherTaskEntry.setActionName("已邀请");
        teacherTaskEntry.setCrmIsDisplay(false);
        return teacherTaskEntry;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.JUNIOR_INVITATION;
    }
}
