package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherSubTaskEntry;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskProgressDao;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * 小学全科布置检查作业的常规任务
 *
 * Created by zhouwei on 2018/9/4
 **/
@Named
@Slf4j
public class TeacherTaskPrimaryAssignCheckHomeworkInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private GroupLoaderClient groupLoaderClient;

    @Inject
    private TeacherTaskDao teacherTaskDao;

    @Inject
    private TeacherTaskProgressDao teacherTaskProgressDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public boolean processNewVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, Map<String, Object> newVars){
        Long createAt = MapUtils.getLong(newVars, "createAt");
        Long checkedAt = MapUtils.getLong(newVars, "checkedAt");
        String type = MapUtils.getString(newVars, "messageType");
        if (Objects.equals(type, "check")) {
            if (!this.checkHomeworkTime(createAt, checkedAt)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateTaskInfo(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, Map<String,Object> varMap){
        //只处理与班级相关的事件
        if (null == varMap.get("event_type") || !Objects.equals(SafeConverter.toString(varMap.get("event_type")), EventUserType.TEACHER_CLAZZ_UPDATED.getEventType())) {
            return;
        }
        List<TeacherTaskProgress.SubTaskProgress> subTaskProgressList = teacherTaskProgress.getSubTaskProgresses();
        int target = subTaskProgressList.get(0).getProgress().getTarget();
        int groupNum = this.getGroupNum(teacherDetail.getId());
        if (groupNum <= target) {//组的数量小于目标值，则不处理
            return;
        }
        for (TeacherTaskProgress.SubTaskProgress subTaskProgress : subTaskProgressList) {
            subTaskProgress.setStatus(TeacherTask.Status.ONGOING.name());
            subTaskProgress.getProgress().setTarget(groupNum);
        }
        teacherTask.setStatus(TeacherTask.Status.ONGOING.name());
        teacherTaskProgress.setStatus(TeacherTask.Status.ONGOING.name());
        teacherTaskDao.upsert(teacherTask);
        teacherTaskProgressDao.upsert(teacherTaskProgress);
    }

    @Override
    public void initAddJexlContext(TeacherDetail teacherDetail, JexlContext jexlContext) {
        //老师名下非毕业班的数量
        jexlContext.set("groupNum", this.getGroupNum(teacherDetail.getId()));
    }

    @Override
    public void progressAddVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {
        //老师主学科
        subTaskProgress.getVarMap().put("main_teacher_subject", teacherDetail.getSubject().name());

        //老师名下非毕业班的数量
        Set<Long> teacherIds = new HashSet<>();
        teacherIds.add(teacherDetail.getId());
        List<Long> ids = teacherLoaderClient.loadSubTeacherIds(teacherDetail.getId());
        if (CollectionUtils.isNotEmpty(ids)) {
            teacherIds.addAll(ids);
        }
        Set<Long> groupIds = new HashSet<>();
        teacherIds.forEach(id -> {
            Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader().findByGroupTeacherIdNotTerminal(id).getUninterruptibly();
            if (!groupMap.isEmpty()) {
                groupIds.addAll(groupMap.keySet());
            }
        });
        subTaskProgress.getVarMap().put("groupNum", groupIds.size());
        if (subTaskProgress.getVarMap().containsKey("receivedCurr")) {
            subTaskProgress.getVarMap().put("receivedCurr", 0);
        }

    }

    @Override
    public void oneProcessComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress){
        Map<String, Object> varMap = subTaskProgress.getVars();
        if (varMap.get("currFinishNum") == null) {
            return;
        }
        if (!varMap.containsKey("currFinishNumList")) {
            varMap.put("currFinishNumList", new ArrayList<>());
        }
        List<Integer> currFinishNumList = (List<Integer>)varMap.get("currFinishNumList");
        Integer currFinishNum = SafeConverter.toInt(varMap.get("currFinishNum"));
        currFinishNumList.add(currFinishNum);
    }

    @Override
    public String rewardProgressComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.ProgressReward reward) {
        TeacherTaskTpl.SubTask subTaskById = teacherTaskTpl.getSubTaskById(subTaskProgress.getId());
        if (TeacherTaskTpl.RewardUnit.integral.name().equals(reward.getUnit())) {//如果是第二个子任务完成了 并且发园丁豆的子任务
            Integer currFinishNum = MapUtils.getInteger(subTaskProgress.getVars(),"currFinishNum");
            String integralComment = StringUtils.formatMessage("共{}个学生完成作业", currFinishNum);
            return "，" + subTaskById.getDesc() + "，完成一个进度" + "，" + integralComment + "。";
        }
        return "，" + subTaskById.getDesc() + "，完成一个进度。";
    }

    @Override
    public List<TeacherSubTaskEntry> getTeacherSubTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        List<TeacherSubTaskEntry> subTaskEntryList = super.getTeacherSubTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
        Map<Long, TeacherTaskProgress.SubTaskProgress> subTaskProgressMap = teacherTaskProgress.getSubTaskProgresses().stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        String subject = teacherDetail.getSubject().name();
        JexlEngine engine = new JexlEngine();
        MapContext context = new MapContext();
        context.set("main_teacher_subject", subject);
        for (TeacherSubTaskEntry teacherSubTaskEntry : subTaskEntryList) {
            if (teacherSubTaskEntry.getId() == null) {
                continue;
            }
            //根据模板计算积分信息
            TeacherTaskTpl.Reward rewardExpTpl = teacherTaskTpl.getSubTaskById(teacherSubTaskEntry.getId()).getProgress().getRewards().stream().filter(r -> Objects.equals(r.getUnit(), TeacherTaskTpl.RewardUnit.exp.name())).findFirst().orElse(null);
            engine.createExpression(rewardExpTpl.getExpression()).evaluate(context);
            Double exp = SafeConverter.toDouble(context.get("num"));

            List<TeacherSubTaskEntry.Reward> rewards = new ArrayList<>();
            rewards.add(new TeacherSubTaskEntry.Reward(1L, TeacherTaskTpl.RewardUnit.integral.name(), null, 1, "*完成人数"));
            rewards.add(new TeacherSubTaskEntry.Reward(2L, TeacherTaskTpl.RewardUnit.exp.name(), exp.intValue(), 2, "/班"));

            teacherSubTaskEntry.setRewards(rewards);
        }
        return subTaskEntryList;
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = super.getTeacherTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);

        int count = 1;
        for (TeacherTaskProgress.SubTaskProgress subTaskProgress : teacherTaskProgress.getSubTaskProgresses()) {
            TeacherTaskEntry.CrmProgress crmProgress = new TeacherTaskEntry.CrmProgress();
            crmProgress.setDesc("推荐检查作业" + count ++ + "次");
            crmProgress.setTarget(subTaskProgress.getProgress().getTarget());
            crmProgress.setCurr(subTaskProgress.getProgress().getCurr());
            crmProgressList.add(crmProgress);
        }

        List<String> crmTaskDesc = new ArrayList<>();
        crmTaskDesc.add("1.一个班一天内布置检查多次只计算一次");
        crmTaskDesc.add("2.每班每周前3次检查作业获得奖励");
        teacherTaskEntry.setCrmTaskDesc(crmTaskDesc);

        return teacherTaskEntry;
    }

    @Override
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return isOfflineOldWeekTask(teacherDetail, teacherTask, teacherTaskProgress);
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_ASSIGN_CHECK_HOMEWORK;
    }

    /**
     * 获取老师名下所有的组，包括包班制信息
     * @param teacherId
     * @return
     * @author zhouwei
     */
    private int getGroupNum(Long teacherId) {
        Set<Long> teacherIds = new HashSet<>();
        teacherIds.add(teacherId);
        List<Long> ids = teacherLoaderClient.loadSubTeacherIds(teacherId);
        if (CollectionUtils.isNotEmpty(ids)) {
            teacherIds.addAll(ids);
        }
        Set<Long> groupIds = new HashSet<>();
        teacherIds.forEach(id -> {
            Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader().findByGroupTeacherIdNotTerminal(id).getUninterruptibly();
            if (!groupMap.isEmpty()) {
                groupIds.addAll(groupMap.keySet());
            }
        });
        return groupIds.size();
    }

}
