package com.voxlearning.utopia.agent.listener.activity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderCourseDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderCourse;
import com.voxlearning.utopia.agent.service.activity.ActivityAttendCourseService;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "solar.studyCourse.tempdata.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "solar.studyCourse.tempdata.topic"),
        }
)
public class ActivityAttendCourseListener extends SpringContainerSupport implements MessageListener {


    @Inject
    private ActivityAttendCourseService activityAttendCourseService;
    @Inject
    private PalaceActivityService palaceActivityService;
    @Inject
    private ActivityOrderCourseDao orderCourseDao;

    @Override
    public void onMessage(Message message) {

        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        boolean isPalaceMuseum = SafeConverter.toBoolean(dataMap.get("isPalaceMuseum"));   // 是否是故宫立春课
        if(isPalaceMuseum){
            String eventType = SafeConverter.toString(dataMap.get("eventType"), "");
            if(StringUtils.isNotBlank(eventType) && Objects.equals(eventType, "PalaceMuseumFinishLesson")) {   // 学生每次完成一个模块
                Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "palace_course_message");
                logMap.put("messageInfo", JsonUtils.toJson(dataMap));
                LogCollector.info("backend-general", logMap);

                Long studentId = SafeConverter.toLong(dataMap.get("studentId"));
                String courseId = SafeConverter.toString(dataMap.get("courseId"), "");  // 课程ID
                if(Objects.equals(courseId, "25001")){
                    List<ActivityOrderCourse> orderCourseList = orderCourseDao.loadBySidAndCid(studentId, courseId);
                    if(CollectionUtils.isEmpty(orderCourseList)) {
                        palaceActivityService.resolveAttendClassData(studentId);
                    }
                }
            }
        }else {

            String eventType = SafeConverter.toString(dataMap.get("eventType"), "");
            if (StringUtils.isBlank(eventType) || !Objects.equals(eventType, "wheneverFinishLesson")) {
                return;
            }

            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "agent_activity_attend_course_message");
            logMap.put("messageInfo", JsonUtils.toJson(dataMap));
            LogCollector.info("backend-general", logMap);

            Long studentId = SafeConverter.toLong(dataMap.get("studentId"));
            String courseId = SafeConverter.toString(dataMap.get("courseId"), "");  // 课程ID
            long time = SafeConverter.toLong(dataMap.get("currentTime"));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            activityAttendCourseService.handleListenerData(studentId, courseId, calendar.getTime());
        }

    }
}
