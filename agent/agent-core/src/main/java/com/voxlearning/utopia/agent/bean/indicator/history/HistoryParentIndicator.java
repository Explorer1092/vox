package com.voxlearning.utopia.agent.bean.indicator.history;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * HistoryParentIndicator
 *
 * @author deliang.che
 * @since  2019/3/18
 */
@Getter
@Setter
@UtopiaCacheRevision("20190318")
public class HistoryParentIndicator extends ParentIndicator implements Serializable {


    private static final long serialVersionUID = -8646481065601293591L;
    private Integer date;

    public static String ck_id_type_day_level(Long id, Integer idType, Integer day, Integer schoolLevel) {
        return CacheKeyGenerator.generateCacheKey(HistoryParentIndicator.class,
                new String[]{"i", "t", "d", "l"},
                new Object[]{id, idType, day, schoolLevel});
    }

}
