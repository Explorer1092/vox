package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.business.impl.listener.handler.TeacherTaskPrivilegeHandler;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 老师特权券删除失败了，在这里监听重试
 */
@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.teacher.task.privilege.coupon.delete.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.teacher.task.privilege.coupon.delete.topic"),
        }
)
public class TeacherTaskPrivilegeCouponDeleteListener implements MessageListener {

    @Inject
    private CouponServiceClient couponServiceClient;

    public void onMessage(Message message) {
        Map<String, Object> msgMap;
        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            log.warn("TeacherTaskPrivilegeCouponDeleteListener message decode failed!", JsonUtils.toJson(message.decodeBody()));
            return;
        }
        Long teacherId = MapUtils.getLong(msgMap, "teacher_id");
        String messageType = MapUtils.getString(msgMap, "messageType");
        List<String> refIds = (List<String>)msgMap.get("refIds");

        if (messageType != null && Objects.equals(messageType, "deleteCoupon") && CollectionUtils.isNotEmpty(refIds)) {
            MapMessage deleteMessage = couponServiceClient.removeCouponUserRefs(refIds);
            if (!deleteMessage.isSuccess()) {
                log.error("老师等级特权，删除券失败, teacherId:{}, refIds:{}", teacherId, refIds);
                try {
                    //打印一下日志
                    Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "teacherId", teacherId, "op", "teacher_task_privilege", "messageType", messageType);
                    logMap.put("messageInfo", JsonUtils.toJson(msgMap));
                    LogCollector.info("backend-general", logMap);
                } catch (Throwable t){
                }
            }
        }
    }
}
