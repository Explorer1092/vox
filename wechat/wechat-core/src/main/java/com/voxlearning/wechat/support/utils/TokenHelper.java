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

package com.voxlearning.wechat.support.utils;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.wechat.cache.WechatWebCacheSystem;
import com.voxlearning.wechat.context.WechatRequestContext;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Xin Xin
 * @since 10/23/15
 */
@Named
public class TokenHelper {
    private final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={}&type=jsapi";

    @Inject private WechatCodeServiceClient wechatCodeServiceClient;

    @Inject
    protected WechatServiceClient wechatServiceClient;
    @Inject
    private WechatWebCacheSystem wechatWebCacheSystem;

    public String getAccessToken(WechatType type) {
        return wechatCodeServiceClient.getWechatCodeService()
                .generateAccessToken(type)
                .getUninterruptibly();
    }

    public String getJsApiTicket(WechatType type) throws CannotAcquireLockException {
        return wechatCodeServiceClient.getWechatCodeService()
                .generateJsApiTicket(type)
                .getUninterruptibly();
    }

    /**
     * 为防止刷新口，生成一个contextId
     *
     * @param context
     * @return
     */
    public String generateContextId(WechatRequestContext context) {
        String contextId = RandomUtils.randomString(10);
        Boolean ret = wechatWebCacheSystem.CBS.unflushable.set("VrfCtxWx_" + contextId, 10 * 60, context.getRealRemoteAddr());

        if (!ret) {
            throw new IllegalStateException("create contextId error.");
        }
        return contextId;
    }

    /**
     * 验证contextId
     *
     * @param contextId
     * @return
     */
    public boolean verifyContextId(String contextId) {
        String ctxIp = wechatWebCacheSystem.CBS.unflushable.load("VrfCtxWx_" + contextId);
        return !StringUtils.isEmpty(ctxIp);
    }
}
