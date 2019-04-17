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

package com.voxlearning.utopia.service.wechat.impl.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeUpdateAction;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeUpdater;
import com.voxlearning.utopia.service.wechat.impl.service.WechatServiceImpl;
import org.slf4j.Logger;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * @author HuanYin Jia
 * @since 2015/5/29
 */
@Named
@Lazy(false)
public class WechatNoticeUpdateHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private Handler_DeleteWechatNotice deleteWechatNoticeHandler;

    @Inject private WechatServiceImpl wechatService;

    public boolean handle(WechatNoticeUpdater updater) throws Exception {
        Objects.requireNonNull(updater, "handle - Null WechatNoticeUpdater");
        WechatNoticeUpdateAction action = updater.getAction();
        if (action == null) {
            logger.error("handle - Null WechatNoticeUpdateAction");
            return false;
        }
        switch (action) {
            case UPDATE_MESSAGE_ID: {
                return updateNoticeMessageId(updater.getId(), updater.getMessageId());
            }
            case UPDATE_BY_ID: {
                return updateNoticeState(updater.getId(), updater.getState(), updater.getErrorCode());
            }
            case UPDATE_BY_MESSAGE_ID: {
                return updateNoticeState(updater.getOpenId(), updater.getMessageId(), updater.getState(), updater.getErrorCode());
            }
            case SCHEDULE__DELETE_WECHAT_NOTICE: {
                deleteWechatNoticeHandler.execute();
                return true;
            }
            default:
                logger.error("handle - Illegal WechatNoticeUpdateAction = {}", action);
                return false;
        }
    }

    public boolean updateNoticeMessageId(Long id, String messageId) {
        if (id == null) {
            return false;
        }
        wechatService.updateNoticeMessageId(id, messageId);
        return true;
    }

    public boolean updateNoticeState(Long id, WechatNoticeState state, String errorCode) {
        if (id == null) {
            return false;
        }
        wechatService.updateNoticeState(id, state, errorCode);
        return true;
    }

    private boolean updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode) {
        if (StringUtils.isBlank(openId) || StringUtils.isBlank(messageId)) {
            return false;
        }
        wechatService.updateNoticeState(openId, messageId, state, errorCode);
        return true;
    }
}
