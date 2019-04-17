package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.agent.listener.handler.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 2016/7/20.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.agent.command.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.agent.command.queue"
                )
        }
)
public class AgentCommandQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject PerformanceRankingHandler performanceRankingHandler;
    @Inject NeedFollowUpHandler needFollowUpHandler;
    @Inject WeeklyHandler weeklyHandler;
    @Inject AlterationRemindHandler alterationRemindHandler;
    @Inject MonthlyHandler monthlyHandler;
    @Inject AgentDictSchoolHandler dictSchoolHandler;
    @Inject AliRefundHandler aliRefundHandler;
    @Inject CrmReviewSchoolClueHandler crmReviewSchoolClueHandler;
    @Inject CrmReviewTeacherFakeHandler crmReviewTeacherFakeHandler;
    @Inject UnifiedExamApplyHandler unifiedExamApplyHandler;
    @Inject UpdateTeacherMemorandumHandler teacherMemorandumHandler;
    @Inject NewTeacherMessageHandler newTeacherMessageHandler;

    @Inject PendingMessageHandler pendingMessageHandler;
    @Inject AgentModifyDictSchoolApplyMessageHandler agentModifyDictSchoolApplyMessageHandler;
    @Inject UpdateSchoolExtInfoHandler updateSchoolExtInfoHandler;
    @Inject ProductFeedbackHandler productFeedbackHandler;
    @Inject IntoSchoolEarlyWarningHandler intoSchoolEarlyWarningHandler;
    @Inject BuildPerformanceCacheHandler buildPerformanceCacheHandler;
    @Inject HideAndShowTeacherHandler hideAndShowTeacherHandler;
    @Inject AgentWorkRecordStatisticsHandler agentWorkRecordStatisticsHandler;
    @Inject AgentDailyScoreStatisticsHandler agentDailyScoreStatisticsHandler;
    @Inject AgentRegisterTeacherStatisticsHandler agentRegisterTeacherStatisticsHandler;
    @Inject AgentEmailUnScoreDictSchoolHandler agentEmailUnScoreDictSchoolHandler;
    @Inject ActivityDataStatisticsHandler activityDataStatisticsHandler;

    @Override
    public void onMessage(Message message) {
        String text = message.getBodyAsString();

        Map<String, Object> command = JsonUtils.fromJson(text);

        if (command == null || !command.containsKey("command")) {
            return;
        }

        String commandName = String.valueOf(command.get("command"));
        // 首页排行榜
        if ("performance_ranking".equals(commandName)) {
            int date = SafeConverter.toInt(command.get("date"));
            performanceRankingHandler.runningRanking(date);
        }

        // 首页线索,需要跟进的学校及老师
        if ("need_follow_up".equals(commandName)) {
            Integer date = SafeConverter.toInt(command.get("date"));
            AlpsThreadPool.getInstance().submit(() -> needFollowUpHandler.executeCommand(date));
        }

        // 生成周报
        if ("generate_weekly".equals(commandName)) {
            Integer date = SafeConverter.toInt(command.get("date"));
            AlpsThreadPool.getInstance().submit(() -> weeklyHandler.executeCommand(date));
        }

        // 老师换班提醒
        if ("alteration_remind".equals(commandName)) {
            Long time = SafeConverter.toLong(command.get("date"));
            AlpsThreadPool.getInstance().submit(() -> alterationRemindHandler.executeCommand(time));
        }

        // 生成周报
        if ("generate_monthly".equals(commandName)) {
            Integer date = SafeConverter.toInt(command.get("date"));
            AlpsThreadPool.getInstance().submit(() -> monthlyHandler.executeCommand(date));
        }

        if ("school_dict_config_verify".equalsIgnoreCase(commandName)) {
            AlpsThreadPool.getInstance().submit(() -> dictSchoolHandler.runningRanking());
        }

        // 支付宝退款修改任务状态
        if ("alipay_refund_callback".equalsIgnoreCase(commandName)) {
            List<String> successIds = (List<String>) command.get("successIds");
            List<String> failIds = (List<String>) command.get("failIds");
            AlpsThreadPool.getInstance().submit(() -> aliRefundHandler.executeCommand(successIds, failIds));
        }

        // CRM进行学校鉴定
        if ("crm_review_school_clue".equals(commandName)) {
            Long schoolId = SafeConverter.toLong(command.get("schoolId"));
            String schoolName = (String) command.get("schoolName");
            Integer reviewStatus = SafeConverter.toInt(command.get("reviewStatus"));
            String reviewerName = (String) command.get("reviewerName");
            String reviewNote = (String) command.get("reviewNote");
            Long receiverId = SafeConverter.toLong(command.get("receiverId"));
            crmReviewSchoolClueHandler.handle(schoolId, schoolName, reviewStatus, reviewerName, reviewNote, receiverId);
        }

        // CRM老师判假
        if ("crm_review_teacher_fake".equals(commandName)) {
            Long teacherId = SafeConverter.toLong(command.get("teacherId"));
            String teacherName = (String) command.get("teacherName");
            String reviewStatus = (String) command.get("reviewStatus");
            String reviewerName = (String) command.get("reviewerName");
            String reviewNote = (String) command.get("reviewNote");
            Long receiverId = SafeConverter.toLong(command.get("receiverId"));
            crmReviewTeacherFakeHandler.handle(teacherId, teacherName, reviewStatus, reviewerName, reviewNote, receiverId);
        }

        // 统考申请上线
        if ("unified_exam_apply_online".equals(commandName)) {
            String unifiedExamName = (String) command.get("unifiedExamName");
            Long receiverId = SafeConverter.toLong(command.get("receiverId"));
            unifiedExamApplyHandler.sendOnlineMessage(unifiedExamName, receiverId);
        }

        if ("update_memorandum".equals(commandName)) {
            teacherMemorandumHandler.handle();
        }

        // 新注册老师消息推送
        if ("new_teacher_message".equals(commandName)) {
            Integer dateInt = SafeConverter.toInt(command.get("date"));
            newTeacherMessageHandler.handle(dateInt);
        }

        //市场人员待我审核的内容数量消息推送
        if ("agent_user_pending_count_message".equals(commandName)) {
            pendingMessageHandler.handle();
        }

        //字段表申请审核消息处理
        if ("crm_modify_dict_school_apply".equals(commandName)) {
            agentModifyDictSchoolApplyMessageHandler.handle(command);
        }

        if ("update_school_ext_info".equals(commandName)) {
            Long schoolId = SafeConverter.toLong(command.get("schoolId"));
            updateSchoolExtInfoHandler.handle(schoolId);
        }

        if ("notice_product_feedback".equals(commandName)) {
            Long workflowId = SafeConverter.toLong(command.get("recordId"));
            productFeedbackHandler.handle(workflowId);
        }

        //通知大区经理进校未达标的专员的信息。
        if ("notice_region_early_warning".equals(commandName)) {
            intoSchoolEarlyWarningHandler.handle();
        }

        // 构建业绩缓存
        if ("build_performance_cache".equals(commandName)) {
            buildPerformanceCacheHandler.handle();
        }

        // 隐藏和显示老师
        if ("hide_and_show_teacher".equals(commandName)) {
            List<Long> teacherIds = new ArrayList<>();
            Object teacherIdsObj = command.get("teacherIds");
            if (teacherIdsObj != null) {
                List<Integer> teacherIdsInt = (List<Integer>) teacherIdsObj;
                teacherIdsInt.forEach(item -> {
                    teacherIds.add(SafeConverter.toLong(item));
                });
            }
            hideAndShowTeacherHandler.handle(teacherIds);
        }

        // 天玑工作量统计
        if ("agent_work_record_statistics".equals(commandName)) {
            Long startDate = SafeConverter.toLong(command.get("startDate"));
            Long endDate = SafeConverter.toLong(command.get("endDate"));
            Integer type = SafeConverter.toInt(command.get("type"), 1);
            agentWorkRecordStatisticsHandler.handle(startDate, endDate, type);
        }

        // 日报得分统计
        if ("agent_daily_score_statistics".equals(commandName)) {
            Long startDate = SafeConverter.toLong(command.get("startDate"));
            Long endDate = SafeConverter.toLong(command.get("endDate"));
            agentDailyScoreStatisticsHandler.handle(startDate, endDate);
        }

        // 天玑新注册老师跑数据
        if ("agent_register_teacher_statistics".equals(commandName)) {
            String dateStr = SafeConverter.toString(command.get("dateStr"));
            Integer dayNum = SafeConverter.toInt(command.get("dayNum"),1);
            agentRegisterTeacherStatisticsHandler.handle(dateStr,dayNum);
        }

        //邮件发送为评分的字典学校
        if ("agent_email_unscore_dict_school".equals(commandName)) {
            Long beginDate = SafeConverter.toLong(command.get("beginDate"));
            Long endDate = SafeConverter.toLong(command.get("endDate"));
            agentEmailUnScoreDictSchoolHandler.handle(beginDate, endDate);
        }

        //邮件发送为评分的字典学校
        if ("agent_cal_activity_statistics".equals(commandName)) {
            String activityId = SafeConverter.toString(command.get("activityId"));
            Integer day = SafeConverter.toInt(command.get("day"));
            activityDataStatisticsHandler.handle(activityId, day);
        }
    }

}
