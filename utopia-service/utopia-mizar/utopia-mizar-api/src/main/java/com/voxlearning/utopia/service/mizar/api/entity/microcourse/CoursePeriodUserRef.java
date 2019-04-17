package com.voxlearning.utopia.service.mizar.api.entity.microcourse;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 微课堂课时与用户预约/购买 关联关系
 * Created by Wang Yuechen on 2016/12/21.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "course_period_user_ref")
@DocumentIndexes({
        @DocumentIndex(def = "{'periodId':1}", background = true),
        @DocumentIndex(def = "{'userId':1}", background = true),
})
@UtopiaCacheRevision("20170208")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class CoursePeriodUserRef implements CacheDimensionDocument {

    private static final long serialVersionUID = 1L;
    @DocumentId private String id;
    private String periodId;        // 课时ID
    private String userId;          // 用户ID
    private String targetId;        // 订单/预约单ID
    private UserPeriodRelation relation; // 购买/预约
    private Boolean fromWechat;     // 是否微信预约
    private Boolean notified;       // 是否已经通知过

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("P", periodId),
                newCacheKey(new String[]{"P", "U"}, new Object[]{periodId, userId})
        };
    }

    public enum UserPeriodRelation {
        Buy,        // 购买
        Reserve,    // 预约
        ;
    }
}
