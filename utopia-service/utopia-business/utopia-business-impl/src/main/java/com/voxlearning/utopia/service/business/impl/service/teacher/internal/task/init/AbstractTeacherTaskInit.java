package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherSubTaskEntry;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskTplDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.apache.commons.jexl2.JexlContext;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhouwei on 2018/9/3
 **/
abstract public class AbstractTeacherTaskInit implements TeacherTaskInit {

    @Inject
    private TeacherTaskTplDao teacherTaskTplDao;

    protected TeacherTaskTpl teacherTaskTpl;

    private static List<ObjectiveConfigType> primaryChinese = new ArrayList<ObjectiveConfigType>();
    private static List<ObjectiveConfigType> primaryEnglish = new ArrayList<ObjectiveConfigType>();
    private static List<ObjectiveConfigType> primaryMath = new ArrayList<ObjectiveConfigType>();
    private static List<ObjectiveConfigType> juniorEnglish = new ArrayList<ObjectiveConfigType>();

    static {
        primaryChinese.add(ObjectiveConfigType.BASIC_KNOWLEDGE);
        primaryChinese.add(ObjectiveConfigType.CHINESE_READING);
        primaryChinese.add(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        primaryChinese.add(ObjectiveConfigType.UNIT_QUIZ);
        primaryChinese.add(ObjectiveConfigType.WORD_RECOGNITION_AND_READING);

        primaryEnglish.add(ObjectiveConfigType.BASIC_APP);
        primaryEnglish.add(ObjectiveConfigType.INTELLIGENCE_EXAM);
        primaryEnglish.add(ObjectiveConfigType.INTELLIGENT_TEACHING);
        primaryEnglish.add(ObjectiveConfigType.NATURAL_SPELLING);
        primaryEnglish.add(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING);
        primaryEnglish.add(ObjectiveConfigType.UNIT_QUIZ);

        primaryMath.add(ObjectiveConfigType.INTELLIGENCE_EXAM);
        primaryMath.add(ObjectiveConfigType.INTELLIGENT_TEACHING);
        primaryMath.add(ObjectiveConfigType.KEY_POINTS);
        primaryMath.add(ObjectiveConfigType.MENTAL_ARITHMETIC);
        primaryMath.add(ObjectiveConfigType.UNIT_QUIZ);

        juniorEnglish.add(ObjectiveConfigType.LISTEN_PRACTICE);
        juniorEnglish.add(ObjectiveConfigType.EXAM);
    }

    /**
     * 检查作业的时间有效
     * @param createAt
     * @param checkedAt
     * @return
     */
    protected boolean checkHomeworkTime(Long createAt, Long checkedAt) {
        if (createAt == null) {
            return false;
        }
        Date validDate = DateUtils.stringToDate("2018-08-20",DateUtils.FORMAT_SQL_DATE);
        if (createAt < validDate.getTime()) {//如果作业的产生时间小于2018-08-20，则无效
            return false;
        }
        Date validDateChecked = DateUtils.stringToDate("2018-09-15",DateUtils.FORMAT_SQL_DATE);
        if (checkedAt != null && checkedAt < validDateChecked.getTime()) {//如果作业的检查时间小于2018-09-15，则无效
            return false;
        }
        return true;
    }

    /**
     * 根据学科判断，作业的形式是否满足需求
     * @param ktwelve
     * @param subject
     * @param objectiveConfigTypes
     * @return
     * @author
     */
    protected boolean checkHomeworkAndObjectiveIsMatch(Ktwelve ktwelve, String subject, String objectiveConfigTypes) {
        if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(objectiveConfigTypes) || null == ktwelve) {
            return false;
        }
        String[] type = objectiveConfigTypes.split(",");
        if (ktwelve == Ktwelve.PRIMARY_SCHOOL) {//小学
            if (Objects.equals(Subject.CHINESE.name(), subject)) {
                return contains(type, primaryChinese);
            } else if (Objects.equals(Subject.ENGLISH.name(), subject)) {
                return contains(type, primaryEnglish);
            } else if (Objects.equals(Subject.MATH.name(), subject)) {
                return contains(type, primaryMath);
            }
        } else if (ktwelve == Ktwelve.JUNIOR_SCHOOL || ktwelve == Ktwelve.SENIOR_SCHOOL) {//中学
            return true;
        }
        return false;
    }

    private boolean contains(String[] inTyps, List<ObjectiveConfigType> types) {
        for (ObjectiveConfigType objectiveConfigType : types) {
            for (String inType : inTyps) {
                if (objectiveConfigType.name().equals(inType)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void updateTaskInfo(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, Map<String,Object> varMap){
    }

    @Override
    public boolean isNeedRestart(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress) {
        return false;
    }

    @Override
    public boolean isPutOn(TeacherDetail teacherDetail, JexlContext calContext){
        return true;
    }

    @Override
    public boolean processNewVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, Map<String, Object> newVars){
        return true;
    }

    @Override
    public void taskComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {

    }

    @Override
    public void subTaskComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {

    }

    @Override
    public void oneProcessComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress){

    }

    @Override
    public String rewardTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.Reward reward){
        return "完成。";
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        TeacherTaskTpl.SubTask subTaskById = teacherTaskTpl.getSubTaskById(subTaskProgress.getId());
        return "，" + subTaskById.getDesc() + "完成。";
    }

    @Override
    public String rewardProgressComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.ProgressReward reward) {
        return "，完成一个进度。";
    }

    protected TeacherTaskTpl loadTaskTpl(Long tplId) {
        List<TeacherTaskTpl> teacherTaskTpls = teacherTaskTplDao.loadAll();
        for (TeacherTaskTpl tpl : teacherTaskTpls) {
            if (Objects.equals(tpl.getId(),tplId)) {
                return tpl;
            }
        }
        return null;
    }

    @Override
    public void initAddJexlContext(TeacherDetail teacherDetail, JexlContext jexlContext) {
    }

    @Override
    public void initTaskAndProgress(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, TeacherTask oldTeacherTask, TeacherTaskProgress oldTeacherTaskProgress) {
    }

    @Override
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress){
        return false;
    }

    @Override
    public void progressAddVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskEntry.Reward> teacherTaskEntryRewardCommon = this.createTeacherTaskEntryRewardCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardList(teacherTaskEntryRewardCommon);
        return teacherTaskEntry;
    }

    /**
     * 创建通过用的老师任务
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return
     * @author zhouwei
     */
    protected TeacherTaskEntry createTeacherTaskEntryCommon(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry entry = new TeacherTaskEntry();
        entry.setTaskId(teacherTask.getId());
        entry.setName(teacherTask.getName());
        entry.setButtonName(Objects.equals(teacherTask.getStatus(), TeacherTask.Status.FINISHED.name()) ? "已完成" : teacherTaskTpl.getButtonName());
        entry.setStatus(teacherTask.getStatus());
        entry.setType(teacherTaskTpl.getType());
        entry.setInstruction(teacherTaskTpl.getInstruction());
        entry.setTaskTplId(teacherTaskTpl.getId());
        entry.setSort(teacherTaskTpl.getSort());
        entry.setTaskParams(new HashMap<>());
        entry.setCrmIsDisplay(true);
        entry.setAutoReceive(teacherTaskTpl.getAutoReceive());
        entry.setCycle(teacherTaskTpl.getLoop());
        entry.setCycleUnit(teacherTaskTpl.getCycleUnit());
        entry.setRewardPrefix("奖励");
        entry.setProgressPrefix("进度");

        //设置任务的显示进度信息，只有一个子任务时才显示
        if (CollectionUtils.isNotEmpty(teacherTaskProgress.getSubTaskProgresses()) && teacherTaskProgress.getSubTaskProgresses().size() == 1) {
            TeacherTaskProgress.SubTaskProgress subTaskProgress = teacherTaskProgress.getSubTaskProgresses().get(0);
            TeacherTaskProgress.Progress progress = subTaskProgress.getProgress();
            if (progress != null) {
                TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();
                entryProgress.setCurr(progress.getCurr());
                entryProgress.setTarget(progress.getTarget());
                entryProgress.setQ(progress.getQ());
                entry.setProgress(entryProgress);

                if (progress.getCurr() != null && progress.getTarget() != null
                        && progress.getCurr() != 0 && progress.getTarget() != 0
                        && progress.getCurr() >= progress.getTarget()) {
                    entry.setButtonName("已完成");
                }
            }
        }

        //设置子任务信息
        if (CollectionUtils.isEmpty(teacherTaskProgress.getSubTaskProgresses())) {
            entry.setSubTaskProgressList(new ArrayList<>());
            entry.setSubTaskFinishedNum(0);
        } else {
            entry.setSubTaskProgressList(teacherTaskProgress.getSubTaskProgresses());
            entry.setSubTaskFinishedNum(teacherTaskProgress.getSubTaskProgresses().stream().filter(p -> Objects.equals(p.getStatus(), TeacherTask.Status.FINISHED.name())).count());
        }

        if (teacherTask.getExpireDate() != null && teacherTaskTpl.getLoop() == false) {
            entry.setDeadline(DateUtils.dateToString(teacherTask.getExpireDate(), "yyyy.MM.dd"));
        }
        if(teacherTaskProgress.getSubTaskNum() > 1){
            entry.setShowSubTask(true);
        }

        TeacherTaskProgress.SubTaskProgress onGoingSubTaks = teacherTaskProgress.getOngoingSubTask();
        if (null != onGoingSubTaks) {//如果有正在进行中的子任务，取一个
            TeacherTaskTpl.SubTask subTask = teacherTaskTpl.getSubTaskById(onGoingSubTaks.getId());
            if (null != subTask) {
                entry.setSkip(subTask.getSkip());
            }
        } else if (CollectionUtils.isNotEmpty(teacherTaskTpl.getSubTaskList())){//如果没有正在进行中的子任务，从前往后，取最后一个
            Map<String,Object> finalSubTaskSkip = null;
            for (TeacherTaskTpl.SubTask subTask : teacherTaskTpl.getSubTaskList()) {
                if (null != subTask.getSkip()) {
                    finalSubTaskSkip = subTask.getSkip();
                }
            }
            entry.setSkip(finalSubTaskSkip);
        }
        return entry;
    }

    /**
     * 创建通用的奖励展示
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return
     * @author zhouwei
     */
    protected List<TeacherTaskEntry.Reward> createTeacherTaskEntryRewardCommon(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        List<TeacherTaskEntry.Reward> rewardEntryList = new ArrayList<>();
        List<TeacherTaskProgress.Reward> rewardsAll = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teacherTaskProgress.getRewards())) {
            rewardsAll.addAll(teacherTaskProgress.getRewards());//总任务奖励添加
        }
        List<TeacherTaskProgress.SubTaskProgress> subTaskProgresses = teacherTaskProgress.getSubTaskProgresses();
        subTaskProgresses.stream().forEach(p -> {
            if (CollectionUtils.isNotEmpty(p.getRewards())) {
                rewardsAll.addAll(p.getRewards());
            }
        });//子任务奖励添加
        subTaskProgresses.stream().forEach(p -> {//进度任务奖励添加
            if (p == null) {
                return;
            }
            TeacherTaskProgress.Progress progressTmp = p.getProgress();
            if (progressTmp == null || p.getProgress().getRewards() == null) {
                return;
            }
            p.getProgress().getRewards().stream().forEach(r -> {
                TeacherTaskProgress.Reward tmpReward = new TeacherTaskProgress.Reward();
                tmpReward.setOpen(r.getOpen());
                tmpReward.setUnit(r.getUnit());
                if (r.getOpen() == false) {
                    tmpReward.setValue(r.getValue());
                } else {
                    tmpReward.setValue(r.getValue() * progressTmp.getTarget());
                }
                rewardsAll.add(tmpReward);
            });
        });
        rewardsAll.stream().collect(Collectors.groupingBy(r -> r.getUnit())).forEach((unit, rewards) -> {

            if (Objects.equals(unit, TeacherTaskTpl.RewardUnit.cash.name())) {//临时去掉列表页的现金部分的展示
                return;
            }

            StringBuilder suffix = new StringBuilder();
            //如果有不能直接结算出来的，末尾添加+
            rewards.stream().forEach(r -> {
                if (r.getOpen() == false) {
                    if (suffix.toString().length() == 0) {
                        suffix.append("+");
                    }
                }
            });
            int rewardNum = rewards.stream().mapToInt(TeacherTaskProgress.Reward::getValue).sum();
            if (rewardNum == 0) {
                rewardNum = 1;
                if (suffix.toString().length() == 0) {
                    suffix.append("+");
                }
            }
            TeacherTaskEntry.Reward rewardEntry = new TeacherTaskEntry.Reward();
            rewardEntry.setContent(rewardNum + suffix.toString());
            rewardEntry.setUnit(unit);
            rewardEntry.setQuantity("");
            rewardEntryList.add(rewardEntry);
        });
        return rewardEntryList;
    }

    @Override
    public List<TeacherSubTaskEntry> getTeacherSubTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return this.createTeacherSubTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
    }

    /**
     * 获取通用的子任务列表信息
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return
     */
    protected List<TeacherSubTaskEntry> createTeacherSubTaskEntryCommon(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        if (CollectionUtils.isEmpty(teacherTaskTpl.getSubTaskList())) {
            return new ArrayList();
        }
        Map<Long, TeacherTaskTpl.SubTask> subTaskMap = teacherTaskTpl.getSubTaskList().stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
        List<TeacherSubTaskEntry> entryList = new ArrayList<>();
        for (TeacherTaskProgress.SubTaskProgress subTaskProgress : teacherTaskProgress.getSubTaskProgresses()) {
            TeacherTaskTpl.SubTask subTask = subTaskMap.get(subTaskProgress.getId());
            if (null == subTask) {
                continue;
            }
            TeacherSubTaskEntry entry = new TeacherSubTaskEntry();
            entry.setId(subTask.getId());
            entry.setName(subTask.getDesc());
            entry.setStatus(subTaskProgress.getStatus());
            entry.setSkip(subTask.getSkip());

            if (null != subTaskProgress.getProgress()) {
                TeacherSubTaskEntry.Progress progress = new TeacherSubTaskEntry.Progress();
                progress.setCurr(subTaskProgress.getProgress().getCurr());
                progress.setTarget(subTaskProgress.getProgress().getTarget());
                progress.setQ(subTaskProgress.getProgress().getQ());
                entry.setProgress(progress);
            }
            entryList.add(entry);
        }

        /** 特殊逻辑，如果任务完成后再发钱，或者发园丁豆，子任务加一条 **/
        TeacherTaskProgress.Reward rewardCash = null;
        if (null != teacherTaskProgress.getRewards()) {
            teacherTaskProgress.getRewards().stream().filter(r -> TeacherTaskTpl.RewardUnit.cash.name().equals(r.getUnit())).findAny().orElse(null);
        }
        TeacherTaskProgress.Reward rewardIntegral = null;
        if (null != teacherTaskProgress.getRewards()) {
            teacherTaskProgress.getRewards().stream().filter(r -> TeacherTaskTpl.RewardUnit.integral.name().equals(r.getUnit())).findAny().orElse(null);
        }
        if (null != rewardCash) {
            TeacherSubTaskEntry entry = new TeacherSubTaskEntry();
            entry.setStatus("REWARD");
            entry.setName("赢取教学流量");
            //entryList.add(entry); //临时去掉任务进度页的  赢取教学流量展示
        } else if (null != rewardIntegral && rewardIntegral.getValue() != null && rewardIntegral.getValue() > 0) {
            TeacherSubTaskEntry entry = new TeacherSubTaskEntry();
            entry.setStatus("REWARD");
            entry.setName("赢取" + rewardIntegral.getValue() + "园丁豆");
            entryList.add(entry);
        }
        return entryList;
    }

    @Override
    public boolean isDisplay(TeacherDetail teacherDetail, List<TeacherTask> teacherTaskList, List<TeacherTaskProgress> teacherTaskProgressList, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return true;
    }

    @Override
    public boolean isStoreLog(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return true;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return null;
    }

    public Date calcExpireDate(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        return null;
    }

    // 测试环境3月13 线上3月18
    private static final Date OFFLINE_OLD_WEEK_TASK = new Date(RuntimeMode.ge(Mode.STAGING) ? 1552838400000L : 1552406400000L);

    /**
     * 新的一周下线老的周任务
     */
    boolean isOfflineOldWeekTask(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        if (new Date().getTime() >= OFFLINE_OLD_WEEK_TASK.getTime()) {
            String status = TeacherTask.Status.CANCEL.name();
            if (Objects.equals(teacherTask.getStatus(), status)) {
                return false;
            }
            Date now = new Date();
            teacherTask.setStatus(status);
            teacherTask.setCancelDate(now);
            teacherTaskProgress.setStatus(status);
            return true;
        }
        return false;
    }
}
