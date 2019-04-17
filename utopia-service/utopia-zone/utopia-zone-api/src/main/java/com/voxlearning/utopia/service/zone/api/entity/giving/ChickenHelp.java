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
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 吃鸡助力类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_chicken_help_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181116")
public class ChickenHelp implements Serializable {
    private static final long serialVersionUID = -2817707483165114925L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id; // activityId_schoolId_classId_userId_type_time

    private Integer activityId;
    private Long clazzId;
    private Long userId;
    private Long schoolId;
    private List<Long> userList; //给该用户助力的user集合
    private Boolean status; //是否完成助力
    private Boolean tipStatus; //是否提示过
    //类型 1：烤箱 2：托盘  3：鸡
    //剧情活动 1：个人排名奖励
    private Integer type;
    @DocumentCreateTimestamp
    private Date ct;

    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ChickenHelp.class, new String[]{"activityId"}, new Object[]{activityId});
    }

    public static String cacheKeyFromClazzIdAndSchool(Integer activityId,Long schoolId,Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ChickenHelp.class, new String[]{"activityId", "schoolId", "clazzId"}, new Object[]{activityId, schoolId, clazzId});
    }

    public static String cacheKeyFromClazzIdAndSchoolAndUserId(Integer activityId,Long schoolId,Long clazzId,Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChickenHelp.class, new String[]{"activityId", "schoolId", "clazzId", "userId"}, new Object[]{activityId, schoolId, clazzId,userId});
    }

    public void generateId() {
        id = activityId + "_" + schoolId + "_" + clazzId + "_" + userId + "_" + type + "_" + DateUtils.dateToString(new Date(),DateUtils.FORMAT_SQL_DATE);
    }

}
