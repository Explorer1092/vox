package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班级吃鸡进度统计表
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_chicken_clazz_progress_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181116")
public class ChickenClazzProgress implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id; // activityId_schoolId_classId

    private Integer activityId;
    private Long clazzId;
    private Long schoolId;
    private Long count;

    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ChickenClazzProgress.class, new String[]{"activityId"}, new Object[]{activityId});
    }

    public static String cacheKeyFromClazzId(Integer activityId,Long schoolId,Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ChickenClazzProgress.class, new String[]{"activityId", "schoolId", "clazzId"}, new Object[]{activityId, schoolId, clazzId});
    }

    public static String cacheKeyFromClazzId(Integer activityId,Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(ChickenClazzProgress.class, new String[]{"activityId", "schoolId"}, new Object[]{activityId, schoolId});
    }

    public void generateId() {
        id = activityId + "_" + schoolId + "_" + clazzId;
    }

    public static String generateId(Integer activityId,Long clazzId,Long schoolId) {
        return activityId + "_" + schoolId + "_" + clazzId;
    }

}
