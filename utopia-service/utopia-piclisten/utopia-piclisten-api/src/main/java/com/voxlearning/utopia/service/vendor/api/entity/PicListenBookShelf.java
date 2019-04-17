package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 书架上的书
 *
 * @author jiangpeng
 * @since 2017-03-02 下午1:22
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "piclisten_book_shelf")
@DocumentIndexes({
        @DocumentIndex(def = "{'parentId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class PicListenBookShelf implements CacheDimensionDocument {

    private static final long serialVersionUID = 1006181160859402944L;
    @DocumentId
    private String id;

    private Long parentId;

    private String bookId;

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_parentId(parentId),
                ck_parentIdWithDisabledCount(parentId)
        };
    }

    public static String ck_parentId(Long parentId){
        return CacheKeyGenerator.generateCacheKey(PicListenBookShelf.class, "PID", parentId);
    }
    public static String ck_parentIdWithDisabledCount(Long parentId){
        return CacheKeyGenerator.generateCacheKey(PicListenBookShelf.class, "PIDWD", parentId);
    }

    public PicListenBookShelf(Long parentId, String bookId){
        this.parentId = parentId;
        this.bookId = bookId;
        this.disabled = false;
    }

}
