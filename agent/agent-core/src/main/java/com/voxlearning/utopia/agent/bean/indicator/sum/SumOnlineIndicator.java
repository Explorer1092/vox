package com.voxlearning.utopia.agent.bean.indicator.sum;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.bean.indicator.BaseOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SumOnlineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Getter
@Setter
@UtopiaCacheRevision("20180822")
public class SumOnlineIndicator extends BaseOnlineIndicator implements Serializable {

    private static final long serialVersionUID = -2714221523046384520L;

    private Integer day;                                                           // 业绩日期
    private Long id;
    private Integer dataType;                         // 1:部门   2：user    3: 部门未分配
    private Integer schoolLevel;                    // 1: 小学，   2： 初中，  4：高中，   5：学前 ，  24：初高中,
    private String name;                                                           // 名称

    private int headCount;                                                    // 部门下的专员数量 dataType = 1时有值

    private Map<Integer, OnlineIndicator> indicatorMap = new HashMap<>();

    public static String ck_id_type_day_level(Long id, Integer dataType, Integer day, Integer schoolLevel) {
        return CacheKeyGenerator.generateCacheKey(SumOnlineIndicator.class,
                new String[]{"i", "t", "d", "l"},
                new Object[]{id, dataType, day, schoolLevel});
    }

    public boolean hasAllDimension(){
        return indicatorMap.size() == 4;
    }
}
