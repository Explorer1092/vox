package com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "image_text_recommend")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181222")
public class ImageTextRecommend extends BaseVoiceRecommend implements Serializable {
    private static final long serialVersionUID = 7343742066268757980L;

    @DocumentId
    private String id;          // 与作业id一致
    @DocumentCreateTimestamp
    private Date createTime;    // 记录创建时间
    @DocumentUpdateTimestamp
    private Date updateTime;    // 记录更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ImageTextRecommend.class, id);
    }
}
