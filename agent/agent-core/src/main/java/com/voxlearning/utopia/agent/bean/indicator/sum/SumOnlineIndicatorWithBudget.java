package com.voxlearning.utopia.agent.bean.indicator.sum;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2018/8/22
 */
@Getter
@Setter
@UtopiaCacheRevision("20180822")
public class SumOnlineIndicatorWithBudget extends SumOnlineIndicator {

    private static final long serialVersionUID = -4533348057268904668L;

    private Map<AgentKpiType, Integer> kpiBudgetMap = new HashMap<>();                     // 预算数据

    public static String ck_id_type_day_level(Long id, Integer dataType, Integer day, Integer schoolLevel) {
        return CacheKeyGenerator.generateCacheKey(SumOnlineIndicatorWithBudget.class,
                new String[]{"i", "t", "d", "l"},
                new Object[]{id, dataType, day, schoolLevel});
    }
}
