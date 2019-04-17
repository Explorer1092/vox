package com.voxlearning.utopia.service.ambassador.impl.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.ambassador.impl.service.AmbassadorServiceImpl;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
@SuppressWarnings("ALL")
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.ambassador.ambassador.mentor"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.ambassador.ambassador.mentor")
        }
)
public class RecordAmbassadorMentorKusteberListener implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.ambassador.ambassador.mentor")
    private MessagePublisher messagePublisher;

    @Inject
    private AmbassadorServiceImpl ambassadorService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        }

        Map<UserTagType, UserTagEventType> mapInfo = new HashMap<>();

        Long userId = MapUtils.getLong(msgMap, "userId");
        Map<Object, Object> map = (Map) msgMap.get("map");

        if (userId == null || map == null) return;

        // 怕出问题, 手动转成符合要求的泛型
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = SafeConverter.toString(entry.getKey());
            String value = SafeConverter.toString(entry.getValue());
            mapInfo.put(UserTagType.value(key), UserTagEventType.value(value));
        }

        ambassadorService.recordAmbassadorMentor(userId, mapInfo);
    }

//    public static void main(String[] args) {
//        Map<String, Object> par = new HashMap<>();
//        par.put("userId", 125110);
//        ExLinkedHashMap<Object, Object> map = MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_REWARD_ORDER, UserTagEventType.AMBASSADOR_MENTOR_REWARD_ORDER);
//        par.put("map", map);
//        System.out.println(JSON.toJSONString(par));
//
//        //  {"userId":125110,"map":{"AMBASSADOR_MENTOR_REWARD_ORDER":"AMBASSADOR_MENTOR_REWARD_ORDER"}}
//    }
}
