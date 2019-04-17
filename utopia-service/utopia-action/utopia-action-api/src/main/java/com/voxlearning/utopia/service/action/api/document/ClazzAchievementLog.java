package com.voxlearning.utopia.service.action.api.document;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author xinxin
 * @since 26/8/2016
 * 记录班级里每个成就的每个等级第一个获得的用户
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-achievement")
@DocumentCollection(collection = "vox_clazz_achievement_log_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClazzAchievementLog implements CacheDimensionDocument {

    private static final long serialVersionUID = -8030378876129779076L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    @DocumentField("ct")
    private Date createTime;
    @DocumentUpdateTimestamp
    @DocumentField("ut")
    private Date updateTime;

    private Long clazzId;
    private Long userId;
    private String achievementType;
    private Integer achievementLevel;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("CID", clazzId)
        };
    }
}
