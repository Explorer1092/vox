package com.voxlearning.utopia.service.campaign.impl.support;

import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Named
public class SendUtils {

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;

    public void sendAppMessage(long uid, String title, String msg, String linkUrl, int messageType, boolean isParent) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(uid);
        message.setTitle(title);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(linkUrl);
        message.setImageUrl("");
        message.setMessageType(messageType);
        if (isParent) {
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("tag", ParentMessageTag.通知.name());
            message.setExtInfo(extInfo);
        }
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
    }

    public void sendTeacherPush(long tid, String msg, String link, int messageType) {
        Map<String, Object> pushExtInfo = new HashMap<>();
        pushExtInfo.put("link", link);
        pushExtInfo.put("s", messageType);
        pushExtInfo.put("t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(msg,
                AppMessageSource.PRIMARY_TEACHER, Collections.singletonList(tid), pushExtInfo);
    }

    public void sendStudentPush(long sid, String msg, int appPushType, String link) {
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("link", link);
        extInfo.put("s", appPushType);
        extInfo.put("t", "h5");
        extInfo.put("key", "j");

        appMessageServiceClient.sendAppJpushMessageByIds(msg,
                AppMessageSource.STUDENT, Collections.singletonList(sid), extInfo);
    }

    public void sendParentPush(long pid, String msg, String appPushType, String url, String tag) {
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", url);
        extras.put("tag", tag);
        extras.put("s", appPushType);
        appMessageServiceClient.sendAppJpushMessageByIds(msg,
                AppMessageSource.PARENT, Collections.singletonList(pid), extras);
    }

}
