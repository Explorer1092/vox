/*
package com.voxlearning.washington.support.queue;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.washington.cache.HomeworkCacheManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Map;

*/
/**
 * @author feng.guo
 * @since 2019-01-31
 *//*

@Slf4j
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "platform.queue.parent.homework.OCR_MENTAL_ARITHMETIC"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "platform.queue.parent.homework.OCR_MENTAL_ARITHMETIC")
}, maxPermits = 64)
public class PaperHomeworkListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        //获取消息body体
        Object body = message.decodeBody();

        //解析body体
        Map<String, Object> map = null;
        if (body instanceof String) {
            map = JsonUtils.fromJson(body.toString());
            if (MapUtils.isEmpty(map)) {
                return;
            }
        }

        //获取学生ID
        Long sid = SafeConverter.toLong(map.get("studentId"));
        if (null == sid || 0 == sid) {
            return;
        }

        //获取业务类型
        String bizType = SafeConverter.toString(map.get("bizType"));
        if (StringUtils.isBlank(bizType)) {
            return;
        }
        if ("OCR_MENTAL_ARITHMETIC".equals(bizType)) {
            bizType = "PARENT_ORAL_EXERCISE_ENTRY_NEW";
        }

        //获取消息类型
        String messageType = SafeConverter.toString(map.get("messageType"));
        if (StringUtils.isBlank(messageType) || "report".equals(messageType)) {
            return;
        }

        HomeworkCacheManager.setHomeWorkCache(sid, bizType);
    }
}
*/
