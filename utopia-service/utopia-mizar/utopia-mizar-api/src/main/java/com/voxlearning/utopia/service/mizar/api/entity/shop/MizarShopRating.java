package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 机构大数据相关信息,目前只有智能排序时用的到分值
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_shop_rating")
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'shop_id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160920")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarShopRating implements Serializable {
    private static final long serialVersionUID = 8121653890465230622L;

    @DocumentId                      private String id;                           //对应机构的id

    @DocumentField                   private Integer rating;                      // 大数据计算出来机构的得分

    @DocumentField("shop_id")        private String shopId;                       // 机构ID

    public static String ck_shopId(String shopId) {
        return CacheKeyGenerator.generateCacheKey(MizarShopRating.class, "shopId", shopId);
    }
}
