package com.voxlearning.utopia.service.rstaff.impl.lintener;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.couchbase.module.monitor.dsl.CBS;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * describe:
 * 校长首页消息收集器
 * @author yong.liu
 * @date 2019/03/05
 */
@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.student.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.student.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.schoolmaster.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.schoolmaster.topic"),
        },
        maxPermits = 6
)

public class SchoolmasterHomePageMsgListener  extends SpringContainerSupport implements MessageListener {

    @Inject TeacherLoaderClient teacherLoaderClient;
    @Inject StudentLoaderClient studentLoaderClient;

    @AlpsPubsubPublisher(topic = "utopia.homework.schoolmaster.topic")
    private MessagePublisher messagePublisher;

    @Override
    public void onMessage(Message message) {
            Object msg = message.decodeBody();
            Map<String, Object> messageMap = JsonUtils.fromJson((String) msg);
            String messageType = SafeConverter.toString(messageMap.get("messageType"));
            Long schoolId = 0L;
            String msgStr = "";
            if (Objects.equals(HomeworkPublishMessageType.assign.name(),messageType) || Objects.equals(HomeworkPublishMessageType.checked.name(),messageType)) {
                Long teacherId = SafeConverter.toLong(messageMap.get("teacherId"));
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                if(Objects.nonNull(teacherDetail)){
                    schoolId = teacherDetail.getTeacherSchoolId();
                    String subjectName = SafeConverter.toString(messageMap.get("subject"));
                    Subject subject = Subject.of(subjectName);
                    if (Objects.equals(HomeworkPublishMessageType.assign.name(), messageType) || Objects.equals("retryAssign",messageType)) {
                        msgStr = teacherDetail.fetchRealname() + "老师布置了一份" + subject.getValue() + "作业";
                    } else {
                        msgStr = teacherDetail.fetchRealname() + "老师批改了一份" + subject.getValue() + "作业";
                    }
                }else{
                    logger.error("有空值的老师ID {},和老师信息{}",teacherId, JsonUtils.toJson(teacherDetail));
                }
            } else if (Objects.equals(HomeworkPublishMessageType.finished.name(),messageType)){
                Long studentId = SafeConverter.toLong(messageMap.get("studentId"));
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                if(Objects.nonNull(studentDetail) && Objects.nonNull(studentDetail.getClazz())){
                    schoolId = studentDetail.getClazz().getSchoolId();
                    String clazzName = studentDetail.getClazz().getClassName();
                    String studentName = studentDetail.fetchRealname();
                    Boolean repair = SafeConverter.toBoolean(messageMap.get("repair"));
                    String subjectName = SafeConverter.toString(messageMap.get("subject"));
                    Subject subject = Subject.of(subjectName);
                    if (repair) {
                        msgStr = clazzName + studentName + "同学补做了" + subject.getValue() + "作业";
                    } else {
                        msgStr = clazzName + studentName + "同学完成了" + subject.getValue() + "作业";
                    }
                }else{
                    logger.error("有空值的学生ID {},和学生信息{},消息内容{}",studentId, JsonUtils.toJson(studentDetail),msg);
                }
            }
        try {

            if (!Objects.equals(schoolId, 0L) && StringUtils.isNotBlank(msgStr)) {
                Long finalSchoolId = schoolId;
                String finalMsgStr = msgStr;
                AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("schoolmasterhomepagemsglist:incr")
                        .keys(schoolId)
                        .callback(() -> incrMsg(finalSchoolId, finalMsgStr))
                        .build()
                        .execute();
            }
        } catch (CannotAcquireLockException e) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(messageMap)));
        }catch (Exception e){
            logger.error("schoolmasterHomePageMsgListener is Error",e);
        }
    }

    private MapMessage incrMsg(Long schoolId , String msg){
        UtopiaCache cache = CacheSystem.CBS.getCache("storage");
        Object msgListObj = cache.load("SCHOOLMASTER_HOMEPAGE_MSGLIST_"+schoolId);
        LinkedList<String> msgList = null;
        if(Objects.isNull(msgListObj)){
            msgList = new LinkedList<>();
        }else{
            msgList = (LinkedList<String>)msgListObj;
        }

        if(msgList.size() >= 200){
            msgList.removeFirst();
            msgList.add(msg);
        }else{
            msgList.add(msg);
        }
        cache.set("SCHOOLMASTER_HOMEPAGE_MSGLIST_"+schoolId,0,msgList);
        return MapMessage.errorMessage();
    }
}
