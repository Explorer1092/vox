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
@DocumentCollection(collection = "agent_activity_group")
public class ActivityGroup implements CacheDimensionDocument {
    @DocumentId
    private String id;

    private String activityId;              // 对应的活动ID

    private String groupId;                 // 组团ID
    private Date groupTime;                 // 组团时间

    private Boolean isComplete;             // 是否组团成功
    private Date completeTime;              // 组团成功时间

    private Long userId;                    // 市场人员ID
    private String userName;                // 市场人员姓名

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_aid_uid(String activityId, Long userId){
        return CacheKeyGenerator.generateCacheKey(ActivityGroup.class, new String[]{"aid", "uid"}, new Object[]{activityId, userId});
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("gid", this.groupId),
                newCacheKey(new String[]{"aid", "uid"}, new Object[]{this.activityId, this.userId})
        };
    }

}
