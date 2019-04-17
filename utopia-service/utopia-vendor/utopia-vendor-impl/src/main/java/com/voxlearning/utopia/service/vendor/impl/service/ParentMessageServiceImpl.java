/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.vendor.api.ParentMessageService;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author Jia HuanYin
 * @since 2015/9/16
 */
@Named
@Service(interfaceClass = ParentMessageService.class)
@ExposeServices({
        @ExposeService(interfaceClass = ParentMessageService.class, version = @ServiceVersion(version = "1.0.DEV")),
        @ExposeService(interfaceClass = ParentMessageService.class, version = @ServiceVersion(version = "1.1"))
})
public class ParentMessageServiceImpl extends SpringContainerSupport implements ParentMessageService {

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private AppMessageServiceImpl appMessageService;

    @Override
    public boolean postParentMessage(List<Long> parentIds,
                                     Long studentId,
                                     String content,
                                     String imageUrl,
                                     String linkUrl,
                                     String senderName,
                                     ParentMessageTag tag,
                                     ParentMessageType type) {
        return postParentMessage(parentIds, studentId, content, imageUrl, null, linkUrl, senderName, tag, type);
    }

    @Override
    public boolean postParentMessage(List<Long> parentIds,
                                     Long studentId,
                                     String content,
                                     String imageUrl,
                                     Integer linkType,
                                     String linkUrl,
                                     String senderName,
                                     ParentMessageTag tag,
                                     ParentMessageType type) {
        if (illegalParentMessage(parentIds, content, tag)) {
            logger.error("Illegal parent message");
            return false;
        }
        if (ParentMessageType.REMINDER == type) {
            List<AppMessage> messageList = new ArrayList<>();
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("studentId", studentId);
            extInfo.put("tag", tag == null ? "" : tag.name());
            extInfo.put("type", type.name());
            extInfo.put("senderName", senderName);
            for (Long parentId : parentIds) {
                //新消息中心
                AppMessage message = new AppMessage();
                message.setUserId(parentId);
                message.setContent(content);
                message.setLinkType(linkType);
                message.setLinkUrl(linkUrl);
                message.setImageUrl(imageUrl);
                message.setExtInfo(extInfo);
                message.setMessageType(type.getType());
                messageList.add(message);
            }
            messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        }
        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("studentId", studentId);
        extras.put("url", linkUrl);
        extras.put("tag", tag == null ? "" : tag.name());
        appMessageService.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentIds, extras);
        return true;
    }

    private boolean illegalParentMessage(Collection<Long> parentIds, String content, ParentMessageTag tag) {
        return CollectionUtils.isEmpty(parentIds) || StringUtils.isBlank(content) || tag == null;
    }
}
