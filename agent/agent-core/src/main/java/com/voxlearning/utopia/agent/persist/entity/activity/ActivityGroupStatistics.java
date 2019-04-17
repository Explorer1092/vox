package com.voxlearning.utopia.agent.persist.entity.activity;

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
@DocumentCollection(collection = "agent_activity_group_statistics")
public class ActivityGroupStatistics implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;               // 活动ID

    private Long userId;
    private String userName;
    private Integer day;                     // 日期

    private Integer groupCount;                   // 组团数
    private Integer completeGroupCount;           // 成功组团的数量

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_aid_uid_d(String activityId, Long userId, Integer day){
        return CacheKeyGenerator.generateCacheKey(ActivityGroupStatistics.class, new String[]{"aid", "uid", "d"}, new Object[]{activityId, userId, day});
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"aid", "uid", "d"}, new Object[]{this.activityId, this.userId, this.day})
        };
    }
}
