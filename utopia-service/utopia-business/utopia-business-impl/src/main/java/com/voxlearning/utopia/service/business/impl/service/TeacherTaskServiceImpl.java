package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.athena.api.SummerMarketLoadSummaryService;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.misc.TeacherInvitationConfig;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.service.business.api.TeacherTaskService;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskProgressDao;
import com.voxlearning.utopia.service.business.impl.loader.TeacherTaskLoaderImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator.TeacherTaskEvaluator;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator.TeacherTaskHandlerEvaluator;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.TeacherTaskHandlerInit;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.TeacherTaskInit;
import com.voxlearning.utopia.service.business.impl.support.FinalReviewConfigUtils;
import com.voxlearning.utopia.service.business.impl.support.WinterWorkConfigUtils;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;
import static com.voxlearning.alps.lang.util.MapMessage.successMessage;
import static com.voxlearning.alps.repackaged.org.apache.commons.collections4.IteratorUtils.forEach;
import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils.isTrue;
import static java.util.stream.Collectors.toMap;

@Named
@ExposeService(interfaceClass = TeacherTaskService.class)
public class TeacherTaskServiceImpl implements TeacherTaskService {

    /** Logger **/
    private static Logger logger = LoggerFactory.getLogger(TeacherTaskServiceImpl.class);

    @Inject private TeacherTaskLoaderImpl ttLoader;
    @Inject private TeacherLoaderClient teacherLoader;
    @Inject private TeacherLevelServiceClient teacherLvlSrv;
    @Inject private SchoolExtServiceClient schoolExtService;
    @Inject private AsyncInvitationServiceClient invitationService;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject private TeacherTaskProgressDao progressDao;
    @Inject private TeacherTaskDao taskDao;
    @Inject private TeacherTaskHandlerInit teacherTaskHandlerInit;
    @Inject private TeacherTaskHandlerEvaluator teacherTaskHandlerEvaluator;
    @Inject private TeacherRookieTaskServiceImpl teacherRookieTaskService;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @ImportService(interfaceClass = SummerMarketLoadSummaryService.class)
    private SummerMarketLoadSummaryService stuAuthQueryService;

    @Inject
    private UserAuthQueryServiceClient userAuthQueryServiceClient;

    @Override
    public void updateProgress(Long teacherId, TeacherTaskTpl.TplEvaluatorEvent tplEvaluatorEvent, Map<String, Object> newVarMap) {
        // 不一定用的到的懒加载，注释
        LazyInitializationSupplier<Map<Long,TeacherTaskProgress>> progressMapSupplier = new LazyInitializationSupplier<>(
                () -> ttLoader.loadTaskProgressMap(teacherId));
        LazyInitializationSupplier<TeacherDetail> tdSupplier = new LazyInitializationSupplier<>(
                () -> teacherLoader.loadTeacherDetail(teacherId));

        // 循环所有任务，及其任务关联的子任务
        // 只看进行中的
        forEach(ttLoader.loadAndInitTaskList(teacherId).iterator(),task -> {
            Long tplId = task.getTplId();
            TeacherTaskTpl tpl = ttLoader.loadTaskTpl(tplId);
            if(tpl == null) {
                return;
            }
            // JEXL engine..
            TeacherTaskProgress progress = progressMapSupplier.initializeIfNecessary().get(task.getId());
            if (progress == null) {
                return;
            }

            //具体任务的一些处理工具
            TeacherTaskInit teacherTaskInit = teacherTaskHandlerInit.getHandler(tpl.getTpl());
            if (tplEvaluatorEvent == TeacherTaskTpl.TplEvaluatorEvent.UPDATE_TASK_INFO) {/**这个必须放第一个**/
                //一些事件只是用来触发更新任务的一些信息，比如任务的target，status等，并不改变任务的任务进度已经上下人，比较特殊的一段流程
                //更新完成后直接退出即可
                teacherTaskInit.updateTaskInfo(tdSupplier.initializeIfNecessary(), task, progress, newVarMap);
                return;
            }

            //针对每个事件的处理工具
            TeacherTaskEvaluator evaluator = teacherTaskHandlerEvaluator.getHandler(tplEvaluatorEvent);
            //根据特定任务，对newVarMap做处理，并返回是否可以执行
            boolean isGo = teacherTaskInit.processNewVars(tdSupplier.initializeIfNecessary(), progress, newVarMap);
            if (!isGo) {
                //如果这个事件不符合该任务的条件，则不再执行
                return;
            }
            if (null == evaluator) {
                //事件没有相关处理的类，也过滤掉
                logger.error("this evaluator is not exsit . exsit : {}", tplEvaluatorEvent.name());
                return;
            }
            if (newVarMap == null || newVarMap.isEmpty()) {
                //newVarMap如果是空，则不处理
                return;
            }
            // 非进行中的任务状态返回
            if (!progress.isOnGoing()) {
                return;
            }

            AtomicInteger finishSubNum = new AtomicInteger(0);
            // 校验每个子任务，看是否达到了完成条件
            tpl.getSubTasksForEach(st -> {
                try {//获得子任务的变量和进度数据
                    TeacherTaskProgress.SubTaskProgress subTaskProgress = progress.getSubTaskProgress(st.getId());
                    if(subTaskProgress == null){
                        logger.error("TT:The progress of sub task is lost,tchId:{},tplId:{}", teacherId,tplId);
                        return;
                    }

                    // 完成的就不看了，这一步要在前面，不然任务就完成不了了
                    if(subTaskProgress.isFinish()) {
                        finishSubNum.incrementAndGet();
                        return;
                    }

                    // 计算类型不同的也要跳过
                    if(!Objects.equals(st.getCalType(), tplEvaluatorEvent.name())) {
                        return;
                    }

                    //根据当前任务与子任务类型，初始化所需要的一些上下文变量
                    teacherTaskInit.progressAddVars(tdSupplier.initializeIfNecessary(), progress, subTaskProgress);

                    //根据当前事件类型，初始化所需要的一些上下文变量信息
                    Map<String, Object> varMap = evaluator.mergeVar(subTaskProgress.getVarMap(), newVarMap);

                    /** 更新任务进度 **/
                    if (isTrue(st.getShowProgress())) {
                        int target = subTaskProgress.getProgress().getTarget();
                        int lastCurr = subTaskProgress.getProgress().getCurr();
                        int currValEvaluate = evaluator.evaluate(st.getProgress().getProExpr(), varMap, "curr", 0);//这里会把本次curr变量写入vars
                        int currVal = Math.min(toInt(currValEvaluate), target);// 最大值不能超过target
                        final int currValFinal = currVal;
                        subTaskProgress.getProgress().setCurr(currVal);//设置当前进度
                        /** 进度任务完成了，可以做一些事情 **/
                        if (currValFinal > lastCurr) {
                            teacherTaskInit.oneProcessComplete(tdSupplier.initializeIfNecessary(), progress, subTaskProgress);
                        } else if (currValEvaluate > lastCurr) {//主要兼容一些任务完成了，但是可以继续记录次数的。如分享文章，点评奖励等
                            teacherTaskInit.oneProcessComplete(tdSupplier.initializeIfNecessary(), progress, subTaskProgress);
                        }
                        /** 给任务进度发奖励 **/
                        if (CollectionUtils.isNotEmpty(subTaskProgress.getProgress().getRewards())) {//进度发生变化，并且上次进度小于进度最大值
                            subTaskProgress.getProgress().getRewards().forEach(r ->
                                rewardProgress(tdSupplier.get(), task, tpl, evaluator, st, varMap, r, target, currValFinal, teacherTaskInit, progress, subTaskProgress)
                            );
                        }
                    }

                    //判断是否满足前置条件
                    boolean isPrevCondFinish = true;
                    if (st.getPrevCondTask() != null) {
                        TeacherTaskProgress.SubTaskProgress prevTask = progress.getSubTaskProgress(st.getPrevCondId());
                        isPrevCondFinish = prevTask != null && prevTask.isFinish();
                    }
                    //计算当前子任务任务是否完成
                    Boolean achieve = evaluator.evaluate(st.getExpression(), varMap, "result", Boolean.FALSE);//这里会把本次result变量写入vars

                    /** 更新子任务任务状态 **/
                    if (isPrevCondFinish && achieve) {//如果任务完成了，并且前置任务也完成了
                        // 如果满足了任务条件，并且前置任务也是成功的
                        subTaskProgress.finish();
                        finishSubNum.incrementAndGet();

                        /** 给子任务发奖励 **/
                        if (CollectionUtils.isNotEmpty(subTaskProgress.getRewards())) {
                            subTaskProgress.getRewards().forEach(reward ->
                                rewardSubTask(tdSupplier.get(), task, tpl, evaluator, st, varMap, reward, teacherTaskInit, progress, subTaskProgress)
                            );
                        }

                        /** 子任务完成了，可以做一些事件 **/
                    }
                } catch (Throwable t) {
                    logger.error("TT:Process sub task error!tchId:{},tplId:{}",teacherId,tplId,t);
                }
            });

            try {
                /** 更新总任务任务状态 **/
                if (finishSubNum.get() >= tpl.getSubTaskNum()) {//如果子任务完成数量大于等于模板所有子任务，则设置任务为完成状态
                    /** 给总任务发奖励 **/
                    if (CollectionUtils.isNotEmpty(progress.getRewards())) {
                        progress.getRewards().forEach(r -> rewardTask(task, tdSupplier.get(), tpl, r, teacherId, r.getValue(), teacherTaskInit, progress));
                    }
                    Date finishedDate = new Date();
                    progress.setStatus(TeacherTask.Status.FINISHED.name());
                    task.setStatus(TeacherTask.Status.FINISHED.name());
                    task.setFinishedDate(finishedDate);
                    taskDao.upsert(task);
                    /** 总子任务完成了，可以做一些事件 **/
                }
                // 更新进度数据里面的变量
                progressDao.upsert(progress);
            } catch (Throwable e) {
                logger.error("TT update error: taskId:{}, progress: {}", task.getId(), JsonUtils.toJson(progress));
            }
        });
    }

    /**
     * 给总任务发奖励
     * @author zhouwei
     */
    private void rewardTask(TeacherTask teacherTask, TeacherDetail teacherDetail, TeacherTaskTpl tpl, TeacherTaskProgress.Reward reward, Long teacherId, Integer num, TeacherTaskInit teacherTaskInit, TeacherTaskProgress teacherTaskProgress) {
        if (reward.getReceived() == true) {//领取过，不在发奖励
            return;
        }
        boolean result = false;
        try {
            String comment = teacherTaskInit.rewardTaskComment(teacherDetail, teacherTaskProgress, reward);
            result = reward(teacherTask, tpl, null, reward.getUnit(), teacherId, num, comment);
            reward.setReceived(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            //奖励发放后，记录日志
            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "teacherId", teacherId, "op", "teacher_task", "result", result, "unit", reward.getUnit(), "num", num,
                    "tplId", tpl.getId(), "rewardId", reward.getId(), "type", "rewardTask");
            LogCollector.info("backend-general", logMap);
        } catch (Exception e){}
    }

    /**
     * 给子任务发奖励
     * @param teacherDetail
     * @param task
     * @param tpl
     * @param evaluator
     * @param st
     * @param varMap
     * @param reward
     * @author zhouwei
     */
    private void rewardSubTask(TeacherDetail teacherDetail, TeacherTask task, TeacherTaskTpl tpl, TeacherTaskEvaluator evaluator, TeacherTaskTpl.SubTask st,
                               Map<String, Object> varMap, TeacherTaskProgress.Reward reward, TeacherTaskInit teacherTaskInit, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {
        if (reward.getReceived() == true) {//领取过，不在发奖励
            return;
        }
        Map<Long, TeacherTaskTpl.Reward> rewardMap = st.getRewards().stream().collect(toMap(k -> k.getId(), v -> v));
        Object rewardValue = reward.getValue() == null ? 0d : reward.getValue();//
        if (false == reward.getOpen()) {//需要通过计算，才知道奖励的值
            TeacherTaskTpl.Reward rewardTpl = rewardMap.get(reward.getId());
            rewardValue = evaluator.evaluate(rewardTpl.getExpression(), varMap, "num", 0.0d);
        }
        Double value = SafeConverter.toDouble(rewardValue);
        if (value.intValue() <= 0) {
            logger.error("TT:Reward config error!tplId:{},stId:{},rewardId:{}",task.getId(), st.getId(), reward.getId());
            return;
        }

        //发奖励
        boolean result = false;
        try {
            String comment = teacherTaskInit.rewardSubTaskComment(teacherDetail, teacherTaskProgress, subTaskProgress, reward);
            result = reward(task, tpl, st, reward.getUnit(), teacherDetail.getId(), value.intValue(), comment);
            reward.setReceived(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            //奖励发放后，记录日志
            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(),"teacherId", teacherDetail.getId(), "op", "teacher_task", "result", result, "unit", reward.getUnit(), "num", value.intValue(),
                    "tplId", tpl.getId(), "subTaskId", st.getId(), "rewardId", reward.getId(), "type", "rewardSubTask");
            LogCollector.info("backend-general", logMap);
        } catch (Exception e){}
    }

    /**
     * 给任务进度发奖励
     * @author zhouwei
     */
    private void rewardProgress(TeacherDetail teacherDetail, TeacherTask task, TeacherTaskTpl tpl, TeacherTaskEvaluator evaluator, TeacherTaskTpl.SubTask st,
                                Map<String, Object> varMap, TeacherTaskProgress.ProgressReward reward, int target, int curr, TeacherTaskInit teacherTaskInit, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {
        int receivedCurr = reward.getReceivedCurr();
        if (receivedCurr >= target || receivedCurr >= curr) {//上次领取的大于目标最大值，或者上次领取的大于当前进度，则不发
            return;
        }
        Map<Long, TeacherTaskTpl.Reward> rewardMap = st.getProgress().getRewards().stream().collect(toMap(k -> k.getId(), v -> v));
        Object rewardValue = reward.getValue() == null ? 0d : reward.getValue();//奖励值
        if (false == reward.getOpen()) {//需要通过计算，才知道奖励的值
            TeacherTaskTpl.Reward rewardTpl = rewardMap.get(reward.getId());
            try {
                rewardValue = evaluator.evaluate(rewardTpl.getExpression(), varMap, "num", 0.0d);
            } catch (Exception e) {
                logger.info("rewardProgress evaluate error,teacherId:{} taskId:{} tplId:{}", teacherDetail.getId(), task.getId(), tpl.getId());
                try {
                    Map<String, String> logMap = MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "teacherId", teacherDetail.getId(),
                            "task", JsonUtils.toJson(task),
                            "subTask", JsonUtils.toJson(st),
                            "varMap", varMap,
                            "op", "teacher_task",
                            "type", "rewardProgressEvaluate"
                    );
                    LogCollector.info("backend-general", logMap);
                } catch (Exception ee) {
                }
            }
        }
        Double value = SafeConverter.toDouble(rewardValue);
        if (value.intValue() <= 0) {
            logger.error("TT:Reward config error!tplId:{},stId:{},rewardId:{}",task.getId(), st.getId(), reward.getId());
            return;
        }

        //发奖励
        boolean result = false;
        try {
            String comment = teacherTaskInit.rewardProgressComment(teacherDetail, teacherTaskProgress, subTaskProgress, reward);
            result = reward(task, tpl, st, reward.getUnit(), teacherDetail.getId(), value.intValue(), comment);
            reward.setReceivedCurr(curr);//存入已经领取了多少进度值
            reward.setReceivedValue(reward.getReceivedValue() + value.intValue());////存入已经领取了多少值
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            //奖励发放后，记录日志
            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "teacherId", teacherDetail.getId(), "op", "teacher_task", "result", result, "unit", reward.getUnit(), "num", value.intValue(),
                    "tplId", tpl.getId(), "subTaskId", st.getId(), "rewardId", reward.getId(), "type", "rewardProgress");
            LogCollector.info("backend-general", logMap);
        } catch (Exception e){}
    }

    /**
     * 发奖励
     * @param tpl
     * @param unit
     * @param teacherId
     * @param num
     * @author zhouwei
     */
    private boolean reward(TeacherTask teacherTask, TeacherTaskTpl tpl, TeacherTaskTpl.SubTask subTask, String unit, Long teacherId, Integer num, String comment) {
        Long taskId = teacherTask == null ? null : teacherTask.getId();
        Long tplId = tpl == null ? null : tpl.getId();
        Long subTplId = subTask == null ? null : subTask.getId();
        if (Objects.equals(TeacherTaskTpl.RewardUnit.exp.name(), unit)) {
            return sendExpReward(teacherId, taskId, tplId, subTplId, tpl.getName() + comment, num);
        } else if (Objects.equals(TeacherTaskTpl.RewardUnit.integral.name(), unit)) {
            return sendIntegralReward(teacherId, taskId, tplId, subTplId, tpl.getName() + comment, num);
        } else if (Objects.equals(TeacherTaskTpl.RewardUnit.cash.name(), unit)) {
            return sendWirelessCharging(teacherId, tpl.getName(), num);
        }
        return false;
    }

    /**
     * 填加计算用到的常量
     * @param td
     * @param varMap
     */
    private void addConstantVar(TeacherDetail td,Map<String,Object> varMap){
        varMap.computeIfAbsent("cityLevel", key -> {
            TeacherInvitationConfig config = invitationService.getAsyncInvitationService()
                    .queryTeacherInvitationConfig(td.getCityCode())
                    .getUninterruptibly();

            return Optional.ofNullable(config).map(c -> c.getCityLevel()).orElse("");
        });
    }

    /**
     * 发经验
     * @param teacherId
     * @param num
     */
    public boolean sendExpReward(Long teacherId, Long teacherTaskId, Long tplId, Long subTaskId, String name, Integer num) {
        Integer subTaskIdInt = subTaskId == null ? null : subTaskId.intValue();
        MapMessage chgExpResult = teacherLvlSrv.addExp(teacherId, num, "老师任务奖励!" + name, null, tplId.intValue(), subTaskIdInt);
        if (!chgExpResult.isSuccess()) {
            logger.error("TT:Change exp error!tId:{}, taskName:{}, reward:{}, detail:{}", teacherId, name, num, chgExpResult.getInfo());
            return false;
        }
        return true;
    }

    /**
     * 发学豆
     * @param teacherId
     * @param name
     * @param num
     */
    public boolean sendIntegralReward(Long teacherId, Long teacherTaskId, Long tplId, Long subTaskId, String name, Integer num) {
        // 如果是小学老师换算成学豆
        TeacherDetail td = teacherLoader.loadTeacherDetail(teacherId);
        /** 小学学豆乘以10 **/
        if (td.isPrimarySchool()) {
            num = num * 10;
        }
        IntegralType integralType = TeacherTaskTpl.getByTplId(tplId);
        IntegralHistory integralHistory = new IntegralHistory(teacherId, integralType, num);
        integralHistory.setComment("老师任务奖励!" + name);
        if (subTaskId != null) {
            integralHistory.setHomeworkId(SafeConverter.toString(subTaskId));
        }
        MapMessage chgIntegralResult = userIntegralService.changeIntegral(integralHistory);
        if (!chgIntegralResult.isSuccess()) {
            logger.error("TT:Change integral error!tId:{}, taskName:{}, reward:{}, detail:{}", teacherId, name, num, chgIntegralResult.getInfo());
            return false;
        }
        return true;
    }

    /**
     * 发现金
     * @param teacherId
     * @param taskName
     * @param reward
     */
    public boolean sendWirelessCharging(Long teacherId, String taskName, Integer reward) {
        // 获取老师手机号
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacherId);
        if (ua == null || StringUtils.isBlank(ua.getSensitiveMobile())) {
            logger.error("TT:Charging failed!ua is error. tId:{},taskName:{},reward:{}", teacherId, taskName, reward);
            return false;
        }

        Boolean result = wirelessChargingServiceClient.getWirelessChargingService()
                .saveWirelessCharging(
                        teacherId,
                        ChargeType.TEACHER_TASK,
                        ua.getSensitiveMobile(),
                        reward * 100, // 金额是分
                        MessageFormat.format("老师好，{0}元教学补贴充值成功。做任务，得教学补贴，享积分，赢园丁豆，快来参加吧！", reward),//
                        taskName)
                .getUninterruptibly();

        if (!isTrue(result)) {
            logger.error("TT:Charging failed!tId:{},taskName:{},reward:{}", teacherId, taskName, reward);
            return false;
        }

        try {
            String msgContent = "恭喜您获得" + reward + "元教学补贴，将在任务完成后的72小时内，发送到您的账户，注意查收哦！如有问题请联系客服咨询：400-160-1717";
            // 发送App消息
            AppMessage msg = new AppMessage();
            msg.setUserId(teacherId);
            msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
            msg.setContent(msgContent);
            msg.setTitle("教学补贴奖励通知");
            msg.setCreateTime(new Date().getTime());
            messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

            // 发送pc消息
            teacherLoader.sendTeacherMessage(teacherId, msgContent);

            // 发送push
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("s", TeacherMessageType.ACTIVIY.name());
            jpushExtInfo.put("key", "j");
            jpushExtInfo.put("t", "h5");

            TeacherDetail td = teacherLoader.loadTeacherDetail(teacherId);
            appMessageServiceClient.sendAppJpushMessageByIds(
                    msgContent,
                    AppMessageUtils.getMessageSource("17Teacher", td),
                    Collections.singletonList(teacherId),
                    jpushExtInfo);

            String mobile = sensitiveUserDataServiceClient.showUserMobile(teacherId, "sendTeacherTaskReward", "teacherTask");
            if (mobile != null) {
                String smsContent = "老师您好，恭喜您完成" + taskName + "获得" + reward + "元教学补贴，将于近期为您充值，注意查收哦！";
                smsServiceClient.createSmsMessage(mobile)
                        .content(smsContent)
                        .type(SmsType.TEACHER_TASK_REWARD_NOTIFY.name())
                        .send();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public MapMessage receiveTask(Long teacherId, Long taskId) {
        TeacherTask teacherTask = ttLoader.internalLoadTaskList(teacherId)
                .stream()
                .filter(t -> Objects.equals(t.getId(),taskId))
                .findFirst()
                .orElse(null);
        if(teacherTask == null)
            return errorMessage("老师任务不存在!");

        Map<Long, TeacherTaskTpl> tplMap = ttLoader.loadTaskTplMap();

        // 必须是INIT状态的才能领取
        if(!Objects.equals(teacherTask.getStatus(), TeacherTask.Status.INIT.name())){
            return MapMessage.errorMessage("该任务不能领取!");
        }

        TeacherDetail teacherDetail = teacherLoader.loadTeacherDetail(teacherId);
        TeacherTaskTpl teacherTaskTpl = tplMap.get(teacherTask.getTplId());
        boolean putOn = isPutOn(teacherDetail, teacherTaskTpl);
        if (putOn == false) {
            return errorMessage("老师没有领取该任务的资格");
        }

        TeacherTaskProgress ttp = ttLoader.loadTaskProgressById(teacherId,taskId);
        if(ttp == null){
            return MapMessage.errorMessage("进度数据丢失!");
        }

        TeacherTaskInit taskInit = teacherTaskHandlerInit.getHandler(teacherTaskTpl.getTpl());
        Date calcExpireDate = taskInit.calcExpireDate(teacherDetail, teacherTask, ttp);
        if (calcExpireDate != null) {
            teacherTask.setExpireDate(calcExpireDate);
            ttp.setExpireTime(calcExpireDate.getTime());
        } else {
            // 设置过期时间
            Optional.ofNullable(ttLoader.loadTaskTpl(teacherTask.getTplId()))
                    .filter(tpl -> toInt(tpl.getActiveTime()) > 0)
                    .map(t -> new Date((DateUtils.nextDay(DayRange.current().getStartDate(), t.getActiveTime()).getTime() - 1000)))
                    .ifPresent(expire -> {
                        teacherTask.setExpireDate(expire);
                        ttp.setExpireTime(expire.getTime());
                    });
        }

        Date receiveDate = new Date();
        teacherTask.setStatus(TeacherTask.Status.ONGOING.name());
        ttp.setStatus(TeacherTask.Status.ONGOING.name());
        ttp.setReceiveDate(DateUtils.dateToString(receiveDate, DateUtils.FORMAT_SQL_DATETIME));
        ttp.setReceiveTime(receiveDate.getTime());
        teacherTask.setReceiveDate(receiveDate);
        taskDao.upsert(teacherTask);
        progressDao.upsert(ttp);

        return MapMessage.successMessage();
    }

    @SuppressWarnings({"unchecked"})
    private JexlContext initNewTaskContext(TeacherDetail td, TeacherTaskTpl tpl){
        LazyContext context = new LazyContext();

        //根据老师所属的城市，加载这个城市的奖励信息
        LazyInitializationSupplier<TeacherInvitationConfig> configSupplier = new LazyInitializationSupplier<>(() -> {
            return invitationService.getAsyncInvitationService()
                    .queryTeacherInvitationConfig(td.getCityCode())
                    .getUninterruptibly();
        });

        //从mongo中的vox_school_ext_info，根据它的模板ID获取奖励信息
        context.set("rewardFromSchool", new LazyInitializationSupplier<>(() -> {
            SchoolExtInfo schoolExtInfo = schoolExtService.getSchoolExtService()
                    .loadSchoolExtInfo(td.getTeacherSchoolId())
                    .getUninterruptibly();

            return Optional.ofNullable(schoolExtInfo)
                    .map(SchoolExtInfo::getRewardMap)
                    .map(rm -> rm.getOrDefault("tpl" + Long.toString(tpl.getId()),0))
                    .orElse(0);
        }));

        //从mysql中的vox_teacher_invitation_config中获取新手任务的奖励，需要区分不同的学段以及不同的学科信息
        context.set("rookieReward", new LazyInitializationSupplier<>(() -> {
            return Optional.ofNullable(configSupplier.initializeIfNecessary())
                    .map(c -> {
                        try {
                            String attrName;
                            if (td.getSubject() == null) {
                                return "0";
                            } else if (td.isPrimarySchool()) {
                                attrName = td.getSubject().name().toLowerCase();
                            } else if (td.isJuniorTeacher()) {
                                attrName = "middle" + StringUtils.capitalize(td.getSubject().name().toLowerCase());
                            } else {
                                return "0";
                            }

                            return BeanUtils.getProperty(c, attrName);
                        } catch (Exception e) {
                            return "0";
                        }
                    })
                    .orElse("0");
        }));

        context.set("cityLevel", new LazyInitializationSupplier<>(() -> {
            return Optional.ofNullable(configSupplier.initializeIfNecessary()).map(c -> c.getCityLevel()).orElse("");
        }));

        //返回老师下面所有的认证学生的数量
        context.set("authNum", new LazyInitializationSupplier<>(() -> {
            // 考虑包班制，所有的都要算上，主副账号的所有ID
            List<Long> allTeacherIds = teacherLoader.loadSubTeacherIds(td.getId());
            allTeacherIds.add(td.getId());

            LongAdder authNum = new LongAdder();
            try {
                allTeacherIds.forEach(tId -> {
                    //获取该老师的学生ID，但是过滤掉毕业班的学生ID
                    List<Long> stuIds = studentLoaderClient.loadStudentIdsNotTerminal(tId);

                    SchoolLevel schoolLvl;
                    if (td.isPrimarySchool()) {
                        schoolLvl = SchoolLevel.JUNIOR;
                    } else if (td.isJuniorTeacher()) {
                        schoolLvl = SchoolLevel.MIDDLE;
                    } else if (td.isSeniorTeacher()) {
                        schoolLvl = SchoolLevel.HIGH;
                    } else {
                        schoolLvl = null;
                    }

                    //获取认证的学生ID
                    List<Long> authStudentIds = userAuthQueryServiceClient.getUserAuthQueryService().filterAuthedStudents(stuIds, schoolLvl);
                    if (!CollectionUtils.isEmpty(authStudentIds)) {
                        authNum.add(authStudentIds.size());
                    }

                });
            } catch (Throwable t) {
                // 如果发生了异常，加一个特别大的数，不能让未满足条件的人领上
                authNum.add(Integer.MAX_VALUE);
                logger.error(t.getMessage(), t);
            }

            return authNum.intValue();
        }));

        context.set("basicReviewReward", new LazyInitializationSupplier<>(() -> {
            Long teacherSchoolId = td.getTeacherSchoolId();
            FinalReviewConfigUtils.FinalReviewReward finalReviewReward = FinalReviewConfigUtils.getFinalReviewReward(teacherSchoolId);
            return finalReviewReward.getBasicReview();
        }));

        context.set("termReviewReward", new LazyInitializationSupplier<>(() -> {
            Long teacherSchoolId = td.getTeacherSchoolId();
            FinalReviewConfigUtils.FinalReviewReward finalReviewReward = FinalReviewConfigUtils.getFinalReviewReward(teacherSchoolId);
            return finalReviewReward.getTermReview();
        }));

        context.set("winterWorkReward", new LazyInitializationSupplier<>(() -> {
            Integer cityId = td.getCityCode();
            WinterWorkConfigUtils.WinterWorkReward winterWorkReward = WinterWorkConfigUtils.getWinterWorkReward(cityId);
            return winterWorkReward.getWinterWork();
        }));

        context.set("rookieTaskFinish", new LazyInitializationSupplier<>(() -> {
            return teacherRookieTaskService.rookieFinished(td.getId());
        }));

        //学段信息
        context.set("ktwelve", Optional.ofNullable(td.getKtwelve()).map(k -> k.name()).orElse(""));
        context.set("schoolId", SafeConverter.toLong(td.getTeacherSchoolId()));

        //副账号逻辑，加载主账号与副账号的学科信息
        context.set("subjects", new LazyInitializationSupplier<>(() -> {
            List<String> subjects = new ArrayList();
            subjects.add(Optional.ofNullable(td.getSubject()).map(s -> s.name()).orElse(""));
            List<Long> subIds = teacherLoader.loadSubTeacherIds(td.getId());
            if (CollectionUtils.isNotEmpty(subIds)) {
                for (Long id : subIds) {
                    TeacherDetail teacherDetailTmp = teacherLoader.loadTeacherDetail(id);
                    if (teacherDetailTmp.getSubject() == null) {
                        continue;
                    }
                    subjects.add(teacherDetailTmp.getSubject().name());
                }
            }
            return subjects;
        }));

        context.set("main_teacher_subject", td.getSubject() == null ? "" : td.getSubject().name());

        context.set("is_authentication", td.getAuthenticationState());

        context.set("createTime", td.getCreateTime().getTime());

        context.set("timestamp", System.currentTimeMillis());
        return context;
    }

    public void deleteTaskAndProgress(TeacherTask teacherTask) {
        List<TeacherTaskProgress> teacherTaskProgressList = progressDao.loadTeacherProgress(teacherTask.getTeacherId());
        TeacherTaskProgress progress = teacherTaskProgressList.stream().filter(p -> Objects.equals(p.getTaskId(), teacherTask.getId())).findAny().orElse(null);
        if (progress != null) {
            progressDao.remove(progress.getId());
        }
        teacherTask.setDisabled(true);
        taskDao.replace(teacherTask);
    }

    public MapMessage newTaskAndProgress(TeacherDetail td, TeacherTaskTpl tpl){
        try {
            MapMessage mapMessage = this.createTaskAndProgress(td, tpl, null, null);
            Boolean isNew = (Boolean) mapMessage.get("isNew");
            if (mapMessage.isSuccess() && isNew != null && isNew) {
                TeacherTask task = (TeacherTask) mapMessage.get("newTask");
                TeacherTaskProgress progress = (TeacherTaskProgress) mapMessage.get("progress");

                /**
                 * 发现有极少量的老师任务会重复，大概发现了10几个老师部分任务重复，未找到具体原因
                 * 为了解决这个问题，上了以下这个逻辑，上线后，未发现再有重复的问题
                 */
                TeacherTask teacherTaskHas = taskDao.getTeacherTaskByTeacherIdAndTplId(td.getId(), tpl.getId());
                if (teacherTaskHas == null) {
                    taskDao.insert(task);
                    progress.setTaskId(task.getId());
                    progressDao.insert(progress);
                } else {
                    mapMessage.put("newTask", teacherTaskHas);
                }
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error("TT:Init task error!tId:{},tplId:{}",td.getId(),tpl.getId(),e);
            return errorMessage(e.getMessage());
        }
    }

    /**
     * 判断用户是否具备领取某个任务的条件
     * @param td
     * @param tpl
     * @return
     */
    @Override
    public boolean isPutOn(TeacherDetail td, TeacherTaskTpl tpl){
        JexlEngine jexlEngine = new JexlEngine();
        JexlContext calContext = initNewTaskContext(td, tpl);//组装表达式所需要的上下文变量

        //初始化各个任务所需要的一些个性化的参数
        TeacherTaskInit taskInit = teacherTaskHandlerInit.getHandler(tpl.getTpl());
        taskInit.initAddJexlContext(td, calContext);

        /** 是否满足任务投放条件 **/
        boolean putOn = Optional.ofNullable(tpl.getPutOnExpr())
                .filter(StringUtils::isNotBlank)
                .map(expr -> jexlEngine.createExpression(tpl.getPutOnExpr()).evaluate(calContext))
                .map(SafeConverter::toBoolean)
                .orElse(false);
        return putOn;
    }

    /**
     * 初始化任务与progress
     * @param td
     * @param tpl
     * @return
     */
    public MapMessage createTaskAndProgress(TeacherDetail td, TeacherTaskTpl tpl, TeacherTask oldTask, TeacherTaskProgress oldProgress) {
        try {
            JexlEngine jexlEngine = new JexlEngine();
            JexlContext calContext = initNewTaskContext(td, tpl);//组装表达式所需要的上下文变量

            //初始化各个任务所需要的一些个性化的参数
            TeacherTaskInit taskInit = teacherTaskHandlerInit.getHandler(tpl.getTpl());
            taskInit.initAddJexlContext(td, calContext);

            /** 是否满足任务投放条件 **/
            boolean putOn = Optional.ofNullable(tpl.getPutOnExpr())
                    .filter(StringUtils::isNotBlank)
                    .map(expr -> jexlEngine.createExpression(tpl.getPutOnExpr()).evaluate(calContext))
                    .map(SafeConverter::toBoolean)
                    .orElse(false);

            if (!putOn) {
                return MapMessage.successMessage().add("isPutOn", false);
            }

            if (!taskInit.isPutOn(td, calContext)) {//主要针对一些非常特殊，不太好实现的场景使用，需要有其他任务依赖的判断
                return MapMessage.successMessage().add("isPutOn", false);
            }

            //初始化用户任务表、初始化任务的执行状态
            final TeacherTask task = new TeacherTask();
            TeacherTaskProgress progress = new TeacherTaskProgress();
            if (oldTask != null) {//如果老任务不为空，则沿用老ID
                task.setId(oldTask.getId());
            }
            task.setName(tpl.getName());
            task.setType(tpl.getType());
            task.setTeacherId(td.getId());
            task.setTplId(tpl.getId());

            if (isTrue(tpl.getAutoReceive())) {//如果任务是自动领取，则直接将任务设置为进行中
                task.setStatus(TeacherTask.Status.ONGOING.name());
                //设置领取时间
                task.setReceiveDate(new Date());
                progress.setReceiveDate(DateUtils.dateToString(task.getReceiveDate(), DateUtils.FORMAT_SQL_DATETIME));
                progress.setReceiveTime(task.getReceiveDate().getTime());

                // 进行中的任务，如果有有效期，则置上失效日期
                /**
                 * 这里分两种情况
                 * 1. 有确切的有效期, 比如有效期 5 天, 1 号领取, 过期时间就是 6 号, 2 号领取, 过期时间就是 7 号
                 * 2. 有确切的结束时间, 比如不管什么时候领取, 只要到 2019-01-01 23:59:59 就过期
                 *
                 * 优先判断第二种, 如果没有实现 calcExpireDate 方法, 默认返回空走第一种策略, 第一种也没有的话就不设置
                 */
                Date calcExpireDate = taskInit.calcExpireDate(td, task, progress);
                if (calcExpireDate != null) {
                    task.setExpireDate(calcExpireDate);
                    progress.setExpireTime(calcExpireDate.getTime());
                } else {
                    Optional.ofNullable(tpl.getActiveTime())
                            .map(at -> new Date((DateUtils.nextDay(DayRange.current().getStartDate(), tpl.getActiveTime()).getTime() - 1000)))
                            .ifPresent(t -> {
                                task.setExpireDate(t);
                                progress.setExpireTime(t.getTime());
                            });
                }
            } else {//如果任务不是自动领取，即只初始化任务，需要用户点击领取任务才能转变为ongoing
                task.setStatus(TeacherTask.Status.INIT.name());
            }

            if (oldProgress != null) {//如果老进度不为空，则将ID于task_id设置进去
                progress.setId(oldProgress.getId());
                progress.setTaskId(oldProgress.getTaskId());
            }
            progress.setTeacherId(td.getId());
            progress.setTplId(tpl.getId());
            progress.setStatus(task.getStatus());
            tpl.getSubTasksForEach(t -> {
                TeacherTaskEvaluator teacherTaskEvaluator = teacherTaskHandlerEvaluator.getHandler(TeacherTaskTpl.TplEvaluatorEvent.valueOf(t.getCalType()));
                TeacherTaskProgress.SubTaskProgress stp = new TeacherTaskProgress.SubTaskProgress();
                stp.setId(t.getId());
                stp.setStatus(task.getStatus());

                /**上下文 **/
                stp.setVars(teacherTaskEvaluator.initVars(tpl,t));
                addMapToJexlContext(calContext, stp.getVars());

                /** 初始化进度数据 **/
                TeacherTaskProgress.Progress subProgress = new TeacherTaskProgress.Progress();
                stp.setProgress(subProgress);//初始化任务进度
                if (isTrue(t.getShowProgress())){
                    Integer target = Optional.ofNullable(t.getProgress().getTargetExpr())
                            .map(e -> jexlEngine.createExpression(e).evaluate(calContext))
                            .map(SafeConverter::toInt)
                            .orElse(0);
                    subProgress.setCurr(0);
                    subProgress.setQ(t.getProgress().getQuantifier());
                    subProgress.setTarget(target);

                    /** 进度的奖励初始化 **/
                    List<TeacherTaskProgress.ProgressReward> progressRewards = new ArrayList<>();
                    subProgress.setRewards(progressRewards);
                    t.getProgress().getRewards().forEach(r -> {
                        int rewardVal = Optional.ofNullable(r.getExpression())
                                .map(expr -> jexlEngine.createExpression(r.getExpression()).evaluate(calContext))
                                .map(SafeConverter::toInt)
                                .orElse(0);
                        boolean open = rewardVal != 0;//注意：open = 0，则会根据模板中的方法奖励表达式重新计算
                        TeacherTaskProgress.ProgressReward progressReward = new TeacherTaskProgress.ProgressReward();
                        progressReward.setId(r.getId());
                        progressReward.setOpen(open);
                        progressReward.setValue(rewardVal);
                        progressReward.setUnit(r.getUnit());
                        progressReward.setReceivedCurr(0);
                        progressReward.setReceivedValue(0);
                        progressRewards.add(progressReward);
                    });
                }

                /** 子任务的奖励初始化 **/
                List<TeacherTaskProgress.Reward> rewards = new ArrayList<>();
                stp.setRewards(rewards);
                t.getRewards().forEach(r -> {
                    int rewardVal = Optional.ofNullable(r.getExpression())
                            .map(expr -> jexlEngine.createExpression(r.getExpression()).evaluate(calContext))
                            .map(SafeConverter::toInt)
                            .orElse(0);
                    boolean open = rewardVal != 0;//注意：open = 0，则会根据模板中的方法奖励表达式重新计算
                    // 增加新的奖励数据
                    TeacherTaskProgress.Reward reward = new TeacherTaskProgress.Reward();
                    reward.setId(r.getId());
                    reward.setReceived(false);
                    reward.setValue(rewardVal);
                    reward.setOpen(open);
                    reward.setUnit(r.getUnit());
                    rewards.add(reward);
                });
                progress.addSubTaskProgress(stp);
            });

            /** 总任务的奖励初始化 **/
            List<TeacherTaskProgress.Reward> rewards = new ArrayList<>();
            progress.setRewards(rewards);
            tpl.getRewards().forEach(r -> {
                int rewardVal = Optional.ofNullable(r.getExpression())
                        .map(expr -> jexlEngine.createExpression(r.getExpression()).evaluate(calContext))
                        .map(SafeConverter::toInt)
                        .orElse(0);
                boolean open = rewardVal != 0;//注意：open = 0，则会根据模板中的方法奖励表达式重新计算
                TeacherTaskProgress.Reward reward = new TeacherTaskProgress.Reward();
                reward.setId(r.getId());
                reward.setReceived(false);
                reward.setValue(rewardVal);
                reward.setOpen(open);
                reward.setUnit(r.getUnit());
                rewards.add(reward);
            });

            if (isTrue(tpl.getLoop())) {//任务是否是循环任务
                switch (tpl.getCycleUnit()){
                    case "D":
                        progress.setExpireTime(DayRange.current().getEndDate().getTime());
                        break;
                    case "W":
                        progress.setExpireTime(WeekRange.current().getEndDate().getTime());
                        break;
                    case "M":
                        progress.setExpireTime(MonthRange.current().getEndDate().getTime());
                        break;
                    default:
                        if (tpl.getActiveTime() > 0) {//如果设置了过期天数，则用这个
                            progress.setExpireTime(DateUtils.nextDay(DayRange.current().getStartDate(), tpl.getActiveTime()).getTime() - 1000);//减去一秒，为了让23:59:59过期
                        }
                        break;
                }
                if (progress.getExpireTime() != null && progress.getExpireTime() > 0) {//设置任务的过期时间
                    task.setExpireDate(new Date(progress.getExpireTime()));
                }
            }

            taskInit.initTaskAndProgress(td, task, progress, oldTask, oldProgress);//初始化结束后，可以根据任务的一些个性化，在设置一些其他信息
            return successMessage().add("newTask", task).add("progress", progress).add("isNew", true);
        } catch (Exception e) {
            logger.error("TT:Init task error!tId:{},tplId:{}",td.getId(),tpl.getId(),e);
            return errorMessage(e.getMessage());
        }
    }

    /**
     * 将map中的变量添加到JexlContext的上下文中
     * @param context
     * @param map
     */
    public void addMapToJexlContext(JexlContext context, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        map.entrySet().stream().forEach(e -> context.set(e.getKey(), e.getValue()));
    }

    private class LazyContext implements JexlContext{

        private Map<String,LazyInitializationSupplier<Object>> map;

        public LazyContext(){
            this.map = new HashMap<>();
        }

        @Override
        public Object get(String name) {
            return map.getOrDefault(name, new LazyInitializationSupplier<>(() -> null)).initializeIfNecessary();
        }

        public void set(String name,LazyInitializationSupplier<Object> supplier){
            map.put(name,supplier);
        }

        @Override
        public void set(String name, Object value) {
            map.put(name, new LazyInitializationSupplier<>(() -> value));
        }

        @Override
        public boolean has(String name) {
            return map.containsKey(name);
        }
    }

}
