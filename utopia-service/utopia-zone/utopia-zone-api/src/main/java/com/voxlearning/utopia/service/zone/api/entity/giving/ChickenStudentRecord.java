package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 吃鸡用户记录表
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_chicken_student_record_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181116")
public class ChickenStudentRecord implements Serializable {
    private static final long serialVersionUID = -2817707488135114925L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id; // activityId_schoolId_classId_userId_objectid
    private Integer activityId;
    private Long clazzId;
    private Long userId;
    private Long schoolId;
    private Integer type;
    @DocumentCreateTimestamp
    private Date ct;

    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ChickenStudentRecord.class, new String[]{"activityId"}, new Object[]{activityId});
    }

    public static String cacheKeyFromClazzIdAndSchool(Integer activityId,Long schoolId,Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ChickenStudentRecord.class, new String[]{"activityId", "schoolId", "clazzId"}, new Object[]{activityId, schoolId, clazzId});
    }

    public void generateId() {
        id = activityId + "_" + schoolId + "_" + clazzId + "_" + userId + "_" + RandomUtils.nextObjectId();
    }

}
