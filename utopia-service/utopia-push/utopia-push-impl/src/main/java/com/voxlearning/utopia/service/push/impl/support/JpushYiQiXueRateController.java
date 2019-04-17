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
 * @author shiwei.liao
 * @since 2017-12-21
 */
@Spring
@Named
public class JpushYiQiXueRateController extends SpringContainerSupport {
    private static final int MAX_MESSAGE_COUNT_PER_MINUTE = 5000;

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
        String key = "JpushYiQiXueRateController::" + format.format(new Date(epochMilli));

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
