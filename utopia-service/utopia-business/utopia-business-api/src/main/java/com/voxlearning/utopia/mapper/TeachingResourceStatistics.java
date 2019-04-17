package com.voxlearning.utopia.mapper;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@UtopiaCacheRevision("20180904")
public class TeachingResourceStatistics implements java.io.Serializable {
    private static final long serialVersionUID = 7993281011107703887L;

    private String id;
    private Long readCount;            // 阅读次数
    private Long collectCount;         // 收藏次数
    private Long participateNum;       // 每周活动的参与人数
    private Long finishNum;            // 每周活动的完成人数

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingResourceStatistics.class, id);
    }
}
