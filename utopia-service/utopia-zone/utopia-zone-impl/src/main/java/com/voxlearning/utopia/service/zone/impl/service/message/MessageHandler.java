package com.voxlearning.utopia.service.zone.impl.service.message;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.Map;

public abstract class MessageHandler {

    public abstract MapMessage handle(int protocol,Map<String, Object> map);

    private int startId;

    private int endId;

    public MessageHandler(int startId, int endId){
        this.startId = startId;
        this.endId = endId;
    }

    public String dispatch(Map<String, Object> map){
        int protocol = SafeConverter.toInt(map.get("protocol"));
        MapMessage result;
        if(protocol>startId&&protocol<endId){
            result = handle(protocol,map);
        }else {
            result = MapMessage.errorMessage("消息号错误");
        }
        return JsonUtils.toJson(result);
    }

}
