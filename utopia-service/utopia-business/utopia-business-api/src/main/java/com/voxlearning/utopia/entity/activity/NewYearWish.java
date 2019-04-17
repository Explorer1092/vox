package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer on 2016/12/20.
 * 新年愿望记录
 */
@Setter
@Getter
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_new_year_wish")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161220")
@DocumentConnection(configName = "mongo-crm")
public class NewYearWish implements Serializable {

    private static final long serialVersionUID = -8218255369430757730L;
    @DocumentId private Long id;
    @DocumentField("wish_content") private String wishContent;
    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(NewYearWish.class, id);
    }
}
