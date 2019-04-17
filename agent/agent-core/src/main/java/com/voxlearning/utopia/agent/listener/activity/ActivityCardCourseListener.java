package com.voxlearning.utopia.agent.listener.activity;

import com.voxlearning.alps.core.util.MapUtils;
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
import com.voxlearning.utopia.agent.service.activity.ActivityCardService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "solar.cardcode.activeSku.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "solar.cardcode.activeSku.topic"),
        }
)
public class ActivityCardCourseListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private ActivityCardService cardService;


    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "ActivityCardCourseListener");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);


        String code = SafeConverter.toString(dataMap.get("code"), "");   // 激活码

        Long cardUserId = SafeConverter.toLong(dataMap.get("parentId"));
        Long studentId = SafeConverter.toLong(dataMap.get("studentId"));

        List courseIds = (List)dataMap.get("skuIdList");
        List<String> courseList = new ArrayList<>();
        courseIds.forEach(p -> {
            if(p == null){
                return;
            }
            courseList.add(String.valueOf(p));
        });


        cardService.cardUsedByRedeemCode(code, studentId, courseList);

    }
}
