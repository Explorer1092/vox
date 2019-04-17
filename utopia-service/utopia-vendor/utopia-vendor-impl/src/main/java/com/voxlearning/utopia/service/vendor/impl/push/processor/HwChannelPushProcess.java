package com.voxlearning.utopia.service.vendor.impl.push.processor;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.vendor.impl.push.AppPushChannelManager;
import com.voxlearning.utopia.service.vendor.impl.push.PushProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by wangshichao on 16/8/25.
 */
@Named
public class HwChannelPushProcess extends SpringContainerSupport implements ChannelPushProcess {

    @Inject
    private PushProducer pushProducer;

    @Inject
    AppPushChannelManager appPushChannelManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        appPushChannelManager.register(this);
    }

    @Override
    public Set<Map<String, Object>> buildSendParams(Map<String, Object> map, PushTarget pushTarget) {

        if (pushTarget == PushTarget.TAG) {
            Map<String, Object> mapResult = buildTagPara(map);
            return Collections.singleton(mapResult);
        } else if (pushTarget == PushTarget.BATCH) {
            return Collections.singleton(buildSendBatchPara(map));
        }
        return Collections.emptySet();
    }


    private Map<String, Object> buildSendBatchPara(Map<String, Object> map) {

        List<Long> userIds = (List<Long>) map.get("userIdList");
        String content = (String) map.get("content");
        //构造请求
        //构造请求
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("deviceTokenList", userIds);
        hashMap.put("message", content);
        hashMap.put("cacheMode", 1);
        hashMap.put("msgType", 1);
        return hashMap;
    }

    private Map<String, Object> buildTagPara(Map<String, Object> map) {

        List<String> tags = (List<String>) map.get("tags");
        List<String> tagsAnd = (List<String>) map.get("tagsAnd");
        Map<String, Object> extras = (Map<String, Object>) map.get("extInfo");
        String content = (String) map.get("content");
        //推送范围，必选
        //1：指定用户，必须指定tokens字段
        //2：所有人，无需指定tokens，tags，exclude_tags
        //3：一群人，必须指定tags或者exclude_tags字段
        Integer push_type = 3;
        //标签，可选
        //当push_type的取值为2时，该字段生效
        Map<String, Object> tagMap = new HashMap<>();
        Set<String> tagSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(tags)) {
            tagSet.addAll(tags);
        }
        if (CollectionUtils.isNotEmpty(tagsAnd)) {
            tagSet.addAll(tagsAnd);
        }
        Map innerMap = new HashMap();
        List list = new ArrayList();
        innerMap.put("tags", tagSet);
        list.add(innerMap);
        tagMap.put("tag", list);
        HashMap android = new HashMap();
        android.put("notification_title", "");
        android.put("notification_content", content);
        android.put("doings", 1);
        android.put("extras", extras);
        //构造请求
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("push_type", push_type);
        hashMap.put("tags", JsonUtils.toJson(tagMap));
        hashMap.put("android", JsonUtils.toJson(android));
        return hashMap;
    }

    @Override
    public PushType getPushType() {
        return PushType.HW;
    }

    @Override
    public void product(Map<String, Object> paramMap) {
        pushProducer.getHwProducer().produce(Message.newMessage().withStringBody(JsonUtils.toJson(paramMap)));
    }

}
