package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.runtime.RuntimeMode;
import org.bson.types.ObjectId;

import java.util.Objects;

public class ActivityShardingUtils {

    private static final long SHARDING_TIME_POINT = 1541199600; // 2018/11/3 07:00:00 时间戳(秒)
    //private static final long SHARDING_TIME_POINT = 1541113200; // 2018/11/2 07:00:00 时间戳(秒)

    /**
     * 根据主键计算出表后缀
     */
    public static String getTableSuffixById(String id) {
        Validate.notNull(id);

        String[] idParts = id.split("-");
        if (idParts.length == 1) {
            return "";
        }

        Validate.isTrue(idParts.length == 2);

        String activityId = SafeConverter.toString(idParts[0]);
        return getTableSuffix(activityId);
    }

    /**
     * 根据 activityId 生成活动成绩表的主键
     */
    public static String generateId(String activityId) {
        Objects.requireNonNull(activityId);

        if (activityId.length() < 24) {
            return RandomUtils.nextObjectId();
        }
        // 指定时间之前的活动归到没有后缀的旧表
        int timestamp = new ObjectId(activityId).getTimestamp();
        if (timestamp < SHARDING_TIME_POINT) {
            return RandomUtils.nextObjectId();
        }

        return activityId + "-" + RandomUtils.nextObjectId();
    }

    /**
     * 根据 activityId 计算出表后缀
     */
    private static String getTableSuffix(String activityId) {
        Objects.requireNonNull(activityId);

        // 兼容手动下发的 activityId
        if (activityId.length() < 24) {
            return "";
        }
        // 指定时间之前的活动归到没有后缀的旧表
        int timestamp = new ObjectId(activityId).getTimestamp();
        if (timestamp < SHARDING_TIME_POINT) {
            return "";
        }

        long sharding = (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) ? 2 : 20;
        long mod = Math.abs(activityId.hashCode()) % sharding;

        return "_" + mod;
    }
}
