package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304.dnf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * push消息流程
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.dnf.PushMessageProcessor")
public class DNFNotifyMessageProcessor implements IProcessor<ActivityContext> {

    //local variables
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    private RateLimiter rateLimiter = RateLimiter.create(66);

    //Logic
    /**
     * 执行
     *
     * @param c
     */
    @Override
    public void process(ActivityContext c) {
        List<Long> parentIds = c.get("udfParentIds");
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", OCRMA_ACTIVITY_URL);
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.ACTIVITY.name());
        int index = c.getActivity().currentPeriod().getIndex();
        String content = messages.get(index);
        int size = parentIds.size();
        AtomicInteger i = new AtomicInteger();

        Lists.partition(parentIds, 100).stream().forEach(list -> {
            LoggerUtils.info("dnf.pushMessage("+i.addAndGet(list.size()) + "/" + size + ")", content);
            if(!RuntimeMode.isStaging()){
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, list, extras);
            }
            List<AppMessage> userMessageList = AppMessageUtils.generateAppUserMessage(Lists.newArrayList(list), ParentMessageType.REMINDER.getType(), content, content, "", OCRMA_ACTIVITY_URL, 1, null);
            LoggerUtils.info("dnf.systemMessage", userMessageList.size());
            userMessageList.forEach(m->{
                rateLimiter.acquire();
//                LoggerUtils.debug("systemMessage...", m.getUserId());
                if(!RuntimeMode.isStaging()){
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(m);
                }
            });
        });

    }

    /**
     * push message
     */
    private static Map<Integer, String> messages = Maps.newHashMap();
    static {
        messages.put(1, "「锦鲤大奖」不要了吗？快来批改打卡>>");
        messages.put(2, "无门槛直减券等你领走！快来批改打卡>>");
        messages.put(3, "无门槛直减券等你领走！快来批改打卡>>");
        messages.put(4, "无门槛直减券等你领走！快来批改打卡>>");
    }
    private static String OCRMA_ACTIVITY_URL = "/view/mobile/activity/parent/print_oral/index.vpage";
}
