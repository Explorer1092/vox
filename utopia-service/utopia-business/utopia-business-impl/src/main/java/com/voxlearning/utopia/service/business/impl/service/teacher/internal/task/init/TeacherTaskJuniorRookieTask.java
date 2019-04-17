package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.misc.TeacherInvitationConfig;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by zhouwei on 2018/9/4
 **/
@Named
@Slf4j
public class TeacherTaskJuniorRookieTask extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private TeacherTaskServiceImpl teacherTaskServiceImpl;

    @Inject
    private AsyncInvitationServiceClient invitationService;

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public boolean processNewVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, Map<String, Object> newVars){
        Long createAt = MapUtils.getLong(newVars, "createAt");
        Long checkedAt = MapUtils.getLong(newVars, "checkedAt");
        String type = MapUtils.getString(newVars, "messageType");
        if (Objects.equals(type, "check") || Objects.equals(type, "assign")) {
            if (!this.checkHomeworkTime(createAt, checkedAt)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void progressAddVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {
        Map<String,Object> varMap = subTaskProgress.getVarMap();
        //第三个子任务，检查作业的时候，需要老师的cityLevel
        if (3 == subTaskProgress.getId()) {
            TeacherInvitationConfig config = invitationService.getAsyncInvitationService().queryTeacherInvitationConfig(teacherDetail.getCityCode()).getUninterruptibly();
            if (null != config) {
                varMap.put("cityLevel", config.getCityLevel());
            }
        }
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
        crmProgress.setTarget(teacherTaskProgress.getSubTaskProgresses().size());
        crmProgress.setCurr(Long.valueOf(teacherTaskEntry.getSubTaskFinishedNum()).intValue());
        crmProgressList.add(crmProgress);
        if (StringUtils.isNotEmpty(teacherTaskProgress.getReceiveDate())) {
            teacherTaskEntry.setReceiveDate(teacherTaskProgress.getReceiveDate());
        }

        if (teacherTaskEntry.getReceiveDate() == null && teacherTask.getExpireDate() != null) {//receiveDate是后面才加的，老任务可能没有，则用过期时间计算，不是很精准，因为过期时间都是设置的过期那天的23:59:59，早期任务没有过期时间
            teacherTaskEntry.setReceiveDate(DateUtils.dateToString(new Date(teacherTask.getExpireDate().getTime() - 15 * 24 * 60 * 60 * 1000), DateUtils.FORMAT_SQL_DATETIME));
        } else {//没有过期时间的任务，使用创建时间来代替
            teacherTaskEntry.setReceiveDate(DateUtils.dateToString(teacherTask.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
        }

        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.FINISHED.name())) {
            teacherTaskEntry.setFinishDate(DateUtils.dateToString(teacherTask.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
        } else if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.EXPIRED.name())) {
            teacherTaskEntry.setExpireDate(DateUtils.dateToString(teacherTask.getExpireDate(), DateUtils.FORMAT_SQL_DATETIME));
        }
        return teacherTaskEntry;
    }

    @Override
    public boolean isDisplay(TeacherDetail teacherDetail, List<TeacherTask> teacherTaskList, List<TeacherTaskProgress> teacherTaskProgressList, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.INIT.name())) {//如果任务存在，并且是初始化状态
            boolean isPutOn = teacherTaskServiceImpl.isPutOn(teacherDetail, this.teacherTaskTpl);
            if (!isPutOn) {//如果还备领取条件，则继续显示，否则不显示了
                return false;
            }
        }
        return true;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.JUNIOR_ROOKIE;
    }

}

