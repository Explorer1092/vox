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
 * @since 2019/1/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_video_black_list")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190325")
public class ChipsVideoBlackList implements Serializable {

    private static final long serialVersionUID = -3399013852157420986L;

    @DocumentId
    private Long id;
    private String userName;
    @DocumentField("DISABLED")
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;

    //缓存key
    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsVideoBlackList.class, id);
    }
}
