package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.business.impl.listener.TeacherHomeworkMsgHandler;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherVocationLottery;
import com.voxlearning.utopia.service.campaign.client.TeacherLotteryServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class TeacherVocationLotteryMsgHandler implements TeacherHomeworkMsgHandler{

    @Inject private TeacherLotteryServiceClient tchActService;

    @AlpsPubsubPublisher(topic = "utopia.homework.teacher.retry.topic")
    private MessagePublisher messagePublisher;

    @Override
    public void handle(Map<String, Object> msg) {
        Long teacherId = MapUtils.getLong(msg, "teacherId");
        String homeworkTypeStr = MapUtils.getString(msg,"homeworkType");

        NewHomeworkType homeworkType = NewHomeworkType.of(homeworkTypeStr);
        // 只看寒假作业的
        if (homeworkType != NewHomeworkType.WinterVacation)
            return;

        String msgType = MapUtils.getString(msg,"messageType");
        switch (msgType){
            case "assign": {
                processAssignAction(teacherId,homeworkTypeStr);
                break;
            }
            case "vocationLotteryRetryAssign": {
                processAssignAction(teacherId,homeworkTypeStr);
                break;
            }
            case "deleted":{
                processDeleteAction(teacherId);
                break;
            }
        }
    }

    private void processDeleteAction(Long teacherId){
        tchActService.incTVLRecordFields(teacherId,MapUtils.m("ASSIGN_TIME",-1));
    }

    private void processAssignAction(Long teacherId,String homeworkTypeCode) {
        try {
            AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherVocationLottery:insert")
                    .keys(teacherId)
                    .callback(() -> internalUpdateVocationLottery(teacherId))
                    .build()
                    .execute();
        }catch (CannotAcquireLockException e){
            // 睡一会儿，避免频繁撞消息
            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            Map<String,Object> msgBody = new HashMap<>();
            msgBody.put("teacherId",teacherId);
            msgBody.put("homeworkType",homeworkTypeCode);
            msgBody.put("messageType","vocationLotteryRetryAssign");
            messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgBody)));
        }
    }

    private MapMessage internalUpdateVocationLottery(Long teacherId){
        TeacherVocationLottery lotteryRecord = tchActService.loadTeacherVocationLottery(teacherId);
        if(lotteryRecord == null){
            lotteryRecord = new TeacherVocationLottery();
            lotteryRecord.setTeacherId(teacherId);
        }

        lotteryRecord.addAssignTime(1);
        return tchActService.updateTeacherVocationLottery(lotteryRecord);
    }
}
