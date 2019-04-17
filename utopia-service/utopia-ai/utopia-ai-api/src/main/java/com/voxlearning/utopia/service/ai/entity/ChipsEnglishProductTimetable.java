package com.voxlearning.utopia.service.ai.entity;

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
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/10/17
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_english_product_timetable")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181017")
public class ChipsEnglishProductTimetable implements Serializable{
    private static final long serialVersionUID = -950058194329641828L;
    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private Date beginDate;
    private Date endDate;
    private List<Course> courses;

    @Getter
    @Setter
    public static class Course implements Serializable{
        private static final long serialVersionUID = 2026016978171686571L;
        private Date beginDate;
        private String unitId;
        private String bookId;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishProductTimetable.class, id);
    }

}
