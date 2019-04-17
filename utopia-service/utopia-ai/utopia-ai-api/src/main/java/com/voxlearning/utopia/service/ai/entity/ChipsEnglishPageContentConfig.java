package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2018/9/10
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_chips_english_page_content_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181116")
public class ChipsEnglishPageContentConfig implements Serializable {

    @DocumentId
    private String id;
    @DocumentField
    private String name;
    @DocumentField
    private String value;
    @DocumentField
    private String memo;
    @DocumentField
    private Boolean disabled;
    @DocumentField
    private Date updateTime;
    @DocumentField
    private Date createTime;

//    public static String ck_all() {
//        return CacheKeyGenerator.generateCacheKey(ChipsEnglishPageContentConfig.class, "ALL");
//    }

}
