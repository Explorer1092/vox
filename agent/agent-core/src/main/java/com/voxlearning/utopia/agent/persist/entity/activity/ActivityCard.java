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

/**
 * 保存活动对应的礼品卡数据
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_card")
public class ActivityCard implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;              // 对应的活动ID

    private String cardNo;                  // 礼品卡卡号
    private Long cardUserId;                // 领取礼品卡的用户ID
    private Date cardTime;                  // 领取时间
    private Date cardUserRegTime;           // 领取用户的注册时间

    private Boolean isUsed;               // 是否已使用

    private Long userId;                    // 市场人员ID
    private String userName;                // 市场人员姓名

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_aid_uid(String activityId, Long userId){
        return CacheKeyGenerator.generateCacheKey(ActivityCard.class, new String[]{"aid", "uid"}, new Object[]{activityId, userId});
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("cn", this.cardNo),
                newCacheKey(new String[]{"aid", "uid"}, new Object[]{this.activityId, this.userId})
        };
    }
}
