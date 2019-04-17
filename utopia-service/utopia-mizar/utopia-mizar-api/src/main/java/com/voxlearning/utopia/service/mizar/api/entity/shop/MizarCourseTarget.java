package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer Yang on 2016/9/21.
 * 课程配置实体
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "vox_course_target")
@DocumentIndexes({
        @DocumentIndex(def = "{'courseId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160919")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarCourseTarget implements Serializable {

    private static final long serialVersionUID = -222891414325183873L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID) private String id;
    private String courseId;
    private Integer targetType;     // 公众号投放对象类型 1-地区编码 2-用户ID 3-Tag标签 4广告投放标签  AdvertisementTargetType
    private String targetStr;
    private Boolean disabled;

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_courseId(String courseId) {
        return CacheKeyGenerator.generateCacheKey(MizarCourseTarget.class, "courseId", courseId);
    }

}