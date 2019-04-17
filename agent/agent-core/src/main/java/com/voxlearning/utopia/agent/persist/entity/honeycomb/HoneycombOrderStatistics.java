package com.voxlearning.utopia.agent.persist.entity.honeycomb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_honeycomb_order_statistics")
public class HoneycombOrderStatistics implements CacheDimensionDocument {


    @DocumentId
    private String id;
    private String activityId;               // 活动ID
    private Long honeycombId;                // 蜂巢用户ID
    private Integer day;                     // 日期

    private Integer count;                   // 订单数

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    public static String ck_uid_d_aid(Long honeycombId, Integer day, String activityId){
        return CacheKeyGenerator.generateCacheKey(HoneycombOrderStatistics.class, new String[]{"uid", "d", "aid"}, new Object[]{honeycombId, day, activityId});
    }

    public static String ck_uid_d(Long honeycombId, Integer day){
        return CacheKeyGenerator.generateCacheKey(HoneycombOrderStatistics.class, new String[]{"uid", "d"}, new Object[]{honeycombId, day});
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"uid", "d", "aid", }, new Object[]{this.honeycombId, this.day, this.activityId}),
                newCacheKey(new String[]{"uid", "d"}, new Object[]{this.honeycombId, this.day})
        };
    }
}
