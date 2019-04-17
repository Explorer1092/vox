package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.task2019;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.service.TeacherRookieTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.AbstractTeacherTaskInit;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * 2019 邀请老师
 **/
@Named
@Slf4j
public class TeacherTask2019FirstInvitationInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject
    private TeacherRookieTaskServiceImpl teacherRookieTaskService;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

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
        teacherTaskEntry.setRewardPrefix("限时奖励");
        teacherTaskEntry.addReward(SafeConverter.toString(1000), TeacherTaskTpl.RewardUnit.integral.name(), "人");
        TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();

        List<InviteHistory> inviteHistoryList = asyncInvitationServiceClient.getAsyncInvitationService().queryByUser2019First(teacherDetail.getId());

        int count = 0;
        for (InviteHistory inviteHistory : inviteHistoryList) {
            TeacherDetail item = teacherLoaderClient.loadTeacherDetail(inviteHistory.getInviteeUserId());
            if (item == null) continue;

            if (item.isPrimarySchool()) {
                // 任务
                TeacherRookieTask teacherRookieTask = teacherRookieTaskService.loadRookieTask(item.getId());
                if (teacherRookieTask != null && teacherRookieTask.fetchFinished()) {
                    count++;
                }
            } else {
                if (item.getAuthenticationState().equals(AuthenticationState.SUCCESS.getState())) {
                    count++;
                }
            }
        }
        entryProgress.setCurr(count);

        entryProgress.setQ("人");
        teacherTaskEntry.setProgress(entryProgress);
        teacherTaskEntry.setActionName("已成功邀请");

        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);

        /*crmProgressList.add(new TeacherTaskEntry.CrmProgress("已成功邀请", null, countSuccess, "人"));
        crmProgressList.add(new TeacherTaskEntry.CrmProgress("邀请中", null, countGoing, "人"));*/

        List<String> crmTaskDesc = new ArrayList<>();
        crmTaskDesc.add("1.奖励根据被邀请人新手任务的完成情况分阶段发");
        crmTaskDesc.add("2.发送奖励的条件是邀请人已认证");
        teacherTaskEntry.setCrmTaskDesc(crmTaskDesc);
        return teacherTaskEntry;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.INVITATION_FIRST_2019;
    }

}