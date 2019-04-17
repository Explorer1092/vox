package com.voxlearning.utopia.service.action.impl.service.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.impl.support.ActionCacheSystem;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 8/8/2016
 * 点赞数据计数器
 */
@Named
public class ActionEventDayRangeCounter_Like implements InitializingBean {

    @Inject
    private ActionCacheSystem actionCacheSystem;

    private Cache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        cache = actionCacheSystem.CBS.storage;
    }

    public long increase(ActionEvent event) {
        String day = DayRange.newInstance(event.getTimestamp()).toString();
        String key = CacheKeyGenerator.generateCacheKey("ActionEventDayRangeCounter_Like",
                new String[]{"userId", "day", "luserId", "type",
                        //如果是成就榜点赞会有以下两个参数,其它类型的点赞留空
                        "atype", "alevel", "cjId"
                },
                new Object[]{event.getUserId(), day, event.getAttributes().get("likedId"), event.getAttributes().get("type"),
                        (event.getAttributes().getOrDefault("achievementType", "")),
                        (event.getAttributes().getOrDefault("achievementLevel", "")),
                        (event.getAttributes().getOrDefault("journalId", "")),
                });
        int expiration = DateUtils.getCurrentToDayEndSecond();
        return SafeConverter.toLong(cache.incr(key, 1, 1, expiration));
    }
}
