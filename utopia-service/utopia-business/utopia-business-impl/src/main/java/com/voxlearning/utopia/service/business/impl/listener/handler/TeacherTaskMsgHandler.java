package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.entity.task.TeacherMonthTask;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskProgressDao;
import com.voxlearning.utopia.service.business.impl.loader.TeacherTaskLoaderImpl;
import com.voxlearning.utopia.service.business.impl.queue.BusinessQueueProducer;
import com.voxlearning.utopia.service.business.impl.service.TeacherMonthTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.TeacherRookieTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherActivateTeacherServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityCardService;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.invitation.api.TeacherActivateService;
import com.voxlearning.utopia.service.invitation.entity.TeacherActivate;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.entity.task.TeacherRookieTask.CHECK_STUDENT_SIZE;
import static com.voxlearning.utopia.entity.task.TeacherRookieTask.HOME_WORK_ID;

/**
 *
 * 老师任务成长体系中，用来负责监听并消费所有与任务相关的事件
 *
 * 这个类是所有老师任务成长体系中的入口，逻辑全部添加在这里
 *
 * 需要消费的topic，具体入口通过搜索这些topic既可以找到其对应的Listener
 *
 * utopia.homework.teacher.topic
 * utopia.business.activity.scholarship.topic
 * utopia.homework.teacher.retry.topic
 * utopia.homework.teacher.junior.topic
 * utopia.group.teacher.ref.topic
 * utopia.teacher.awake.topic
 * utopia.teacher.task.retry.topic
 * utopia.user.basic.topic
 * utopia.teacher.task.signin.topic
 * utopia.teacher.share.article.queue
 *
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskMsgHandler implements InitializingBean {

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherActivateTeacherServiceImpl teacherActivateTeacherServiceImpl;
    @Inject private BusinessQueueProducer businessQueueProducer;
    @Inject private TeacherTaskServiceImpl teacherTaskService;
    @Inject private TeacherTaskLoaderImpl teacherTaskLoader;
    @Inject private TeacherTaskProgressDao teacherTaskProgressDao;
    @Inject private GroupLoaderClient groupLoaderClient;

    @ImportService(interfaceClass = TeacherActivityCardService.class)
    private TeacherActivityCardService teacherActivityCardService;
    @ImportService(interfaceClass = TeacherActivateService.class)
    private TeacherActivateService teacherActivateService;

    @Inject
    private TeacherRookieTaskServiceImpl teacherRookieTaskService;
    @Inject
    private TeacherMonthTaskServiceImpl teacherMonthTaskService;

    @AlpsPubsubPublisher(topic = "utopia.teacher.task.retry.topic")
    private MessagePublisher messagePublisherRetry;
    @AlpsPubsubPublisher(topic = "utopia.teacher.awake.topic")
    private MessagePublisher messagePublisherAwake;
    @Inject
    private BusinessCacheSystem businessCacheSystem;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void handler(Message message) {
        Map<String, Object> msgMap;
        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            log.warn("TeacherTaskMsgHandler message decode failed!", JsonUtils.toJson(message.decodeBody()));
            return;
        }
        this.handlerMessage(msgMap);
    }

    /**
     * 处理与老师任务成长体系相关的事件
     * @param msg
     */
    private void handlerMessage(Map<String, Object> msg) {
        Long teacherId = getTeacherId(msg);
        String messageType = getMessageType(msg);
        if (null == teacherId || null == messageType) {//如果没有合法的messgeType与老师ID，则退出不处理
            printLog(msg, teacherId, messageType, null);
            return;
        }

        // 考虑包班制，选取主老师，所有的任务都是挂在了主老师身上
        Long mainTchId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if(mainTchId != null && mainTchId != 0L) {
            teacherId = mainTchId;
        }
        final Long finalTeacherId = teacherId;

        printLog(msg, teacherId, messageType, finalTeacherId);

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(finalTeacherId);
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return;
        }
        try {
            AtomicCallbackBuilderFactory.getInstance()
                    .<Boolean>newBuilder()
                    .keyPrefix("TT:updateProgress")
                    .keys(finalTeacherId)
                    .callback(() -> {
                        if (Objects.equals(messageType, "checked") || Objects.equals(messageType, getRetryMessageType("checked"))) {
                            //处理检查作业事件
                            Integer finishNum = MapUtils.getInteger(msg,"finishCount");
                            Long createAt = MapUtils.getLong(msg, "createAt");
                            Long checkedAt = MapUtils.getLong(msg, "checkedAt");
                            String subject = MapUtils.getString(msg, "subject");
                            String objectiveConfigTypes = MapUtils.getString(msg, "objectiveConfigTypes");
                            String homeworkId = MapUtils.getString(msg, "homeworkId");
                            Long groupId = MapUtils.getLong(msg, "groupId");

                            processHomeworkAction(teacherDetail, finalTeacherId, groupId, homeworkId, finishNum, createAt, checkedAt, subject, objectiveConfigTypes);
                            processRookieTask(finalTeacherId, TeacherRookieTask.RookieTaskTrigger.CHECK_HOMEWORK, MapUtils.map(HOME_WORK_ID, homeworkId, CHECK_STUDENT_SIZE, finishNum));
                            processWeekTask(teacherDetail, groupId, homeworkId, finishNum);
                            //processMonthTask(finalTeacherId, groupId, homeworkId, finishNum);
                        } else if (Objects.equals(messageType, "assign") || Objects.equals(messageType, getRetryMessageType("assign"))) {
                            //处理布置作业事件
                            Long createAt = MapUtils.getLong(msg, "createAt");
                            String subject = MapUtils.getString(msg, "subject");
                            String objectiveConfigTypes = MapUtils.getString(msg, "objectiveConfigTypes");
                            String homeworkType = MapUtils.getString(msg, "homeworkType").trim();
                            processAssignHomeworkAction(teacherDetail, finalTeacherId, MapUtils.getLong(msg, "groupId"), MapUtils.getString(msg, "homeworkId"), createAt, subject, objectiveConfigTypes, homeworkType);

                            processRookieTask(finalTeacherId, TeacherRookieTask.RookieTaskTrigger.ASSIGN_HOMEWORK);
                        } else if (Objects.equals(messageType, "comment") || Objects.equals(messageType, getRetryMessageType("comment"))) {
                            //处理作业评语事件
                            processCommentMsg(finalTeacherId, MapUtils.getLong(msg, "groupId"), MapUtils.getString(msg, "homeworkId"), msg);
                        } else if (Objects.equals(messageType, "shareHomeworkReport") || Objects.equals(messageType, getRetryMessageType("shareHomeworkReport"))) {
                            //分享作业报告事件
                            Long createAt = MapUtils.getLong(msg, "createAt");
                            processReportMsg(finalTeacherId, MapUtils.getLong(msg, "groupId"), MapUtils.getString(msg, "homeworkId"), createAt);
                            processRookieTask(finalTeacherId, TeacherRookieTask.RookieTaskTrigger.SHARE_HOMEWORK);
                        } else if (Objects.equals(messageType, "awake") || Objects.equals(messageType, getRetryMessageType("awake"))) {
                            //处理唤醒事件
                            processAwake(finalTeacherId, MapUtils.getLong(msg, "inviteeId"));
                        } else if (Objects.equals(messageType, EventUserType.TEACHER_CLAZZ_INSERT.getEventType()) || Objects.equals(messageType, getRetryMessageType(EventUserType.TEACHER_CLAZZ_INSERT.getEventType()))) {
                            //老师建立班级事件
                            processCreateClazz(finalTeacherId);
                        } else if (Objects.equals(messageType, "signin") || Objects.equals(messageType, getRetryMessageType("signin"))) {
                            //老师签到事件
                            Long signInAt = MapUtils.getLong(msg, "signInAt");
                            processSignIn(finalTeacherId, signInAt);
                        } else if (Objects.equals(messageType, "teacherCommentAndAward") || Objects.equals(messageType, getRetryMessageType("teacherCommentAndAward"))) {
                            //老师评论并领取奖励
                            processTeacherCommentAndAward(finalTeacherId, MapUtils.getString(msg, "homeworkId"), MapUtils.getString(msg, "type"),msg);
                        } else if (Objects.equals(messageType, "teacherShareArticle") || Objects.equals(messageType, getRetryMessageType("teacherShareArticle"))) {
                            //分享文章
                            processTeacherShareArticle(finalTeacherId);
                        } else if (Objects.equals(messageType, "user_basic_info_updated") || Objects.equals(messageType, getRetryMessageType("user_basic_info_updated"))) {
                            //个人信息发生变化
                            processTeacherUserInfoFull(finalTeacherId, teacherDetail);
                        } else if (Objects.equals(messageType, EventUserType.TEACHER_CLAZZ_UPDATED.getEventType()) || Objects.equals(messageType, getRetryMessageType(EventUserType.TEACHER_CLAZZ_UPDATED.getEventType()))) {
                            //老师的班组关系发生了变化
                            processChangedClazz(finalTeacherId);
                        }
                        return true;
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e1) {
            }
            msg.put("messageType",getRetryMessageType(messageType));
            messagePublisherRetry.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msg)));
        }
    }

    private void printLog(Map<String, Object> msg, Long teacherId, String messageType, Long finalTeacherId) {
        try {
            //打印一下日志
            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "teacherId", teacherId, "finalTeacherId", finalTeacherId, "op", "teacher_task", "messageType", messageType);
            logMap.put("messageInfo", JsonUtils.toJson(msg));
            LogCollector.info("backend-general", logMap);

            if (RuntimeMode.le(Mode.STAGING)) {
                log.info("junbao-log:" + JSON.toJSONString(msg));
            }
        } catch (Throwable t){
        }
    }

    /**
     * 获取重试的type的message
     * @param type
     * @return
     * @author zhouwei
     */
    private String getRetryMessageType(String type) {
        if (!type.contains("retry")) {//如果不是retry，则将首字母大写，拼写成：retry + messageType
            return "retry" + StringUtils.capitalize(type);
        }
        return type;
    }

    /**
     * 监听的topic比较多，各个message定义不一样，获取messageType
     * @param msg
     * @return
     */
    private String getMessageType(Map<String, Object> msg) {
        if (null != MapUtils.getString(msg, "messageType")) {
            return MapUtils.getString(msg, "messageType");
        }
        if (null != MapUtils.getString(msg, "event_type")) {
            return MapUtils.getString(msg, "event_type");
        }
        return null;
    }

    /**
     * 监听的topic比较多，各个message定义不一样，获取老师ID
     * @param msg
     * @return
     */
    private Long getTeacherId(Map<String, Object> msg) {
        if (null != MapUtils.getLong(msg, "teacherId")) {
            return MapUtils.getLong(msg, "teacherId");
        }
        if (null != MapUtils.getLong(msg, "event_id")) {
            return MapUtils.getLong(msg, "event_id");
        }
        return null;
    }

    /**
     *
     * 处理老师的完善个人信息事件
     *
     * @author zhouwei
     */
    private void processTeacherUserInfoFull(Long teacherId, TeacherDetail teacherDetail) {
        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherId);
        boolean isUserInfoFull = true;
        Integer year = null;
        Integer month = null;
        Integer day = null;
        String gender = null;
        String duty = null;
        Integer teacherYears = null;
        if (teacherDetail.getProfile() != null) {
            year = teacherDetail.getProfile().getYear();//出生年
            month = teacherDetail.getProfile().getMonth();//出生月
            day = teacherDetail.getProfile().getDay();//出生日
            gender = teacherDetail.getProfile().getGender();//性别
            if (null == year || 0 == year) {
                isUserInfoFull = false;
            }
            if (null == month || 0 == month) {
                isUserInfoFull = false;
            }
            if (null == day || 0 == day) {
                isUserInfoFull = false;
            }
            if (StringUtils.isEmpty(gender) || Objects.equals(gender, Gender.NOT_SURE.getCode())) {
                isUserInfoFull = false;
            }
        }
        if (teacherExtAttribute != null) {
            duty = teacherExtAttribute.getDuty();//职务
            teacherYears = teacherExtAttribute.getTeachingYears();//教龄
            if (StringUtils.isEmpty(duty)) {
                isUserInfoFull = false;
            }
            if (null == teacherYears || 0 == teacherYears) {
                isUserInfoFull = false;
            }
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("year", year);
        userInfo.put("month", month);
        userInfo.put("day", day);
        userInfo.put("gender", gender);
        userInfo.put("duty", duty);
        userInfo.put("teacherYears", teacherYears);
        Map<String, Object> var = MapUtils.m("isUserInfoFull", isUserInfoFull, "userInfo", userInfo);
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.USER_INFO_CHANGED, var);
    }

    /**
     * 老师分享文章的处理
     * @param teacherId
     * @author zhouwei
     */
    private void processTeacherShareArticle(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        Map<String, Object> var = MapUtils.m("date", new Date().getTime());
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.SHARE_ARTICLE, var);
    }

    /**
     * 处理用户签到的事件
     * @param teacherId
     * @author zhouwei
     */
    private void processSignIn(Long teacherId, Long signInAt) {
        if (teacherId == null) {
            return;
        }
        if (null == signInAt) {
            signInAt = new Date().getTime();
        }
        // 发现有重复签到的数据,在这里拦一下
        Long incr = businessCacheSystem.CBS.storage.incr("TEACHER_TASK:T_SIGN_" + teacherId, 1L, 1L, DateUtils.getCurrentToDayEndSecond());
        if (Objects.equals(incr, 1L)) {
            Map<String, Object> var = MapUtils.m("date", signInAt);
            teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.USER_SIGN_IN, var);
        }
    }

    /**
     * 处理创建班级的事件
     * @author zhouwei
     */
    private void processCreateClazz(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        Map<String, Object> var = MapUtils.m("updateGroup", "1");
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.CREATE_CLAZZ, var);

        //建立班级后，需要产生一条班级发生变更的事件，用来修改相关任务的一些状态
        Map<String, Object> msgMapNew = new HashMap<>();
        msgMapNew.put("messageType", "teacher_clazz_updated");
        msgMapNew.put("teacherId", teacherId);
        messagePublisherRetry.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgMapNew)));
    }

    /**
     * 处理老师班级变化事件
     * @author zhouwei
     */
    private void processChangedClazz(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        Map<String, Object> var = new HashMap<>();
        var.put("event_type", EventUserType.TEACHER_CLAZZ_UPDATED.getEventType());
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.UPDATE_TASK_INFO, var);
    }


    /**
     * 处理布置作业的事件
     * @param teacherId
     * @param groupId
     * @param homeworkId
     */
    private void processAssignHomeworkAction(TeacherDetail teacherDetail, Long teacherId, Long groupId,
                                             String homeworkId, Long createAt, String subject,
                                             String objectiveConfigTypes, String homeworkType) {
        if (teacherId == null || groupId == null || homeworkId == null || subject == null) {
            return;
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("hwId", homeworkId);
        varMap.put("assignNum", 1);
        varMap.put("groupId", groupId);
        varMap.put("assignTime", new Date().getTime());
        varMap.put("subject", subject);
        varMap.put("objectiveConfigTypes", objectiveConfigTypes);
        varMap.put("messageType", "assign");
        varMap.put("createAt", createAt);
        varMap.put("homeworkType", homeworkType);
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.ASSIGN_HOMEWORK, varMap);
    }

    /**
     * 分享学情的事件
     * @param teacherId
     * @param groupId
     * @param hwId
     */
    private void processReportMsg(Long teacherId, Long groupId, String hwId, Long createAt){
        if (teacherId == null || groupId == null || hwId == null || createAt == null) {
            return;
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("hwId", hwId);
        varMap.put("groupId", groupId);
        varMap.put("messageType", "reportHomework");
        varMap.put("createAt", createAt);
        varMap.put("reportHomeworkTime", new Date().getTime());

        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.REPORT_HOMEWORK, varMap);
    }

    /**
     * 处理批阅作业的事件
     * @param teacherId
     * @param groupId
     * @param hwId
     */
    private void processCommentMsg(Long teacherId, Long groupId, String hwId, Map<String, Object> msg) {
        if (teacherId == null || groupId == null || hwId == null) {
            return;
        }

        /**
         * 评论或者奖励事件合并
         */
        Map<String, Object> msgMapNew = new HashMap<>();
        msgMapNew.put("messageType", "teacherCommentAndAward");
        msgMapNew.put("teacherId", teacherId);
        msgMapNew.put("homeworkId", hwId);
        msgMapNew.put("groupId", groupId);
        msgMapNew.put("type","comment");

        String studentListJson = MapUtils.getString(msg, "studentIds");
        List<Long> studentIds = JsonUtils.fromJsonToList(studentListJson, Long.class);
        msgMapNew.put("studentIds", studentIds);
        messagePublisherRetry.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgMapNew)));
    }

    /**
     * 评论或者奖励事件
     * @param teacherId
     * @param hwId
     * @param type
     */
    private void processTeacherCommentAndAward(Long teacherId, String hwId, String type, Map<String, Object> msg) {
        if (teacherId == null || hwId == null || type == null) {
            return;
        }

        List<Long> studentList = new ArrayList<>();
        if (Objects.equals(type, "comment")) {
            String studentListJson = MapUtils.getString(msg, "studentIds");
            studentList = JsonUtils.fromJsonToList(studentListJson, Long.class);
        } else if (Objects.equals(type, "award")) {
            Long studentId = MapUtils.getLong(msg, "studentId");
            studentList.add(studentId);
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("hwId", hwId);
        varMap.put("commentAndAward", 1);
        varMap.put("type", type);
        varMap.put("studentIds", studentList);
        varMap.put("commentAndAwardTime", new Date().getTime());
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.COMMENT_AND_AWARD_HOMEWORK, varMap);

        Map<String, Object> ext = new HashMap<>();
        ext.put("studentIds", studentList);
        processRookieTask(teacherId, TeacherRookieTask.RookieTaskTrigger.COMMENT_STUDENT, ext);
    }

    /**
     * 处理唤醒老师的事件
     * @param inviterId 发起唤醒的老师ID，与任务中的teacherId一致
     * @param inviteeId 被唤醒的老师ID
     * @author zhouwei
     */
    private void processAwake(Long inviterId, Long inviteeId){
        if (inviterId == null || inviteeId == null) {
            return;
        }
        TeacherDetail invitee = teacherLoaderClient.loadTeacherDetail(inviteeId);
        Long teacherId = invitee.getId();//被邀请人ID

        //更新任务的进度
        Map<String,Object> var = MapUtils.m("activeNum","1", "inviteeId", inviteeId);
        teacherTaskService.updateProgress(inviterId, TeacherTaskTpl.TplEvaluatorEvent.USER_AWAKE, var);

        //通知邀请人被邀请人的完成情况
        BusinessEvent event = new BusinessEvent();
        event.setType(BusinessEventType.TEACHER_ACTIVATE_TEACHER_FINISH);
        event.getAttributes().put("inviteeId", teacherId);
        Message message = Message.newMessage().writeBinaryBody(JsonUtils.toJson(event).getBytes(ICharset.DEFAULT_CHARSET));
        businessQueueProducer.getProducer().produce(message);
    }

    /**
     * 处理检查作业的事件
     * @param teacherId
     * @param groupId
     * @param homeworkId
     * @param finishNum
     * @author zhouwei
     */
    private void processHomeworkAction(TeacherDetail teacherDetail, Long teacherId, Long groupId, String homeworkId, Integer finishNum, Long createAt, Long checkedAt,  String subject, String objectiveConfigTypes) {
        if (teacherId == null || groupId == null || homeworkId == null || createAt == null || checkedAt == null || subject == null || objectiveConfigTypes == null) {
            return;
        }

        finishNum = null == finishNum ? 0 : finishNum;
        final Integer finishNumFinal = finishNum;
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("hwId", homeworkId);
        varMap.put("finishNum", finishNumFinal);
        varMap.put("groupId", groupId);
        varMap.put("createAt", createAt);
        varMap.put("checkedAt", checkedAt);
        varMap.put("checkAt", checkedAt);
        varMap.put("subject", subject);
        varMap.put("objectiveConfigTypes", objectiveConfigTypes);
        varMap.put("messageType", "check");
        teacherTaskService.updateProgress(teacherId, TeacherTaskTpl.TplEvaluatorEvent.CHECK_HOMEWORK, varMap);

        /**
         * 被唤醒老师完成检查过作业以后，查找其是否正在被唤醒，找到唤醒他的老师ID，插入事件
         */
        Set<Long> activatingInviters = teacherActivateTeacherServiceImpl.getActivatingInviter(teacherId);
        List<TeacherActivate> teacherActivates = teacherActivateService.loadActivateInitOrIng(Collections.singleton(teacherId));

        // 如果自己醒过来了,要从待唤醒中剔除
        teacherActivates.stream()
                .filter(i -> Objects.equals(i.getStatus(), TeacherActivate.Status.INIT.getCode()))
                .forEach(teacherActivate -> teacherActivateService.cancel(teacherActivate.getId()));

        Set<Long> newVersionInviters = teacherActivates.stream()
                .filter(i -> Objects.equals(i.getStatus(), TeacherActivate.Status.ING.getCode()))
                .map(TeacherActivate::getUserId)
                .collect(Collectors.toSet());

        activatingInviters.addAll(newVersionInviters);

        if(CollectionUtils.isEmpty(activatingInviters)){
            return;
        }
        activatingInviters.forEach(inviterId -> {
            Map<String,Object> msgBody = new HashMap<>();
            msgBody.put("teacherId", inviterId);//发起唤醒老师的ID
            msgBody.put("inviteeId", teacherId);//被唤醒的老师ID
            msgBody.put("messageType", "awake");
            messagePublisherAwake.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgBody)));
        });
    }

    private void processWeekTask(TeacherDetail teacherDetail, Long groupId, String homeworkId, Integer finishNum) {
        if (teacherDetail == null || groupId == null || homeworkId == null || finishNum == null || Objects.equals(finishNum, 0)) {
            return;
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("hwId", homeworkId);
        varMap.put("finishNum", finishNum);
        //varMap.put("groupId", groupId);
        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
        if (group == null) {
            return;
        }
        Long clazzId = group.getClazzId();
        varMap.put("clazzId", clazzId);
        teacherTaskService.updateProgress(teacherDetail.getId(), TeacherTaskTpl.TplEvaluatorEvent.CHECK_HOMEWORK_V2, varMap);
    }

    private void processRookieTask(Long finalTeacherId, TeacherRookieTask.RookieTaskTrigger taskTrigger) {
        processRookieTask(finalTeacherId, taskTrigger, null);
    }

    private void processRookieTask(Long finalTeacherId, TeacherRookieTask.RookieTaskTrigger taskTrigger, Map<String, Object> exts) {
        try {
            teacherRookieTaskService.updateProgress(finalTeacherId, taskTrigger, exts);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void processMonthTask(Long finalTeacherId, Long groupId, String homeworkId, Integer studentCount) {
        try {
            teacherMonthTaskService.updateProgress(finalTeacherId, groupId, new TeacherMonthTask.Homework(homeworkId, studentCount));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
