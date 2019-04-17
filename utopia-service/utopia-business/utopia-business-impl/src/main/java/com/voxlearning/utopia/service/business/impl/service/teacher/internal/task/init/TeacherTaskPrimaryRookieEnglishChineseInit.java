package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherSubTaskEntry;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskDao;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.JexlContext;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小英小语新手任务
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskPrimaryRookieEnglishChineseInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private TeacherTaskServiceImpl teacherTaskServiceImpl;

    @Inject
    private TeacherTaskDao teacherTaskDao;

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

    /**
     * 临时补一个特殊逻辑，新手任务拆分，小数与小英小语文两个，为了兼容老数据，之前已经领取了小数的语文或者英语老师，则不能在领取拆分后的任务
     * 1 表示小学数学新手任务， 14表示小学英语与语文新手任务
     *
     * 这里主要是兼容，如果脏数据都没有了，可以删除这个代码
     *
     * @param teacherDetail
     * @param calContext
     * @return
     */
    @Override
    public boolean isPutOn(TeacherDetail teacherDetail, JexlContext calContext){
        List<TeacherTask> teacherTasks = teacherTaskDao.loadByTeacherId(teacherDetail.getId());
        List<Long> tplIds = teacherTasks.stream().map(TeacherTask::getTplId).filter(id -> id == 14 || id == 1).collect(Collectors.toList());
        if (!tplIds.isEmpty()) {//表示已经领取了新手任务，则不在处理
            return false;
        }
        return true;
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        TeacherTaskTpl.SubTask subTaskById = teacherTaskTpl.getSubTaskById(subTaskProgress.getId());
        if (Objects.equals(subTaskProgress.getId(), 2L) && TeacherTaskTpl.RewardUnit.integral.name().equals(reward.getUnit())) {//如果是第二个子任务完成了 并且发园丁豆的子任务
            Integer maxFinishNum = MapUtils.getInteger(subTaskProgress.getVars(),"maxFinishNum");
            String integralComment = StringUtils.formatMessage("共{}个学生完成作业",maxFinishNum);
            return "，" + subTaskById.getDesc() + "完成，" + integralComment + "。";
        }
        return "，" + subTaskById.getDesc() + "完成。";
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = super.getTeacherTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
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

        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);

        TeacherTaskEntry.CrmProgress crmProgress = new TeacherTaskEntry.CrmProgress();
        crmProgress.setTarget(teacherTaskProgress.getSubTaskProgresses().size());
        crmProgress.setCurr(Long.valueOf(teacherTaskEntry.getSubTaskFinishedNum()).intValue());
        crmProgressList.add(crmProgress);

        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.FINISHED.name())) {
            teacherTaskEntry.setFinishDate(DateUtils.dateToString(teacherTask.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
        } else if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.EXPIRED.name())) {
            teacherTaskEntry.setExpireDate(DateUtils.dateToString(teacherTask.getExpireDate(), DateUtils.FORMAT_SQL_DATETIME));
        }

        return teacherTaskEntry;
    }

    @Override
    public List<TeacherSubTaskEntry> getTeacherSubTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        List<TeacherSubTaskEntry> subTaskEntryList = super.getTeacherSubTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
        Map<Long, TeacherTaskProgress.SubTaskProgress> subTaskProgressMap = teacherTaskProgress.getSubTaskProgresses().stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        for (TeacherSubTaskEntry teacherSubTaskEntry : subTaskEntryList) {
            if (teacherSubTaskEntry.getId() == null) {
                continue;
            }
            TeacherTaskProgress.SubTaskProgress subTaskProgress = subTaskProgressMap.get(teacherSubTaskEntry.getId());
            Map<Long, TeacherTaskProgress.Reward> rewardMap = subTaskProgress.getRewards().stream().collect(Collectors.toMap(r -> r.getId(), r -> r));
            List<TeacherSubTaskEntry.Reward> rewards = new ArrayList<>();
            if (Objects.equals(teacherSubTaskEntry.getId(), 1L)) {
                TeacherTaskProgress.Reward rewardIntegral = rewardMap.values().stream().filter(r -> Objects.equals(r.getUnit(), TeacherTaskTpl.RewardUnit.integral.name())).findAny().orElse(null);
                rewards.add(new TeacherSubTaskEntry.Reward(1L, TeacherTaskTpl.RewardUnit.integral.name(), rewardIntegral.getValue(), 1, ""));

                TeacherTaskProgress.Reward rewardExp = rewardMap.values().stream().filter(r -> Objects.equals(r.getUnit(), TeacherTaskTpl.RewardUnit.exp.name())).findAny().orElse(null);
                if (rewardExp != null) {
                    rewards.add(new TeacherSubTaskEntry.Reward(2L, TeacherTaskTpl.RewardUnit.exp.name(), rewardExp.getValue(), 2, ""));
                }
            }
            if (Objects.equals(teacherSubTaskEntry.getId(), 2L)) {
                rewards.add(new TeacherSubTaskEntry.Reward(1L, TeacherTaskTpl.RewardUnit.integral.name(), null, 1, "*完成人数"));

                TeacherTaskProgress.Reward rewardExp = rewardMap.values().stream().filter(r -> Objects.equals(r.getUnit(), TeacherTaskTpl.RewardUnit.exp.name())).findAny().orElse(null);
                if (rewardExp != null) {
                    rewards.add(new TeacherSubTaskEntry.Reward(2L, TeacherTaskTpl.RewardUnit.exp.name(), rewardExp.getValue(), 2, ""));
                }
            }
            teacherSubTaskEntry.setRewards(rewards);
        }
        return subTaskEntryList;
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
        return TeacherTaskTpl.Tpl.PRIMARY_ROOKIE_ENGLISH_CHINESE;
    }
}