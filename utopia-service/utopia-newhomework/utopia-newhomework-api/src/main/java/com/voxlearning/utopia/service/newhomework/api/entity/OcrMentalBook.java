package com.voxlearning.utopia.service.newhomework.api.entity;

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
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-homework")
@DocumentCollection(collection = "ocr_mental_book")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190214")
public class OcrMentalBook implements Serializable {

    private static final long serialVersionUID = -4627966681019861234L;

    @DocumentId
    private String id;
    private Long teacherId;     // 老师id
    private String bookName;    // 课本名称
    public Boolean disabled;    // 默认false，删除true
    @DocumentCreateTimestamp
    private Date createTime;    // 记录创建时间
    @DocumentUpdateTimestamp
    private Date updateTime;    // 记录更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OcrMentalBook.class, id);
    }

    public static String ck_teacherId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(OcrMentalBook.class,
                new String[]{"TID"},
                new Object[]{teacherId});
    }
}
