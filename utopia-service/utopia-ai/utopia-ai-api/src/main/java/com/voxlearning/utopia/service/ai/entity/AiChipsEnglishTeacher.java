package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
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
 * @since 2018/12/3
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_english_teacher")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181030")
public class AiChipsEnglishTeacher implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private String wxCode;
    private String qrImage;
    private String headPortrait;
    @DocumentCreateTimestamp
    private Date createDate; // 做题时间
    @DocumentUpdateTimestamp
    private Date updateDate; // 更新时间
    @DocumentField("DISABLED")
    private Boolean disabled;

    public static String ck_name(String name) {
        return CacheKeyGenerator.generateCacheKey(AiChipsEnglishTeacher.class, "name", name);
    }

    @Override
    public String toString() {
        return "AiChipsEnglishTeacher{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
