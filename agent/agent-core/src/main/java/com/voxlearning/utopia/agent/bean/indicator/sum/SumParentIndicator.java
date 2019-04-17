package com.voxlearning.utopia.agent.bean.indicator.sum;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.bean.indicator.BaseParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SumParentIndicator
 *
 * @author deliang.che
 * @since  2019/3/6
 */
@Getter
@Setter
@UtopiaCacheRevision("20190306")
public class SumParentIndicator extends BaseParentIndicator implements Serializable {


    private static final long serialVersionUID = -6052572409225965245L;
    private Integer day;                                                           // 业绩日期
    private Long id;
    private Integer idType;                         // 1:部门   2：user    3: 部门未分配
    private Integer schoolLevel;                    // 1: 小学，   2： 初中，  4：高中，   5：学前 ，  24：初高中,
    private String name;                                                           // 名称

    private int headCount;                                                    // 部门下的专员数量 idType = 1时有值

    private Map<Integer, ParentIndicator> indicatorMap = new HashMap<>();

    public static String ck_id_type_day_level(Long id, Integer idType, Integer day, Integer schoolLevel) {
        return CacheKeyGenerator.generateCacheKey(SumParentIndicator.class,
                new String[]{"i", "t", "d", "l"},
                new Object[]{id, idType, day, schoolLevel});
    }

    public boolean hasAllDimension(){
        return indicatorMap.size() == 4;
    }
}
