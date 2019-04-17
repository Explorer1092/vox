package com.voxlearning.wechat.service.impl;

import com.voxlearning.wechat.cache.WechatWebCacheSystem;
import com.voxlearning.wechat.service.MessageService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
@Named("wechatMessageService")
public class MessageServiceImpl implements MessageService {
    private static final String CACHE_PREFIX = "WX_LAST_OP_TIME_";

    @Inject
    private WechatWebCacheSystem wechatWebCacheSystem;

    @Override
    public void updateActiveTime(String openId, Long opTime) {
        Objects.requireNonNull(openId, "openId must not be null.");
        Objects.requireNonNull(opTime, "opTime must not be null.");

        wechatWebCacheSystem.CBS.flushable.set(CACHE_PREFIX + openId, 48 * 60 * 60, opTime);
    }
}
