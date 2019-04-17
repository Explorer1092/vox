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

package com.voxlearning.luffy.support.utils;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.luffy.cache.LuffyWebCacheSystem;
import com.voxlearning.luffy.context.LuffyRequestContext;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Xin Xin
 * @since 10/23/15
 */
@Named
public class TokenHelper {
    private final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={}&type=jsapi";

    @Inject
    private LuffyWebCacheSystem luffyWebCacheSystem;

    /**
     * 为防止刷新口，生成一个contextId
     *
     * @param context
     * @return
     */
    public String generateContextId(LuffyRequestContext context) {
        String contextId = RandomUtils.randomString(10);

        Boolean ret = luffyWebCacheSystem.CBS.unflushable.set("VrfCtxLf_" + contextId, 10 * 60, context.getRealRemoteAddr());

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
    public boolean verifyAndConsumeContextId(String contextId) {
        String ctxIp = luffyWebCacheSystem.CBS.unflushable.load("VrfCtxLf_" + contextId);
        if (StringUtils.isNotBlank(ctxIp)){
            luffyWebCacheSystem.CBS.unflushable.delete("VrfCtxLf_" + contextId);
            return true;
        }else
            return false;
    }
}
