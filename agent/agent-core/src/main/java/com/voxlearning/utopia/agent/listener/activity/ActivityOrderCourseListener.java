package com.voxlearning.utopia.agent.listener.activity;

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
import com.voxlearning.utopia.agent.service.activity.ActivityOrderCourseService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "solar.order.activeSku.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "solar.order.activeSku.topic"),
        }
)
public class ActivityOrderCourseListener extends SpringContainerSupport implements MessageListener {



    @Inject
    private ActivityOrderCourseService activityOrderCourseService;

    @Override
    public void onMessage(Message message) {

        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        String eventType = SafeConverter.toString(dataMap.get("eventType"), "");
        if(StringUtils.isNotBlank(eventType) && Objects.equals(eventType, "orderActiveSkuId")){

            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "agent_activity_order_course_message");
            logMap.put("messageInfo", JsonUtils.toJson(dataMap));
            LogCollector.info("backend-general", logMap);

            String orderId = SafeConverter.toString(dataMap.get("orderId"), "");
            Long studentId = SafeConverter.toLong(dataMap.get("studentId"));

            Map<String, Map<String, Object>> courseIdMap = (Map<String, Map<String, Object>>)dataMap.get("courseIdInfoMap");
            List<Map<String, Object>> courseInfoList = new ArrayList<>();
            courseIdMap.forEach((k, v) -> {
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("courseId", k);
                courseInfo.put("courseName", v.get("name"));
                courseInfo.put("isFirstActive", v.get("isFirstActive"));
                courseInfoList.add(courseInfo);
            });
            activityOrderCourseService.handleListenerData(orderId, studentId, courseInfoList);
        }
    }
}
