package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;

import java.util.HashMap;
import java.util.Map;

public class QueueMessageUtils {

    public static Map<String, Object> decodeMessage(Message message){
        Map<String, Object> dataMap = new HashMap<>();
        Object decoded = message.decodeBody();

        if (decoded instanceof String) {
            String messageText = (String) decoded;
            dataMap = JsonUtils.fromJson(messageText);
        }else if (decoded instanceof Map)
            dataMap = (Map) decoded;

        return dataMap;
    }
}
