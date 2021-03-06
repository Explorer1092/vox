package com.voxlearning.utopia.agent.persist.entity.activity.palace;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
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
@DocumentCollection(collection = "agent_palace_activity_user_statistics")
public class PalaceActivityUserStatistics implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;               // 活动ID

    private Long userId;
    private String userName;
    private Integer day;                     // 日期

    private Integer couponCount;             // 优惠券数
    private Integer orderCount;              // 订单数
    private Integer attendClassStuCount;     // 上课学生数
    private Integer meetConditionStuCount;   // 上课并且满足市场指定条件的学生数

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"aid", "uid", "d"}, new Object[]{this.activityId, this.userId, day})
        };
    }
}
