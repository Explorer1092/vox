package com.voxlearning.utopia.agent.listener.activity.palace;

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
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

@Named
//@PubsubSubscriber(
//        destinations = {
//                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "solar.studyCourse.tempdata.topic"),
//                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "solar.studyCourse.tempdata.topic"),
//        }
//)
public class PalaceActivityCourseListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private PalaceActivityService palaceActivityService;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    private String CACHE_PREFIX = "palace_finish_course_count_";
    private String CACHE_PREFIX_DAY = "palace_finish_course_latest_day_";

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            dataMap = JsonUtils.fromJson(messageText);
        }else if (decoded instanceof Map)
            dataMap = (Map) decoded;

        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        boolean isPalaceMuseum = SafeConverter.toBoolean(dataMap.get("isPalaceMuseum"));   // 是否是故宫课
        if(isPalaceMuseum){
            String eventType = SafeConverter.toString(dataMap.get("eventType"), "");
            if(StringUtils.isNotBlank(eventType) && Objects.equals(eventType, "PalaceMuseumFinishLesson")){   // 学生第一次完成课程

                Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "palace_course_message");
                logMap.put("messageInfo", JsonUtils.toJson(dataMap));
                LogCollector.info("backend-general", logMap);

                Long studentId = SafeConverter.toLong(dataMap.get("studentId"));
                String courseId = SafeConverter.toString(dataMap.get("courseId"), "");  // 课程ID
                if(Objects.equals(courseId, "25001")){
                    palaceActivityService.resolveAttendClassData(studentId);
                }

//                Integer day = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMMdd"));


//                //
//                String dayKey = CACHE_PREFIX_DAY + studentId+ "_"+ courseId;
//                Integer cacheDay = SafeConverter.toInt(agentCacheSystem.CBS.persistence.load(dayKey));
//                if(!Objects.equals(cacheDay, day)){
//                    agentCacheSystem.CBS.persistence.set(dayKey, 0, day);
//
//                    String key = CACHE_PREFIX + studentId+ "_"+ courseId;
//                    Long count = agentCacheSystem.CBS.persistence.incr(key, 1, 1, 0);
//                    if(count == 3){
//                        palaceActivityService.resolveAttendClassData(studentId);
//                    }
//                }
            }
        }
    }
}
