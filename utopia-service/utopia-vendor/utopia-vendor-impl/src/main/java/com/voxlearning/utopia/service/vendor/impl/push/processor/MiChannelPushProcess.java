package com.voxlearning.utopia.service.vendor.impl.push.processor;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushMeta;
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
public class MiChannelPushProcess extends SpringContainerSupport implements ChannelPushProcess {


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
            return buildTagPara(map);
        } else if (pushTarget == PushTarget.BATCH) {
            return Collections.singleton(buildSendBatchPara(map));
        }
        return Collections.emptySet();
    }

    private Map<String, Object> buildSendBatchPara(Map<String, Object> map) {

        Map<String, Object> extras = (Map<String, Object>) map.get("extInfo");
        List<Long> userIds = (List<Long>) map.get("userIdList");
        String content = (String) map.get("content");
        AppMessageSource source = (AppMessageSource) map.get("source");
        //构造请求
        HashMap<String, Object> hashMap = new HashMap<>();
        if (userIds.size() > 1) {
            hashMap.put("alias", StringUtils.join(userIds.toArray(), ","));
        } else {
            hashMap.put("alias", userIds.get(0));
        }
        hashMap.put("payload", content);
        hashMap.put("title", "家长通");
        hashMap.put("notify_type", 1);
        hashMap.put("restricted_package_name", getPackageName(source));
        for (Map.Entry entry : extras.entrySet()) {
            if (entry.getKey().equals("sound")) {
                continue;
            }
            if (entry.getKey().equals("timestamp")) {
                continue;
            }
            hashMap.put("extra." + entry.getKey(), entry.getValue());
        }
        hashMap.put("pass_through", 0);
        hashMap.put("description", content);
        return hashMap;
    }

    private Set<Map<String, Object>> buildTagPara(Map<String, Object> map) {

        List<String> tags = (List<String>) map.get("tags");
        List<String> tagsAnd = (List<String>) map.get("tagsAnd");
        Map<String, Object> extras = (Map<String, Object>) map.get("extInfo");
        String content = (String) map.get("content");
        AppMessageSource source = (AppMessageSource) map.get("source");
        if (tags == null)
            tags = new ArrayList<>();

        Set<Map<String, Object>> mapSet = new LinkedHashSet<>();

        if (CollectionUtils.isEmpty(tags) && CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.size() == 1) {
            tags.addAll(tagsAnd);
            tagsAnd = new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(tags) && CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.size() > 1) {
            tags.add(tagsAnd.get(0));
            tagsAnd.remove(0);
        }

        final List<String> finalTagsAnd = tagsAnd;
        tags.forEach(tag -> {
            //构造请求
            Map<String, Object> hashMap = new HashMap<>();
            List<String> audienceList = new ArrayList<>();
            audienceList.add(tag);
            if (CollectionUtils.isNotEmpty(finalTagsAnd)) {
                audienceList.addAll(finalTagsAnd);
            }
            if (audienceList.isEmpty()) { //如果没有tag,还发啥。。。不发了。。
                return;
            }
            //小米的multi topic接口不允许只放一个tag，当只有一个tag时放一个假的mock tag，并使用并集UNION
            if (audienceList.size() == 1) {
                String mockTopic = "mock";
                audienceList.add(mockTopic);
                hashMap.put("topic_op", "UNION");
            } else {
                hashMap.put("topic_op", "INTERSECTION");
            }
            hashMap.put("payload", content);
            hashMap.put("title", "家长通");
            hashMap.put("notify_type", 1);
            hashMap.put("restricted_package_name", getPackageName(source));
            hashMap.put("topics", StringUtils.join(audienceList.toArray(), ";$;") + ";$;");
            for (Map.Entry entry : extras.entrySet()) {
                if (entry.getKey().equals("sound")) {
                    continue;
                }
                if (entry.getKey().equals("timestamp")) {
                    continue;
                }
                hashMap.put("extra." + entry.getKey(), entry.getValue());
            }
            hashMap.put("pass_through", 0);
            hashMap.put("description", content);
            mapSet.add(hashMap);
        });

        return mapSet;
    }

    @Override
    public PushType getPushType() {
        return PushType.MI;
    }

    @Override
    public void product(Map<String, Object> paramMap) {

        String messageText = JsonUtils.toJson(paramMap);
        pushProducer.getMiProducer().produce(Message.newMessage().withStringBody(messageText));
    }

    @Override
    public String getPackageName(AppMessageSource source) {
        return PushMeta.MI.getPackageName();
    }
}
