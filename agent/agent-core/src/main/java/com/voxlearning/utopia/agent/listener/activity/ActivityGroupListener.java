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
import com.voxlearning.utopia.agent.service.activity.ActivityGroupService;
import com.voxlearning.utopia.agent.service.activity.ActivityGroupUserService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "galaxy.groupon.tianji.notify.queue"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "galaxy.groupon.tianji.notify.queue"),
        }
)
public class ActivityGroupListener extends SpringContainerSupport implements MessageListener {


    @Inject
    private ActivityGroupService groupService;
    @Inject
    private ActivityGroupUserService groupUserService;

    @Override
    public void onMessage(Message message) {

        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "ActivityGroupListener-message");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);

        String type = SafeConverter.toString(dataMap.get("type"), "");
        String groupId = SafeConverter.toString(dataMap.get("groupId"), "");
        long time = SafeConverter.toLong(dataMap.get("ct"));
        Date date = new Date(time);
        if(Objects.equals(type, "CREATE")){
            String activityId = SafeConverter.toString(dataMap.get("activity_id"), "");
            Long userId = SafeConverter.toLong(dataMap.get("clerk_id"));
            Long groupUserId = SafeConverter.toLong(dataMap.get("uid"));
            groupService.addNewGroupData(activityId, groupId, date, groupUserId, userId);
        }else if(Objects.equals(type, "JOIN")){
            Long joinUserId = SafeConverter.toLong(dataMap.get("uid"));
            groupUserService.userJoinGroup(groupId, joinUserId, date, false);
        }else if(Objects.equals(type, "SUCCESS")){
            groupService.completeGroup(groupId, date);
        }else if(Objects.equals(type, "FAIL")){

        }
    }
}
