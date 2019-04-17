package com.voxlearning.utopia.service.action.impl.service.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.impl.support.ActionCacheSystem;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * Created by alex on 2017/12/26.
 */
@Named
public class ActionEventMonthRangeCounter_Like implements InitializingBean {

    @Inject
    private ActionCacheSystem actionCacheSystem;
    private Cache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        cache = actionCacheSystem.CBS.storage;
    }

    public long increase(ActionEvent event) {
        String month = MonthRange.newInstance(event.getTimestamp()).toString();
        String key = CacheKeyGenerator.generateCacheKey("ActionEventMonthRangeCounter_Like",
                new String[]{"userId", "type", "month"},
                new Object[]{event.getUserId(), event.getType(), month});
        int expiration = DateUtils.getCurrentToMonthEndSecond() + 86400;
        return SafeConverter.toLong(cache.incr(key, 1, 1, expiration));
    }

}
