package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskTplDao;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 三次作业奖励
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskPrimaryThreeHomeworkInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private TeacherTaskServiceImpl teacherTaskServiceImpl;
    
    @Inject
    private TeacherTaskTplDao teacherTaskTplDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public boolean processNewVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, Map<String, Object> newVars){
        Long createAt = MapUtils.getLong(newVars, "createAt");
        Long checkedAt = MapUtils.getLong(newVars, "checkedAt");
        String subject = MapUtils.getString(newVars, "subject");
        String objectiveConfigTypes = MapUtils.getString(newVars, "objectiveConfigTypes");
        String type = MapUtils.getString(newVars, "messageType");
        if (Objects.equals(type, "check") || Objects.equals(type, "assign")) {
            if (!this.checkHomeworkTime(createAt, checkedAt) || !this.checkHomeworkAndObjectiveIsMatch(teacherDetail.getKtwelve(), subject, objectiveConfigTypes)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = super.getTeacherTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
        TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();
        entryProgress.setCurr(teacherTaskProgress.getSubTaskProgresses().get(0).getProgress().getCurr());
        entryProgress.setQ("次");
        teacherTaskEntry.setProgress(entryProgress);
        teacherTaskEntry.setActionName("已完成");

        List<String> crmTaskDesc = new ArrayList<>();
        crmTaskDesc.add("1.检查作业要求20人以上完成");
        crmTaskDesc.add("2.给任意一个班级检查3次满足要求的作业即可");
        crmTaskDesc.add("3.当天给多班检查，仅算一次");
        teacherTaskEntry.setCrmTaskDesc(crmTaskDesc);

        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.INIT.name())) {
            return teacherTaskEntry;
        }
        if (StringUtils.isNotEmpty(teacherTaskProgress.getReceiveDate())) {
            teacherTaskEntry.setReceiveDate(teacherTaskProgress.getReceiveDate());
        }
        
        if (teacherTaskEntry.getReceiveDate() == null && teacherTask.getExpireDate() != null) {//receiveDate是后面才加的，老任务可能没有，则用过期时间计算，不是很精准，因为过期时间都是设置的过期那天的23:59:59，早期任务没有过期时间
            teacherTaskEntry.setReceiveDate(DateUtils.dateToString(new Date(teacherTask.getExpireDate().getTime() - 15 * 24 * 60 * 60 * 1000), DateUtils.FORMAT_SQL_DATETIME));
        } else {//没有过期时间的任务，使用创建时间来代替
            teacherTaskEntry.setReceiveDate(DateUtils.dateToString(teacherTask.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
        }

        if (CollectionUtils.isNotEmpty(teacherTaskProgress.getSubTaskProgresses())) {
            List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
            teacherTaskEntry.setCrmProgressList(crmProgressList);
            TeacherTaskProgress.Progress progress = teacherTaskProgress.getSubTaskProgresses().get(0).getProgress();
            TeacherTaskEntry.CrmProgress crmProgress = new TeacherTaskEntry.CrmProgress();
            crmProgress.setTarget(progress.getTarget());
            crmProgress.setCurr(progress.getCurr());
            crmProgressList.add(crmProgress);
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

        List<TeacherTaskTpl> teacherTaskTpls = teacherTaskTplDao.loadAll();
        Map<Long, TeacherTaskTpl> tplMap = teacherTaskTpls.stream().collect(Collectors.toMap(t -> t.getId(), t -> t));

        TeacherTask rookieTask = teacherTaskList.stream().filter(t -> Objects.equals(t.getTplId(),TeacherTaskTpl.Tpl.JUNIOR_ROOKIE.getTplId())
                || Objects.equals(t.getTplId(),TeacherTaskTpl.Tpl.PRIMARY_ROOKIE.getTplId())
                || Objects.equals(t.getTplId(),TeacherTaskTpl.Tpl.PRIMARY_ROOKIE_ENGLISH_CHINESE.getTplId())).findAny().orElse(null);

        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.INIT.name())) {
            if (rookieTask == null) {//没有新手任务，则不展示3次作业奖励
                return false;
            }

            TeacherTaskTpl teacherTaskTpl = tplMap.get(rookieTask.getTplId());
            if (!Objects.equals(rookieTask.getStatus(), TeacherTask.Status.ONGOING.name())) {//如果新手任务不在进行中的状态
                if (Objects.equals(rookieTask.getStatus(), TeacherTask.Status.EXPIRED.name())) {//新手任务任务过期的不展示
                    return false;
                }

                boolean isPutOn = teacherTaskServiceImpl.isPutOn(teacherDetail, teacherTaskTpl);
                if (Objects.equals(rookieTask.getStatus(), TeacherTask.Status.INIT.name()) && !isPutOn) {//新手任务是初始化状态，但是不具备领取条件，也不显示
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_THREE_HOMEWORK;
    }
}