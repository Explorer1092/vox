package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * push消息流程
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.PushMessageProcessor")
public class NotifyMessageProcessor implements IProcessor<ActivityContext> {

    //local variables
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    //Logic
    /**
     * 执行
     *
     * @param c
     */
    @Override
    public void process(ActivityContext c) {
        int i = c.getUserActivity().getExtInfo().size();
        String content = pushMsgs.get(i);
        LoggerUtils.debug("systemMessage", c, content);
        List<AppMessage> userMessageList = AppMessageUtils.generateAppUserMessage(Lists.newArrayList(c.getParentId()), ParentMessageType.REMINDER.getType(), content, content, "", OCRMA_ACTIVITY_URL, 1, null);
        userMessageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
    }

    /**
     * push message
     */
    private static Map<Integer, String> pushMsgs = Maps.newHashMap();
    static {
        pushMsgs.put(1, "口算打卡成功！5元直减券速来领走>>");
        pushMsgs.put(2, "口算打卡成功！5元直减券速来领走>>");
        pushMsgs.put(3, "口算打卡成功！10元直减券速来领走>>");
        pushMsgs.put(4, "口算打卡成功！速来把「锦鲤」抱走>>");
    }
    private static String OCRMA_ACTIVITY_URL = "/view/mobile/activity/parent/print_oral/index.vpage";
}
