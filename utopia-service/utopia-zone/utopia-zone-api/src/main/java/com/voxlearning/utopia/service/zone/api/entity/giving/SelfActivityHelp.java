package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : kai.sun
 * @version : 2018-11-15
 * @description :
 **/
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_self_activity_help_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20181110")
public class SelfActivityHelp implements Serializable {

    private static final long serialVersionUID = 1280102968458608157L;

    /**一天最大助力次数*/
    public static final int MAX_LIMIT_HELP = 3;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    @DocumentField("activity_id")
    private Integer activityId;

    @DocumentField("clazz_id")
    private Long clazzId;

    @DocumentField("user_id")
    private Long userId;

    @DocumentField("school_id")
    private Long schoolId;

    private Integer num;        //

    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(SelfActivityHelp.class, new String[]{"activityId"}, new Object[]{activityId});
    }

    public static String cacheKeyFromClazzIdAndSchool(Integer activityId,Long schoolId,Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(SelfActivityHelp.class, new String[]{"activityId", "schoolId", "clazzId"}, new Object[]{activityId, schoolId, clazzId});
    }

    public static String cacheKeyFromClazzIdAndSchoolAndUserId(Integer activityId,Long schoolId,Long clazzId,Long userId) {
        return CacheKeyGenerator.generateCacheKey(SelfActivityHelp.class, new String[]{"activityId", "schoolId", "clazzId", "userId"}, new Object[]{activityId, schoolId, clazzId,userId});
    }

    /**主键id*/
    public static String generateId(Integer activityId,Long schoolId,Long clazzId,Long userId) {
        return activityId + "_" + schoolId + "_" + clazzId + "_" + userId + "_" + DateUtils.dateToString(new Date(),DateUtils.FORMAT_SQL_DATE);
    }

    /**主键id*/
    public void generateId() {
        id = activityId + "_" + schoolId + "_" + clazzId + "_" + userId + "_" + DateUtils.dateToString(new Date(),DateUtils.FORMAT_SQL_DATE);
    }

}
