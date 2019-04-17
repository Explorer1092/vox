package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.newhomework.batch.reward.integral.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.newhomework.batch.reward.integral.queue")
        },
        maxPermits = 256
)
public class BatchRewardIntegralQueueListener extends SpringContainerSupport implements MessageListener {


    @Inject private UserIntegralServiceClient userIntegralServiceClient;
    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = null;
        Object object = message.decodeBody();
        if (object instanceof String) {
            msgMap = JsonUtils.fromJson((String) object);
        }
        if (msgMap == null) {
            return;
        }
        try {
            Long studentId = SafeConverter.toLong(msgMap.get("studentId"));
            Integer count = SafeConverter.toInt(msgMap.get("count"));
            Long teacherId = SafeConverter.toLong(msgMap.get("teacherId"));
            NewHomework.Location location = JsonUtils.fromJson(JsonUtils.toJson(msgMap.get("homeworkLocation")), NewHomework.Location.class);
            String content = SafeConverter.toString(msgMap.get("content"));
            String link = SafeConverter.toString(msgMap.get("link"));
            Map extInfo = (Map) msgMap.get("extInfo");
            //给学生账户加学豆
            IntegralHistory integralHistory = new IntegralHistory(studentId, IntegralType.学生收到老师在作业报告发放的学豆_产品平台, count);
            integralHistory.setAddIntegralUserId(teacherId);
            integralHistory.setComment("老师奖励学豆");
            if (userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                newHomeworkResultService.saveHomeworkRewardIntegral(location, studentId, count);
                AppMessage msg = new AppMessage();
                msg.setUserId(studentId);
                msg.setMessageType(StudentAppPushType.HOMEWORK_SEND_INTEGRAL.getType());
                msg.setTitle("获得老师奖励");
                msg.setContent(content + count + "学豆，再接再励！");
                msg.setLinkUrl(link);
                msg.setLinkType(1); // 站内的相对地址
                //发送jpush消息
                appMessageServiceClient.sendAppJpushMessageByIds(content + count + "学豆，再接再励！", AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
                // 发送消息中心
                messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);
            }else {
                throw new IllegalArgumentException(StringUtils.formatMessage("学生{}加学豆失败Info {}", studentId, JsonUtils.toJson(object)));
            }
        }catch (Exception ex) {
            logger.error("teacher reward student error, info {}, error {}", JsonUtils.toJson(object), ex.getMessage());
        }
    }
}
