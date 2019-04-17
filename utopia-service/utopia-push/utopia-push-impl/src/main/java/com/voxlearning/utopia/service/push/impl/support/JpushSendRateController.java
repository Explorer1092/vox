/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.push.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;

import javax.inject.Named;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JPUSH 消息速率控制器
 * 每分钟最大119000条消息
 */
@Spring
@Named
public class JpushSendRateController extends SpringContainerSupport {

    private static final int MAX_MESSAGE_COUNT_PER_MINUTE = 214000;

    public void acquire() {
        AtomicBoolean flag = new AtomicBoolean(false);
        while (true) {
            doAcquire(flag);
            if (flag.get()) {
                break;
            }
        }
    }

    private void doAcquire(AtomicBoolean flag) {
        long epochMilli = System.currentTimeMillis();

        FastDateFormat format = FastDateFormat.getInstance("yyyyMMddHHmm");
        String key = "JpushSendRateController::" + format.format(new Date(epochMilli));

        Long ret = CacheSystem.CBS.getCache("unflushable").incr(key, 1, 1, 60);
        if (ret == null) {
            logger.warn("Cache server access failed, ignore");
            flag.set(true);
            return;
        }
        long count = ret;
        if (count <= MAX_MESSAGE_COUNT_PER_MINUTE) {
            flag.set(true);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMilli);
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long s = calendar.getTimeInMillis() - epochMilli + 1;
        ThreadUtils.sleepCurrentThread(s);
    }
}
