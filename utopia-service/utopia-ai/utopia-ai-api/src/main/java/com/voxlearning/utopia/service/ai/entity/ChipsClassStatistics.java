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

/**
 * @author guangqing
 * @since 2019/1/17
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_class_statistics")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 10800)
@UtopiaCacheRevision("20181130")
public class ChipsClassStatistics implements Serializable {

    private static final long serialVersionUID = 5691222368884229062L;
    @DocumentId
    private String id;
    @DocumentField(value = "classid")
    private Long classId;
    @DocumentField(value = "product_id")
    private String productId;
    @DocumentField(value = "classranknum")
    private Integer classRankNum;//本班定级人数
    @DocumentField(value = "classpaidnum")
    private Integer classPaidNum;//本班续费人数
    @DocumentField(value = "classusernum")
    private Integer classUserNum;//本班应到学生数
    @DocumentField(value = "totalranknum")
    private Integer totalRankNum;//本期定级人数
    @DocumentField(value = "totalpaidnum")
    private Integer totalPaidNum;//本期续费人数
    @DocumentField(value = "totalusernum")
    private Integer totalUserNum;//本期应到学生数
    @DocumentField(value = "createdate")
    private String createDate;

    //缓存key
    public static String ck_class_id(Long classId) {
        return CacheKeyGenerator.generateCacheKey(ChipsClassStatistics.class, "CID", classId);
    }
}
