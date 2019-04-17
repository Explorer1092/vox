package com.voxlearning.utopia.service.newhomework.api.entity.vacation;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-winter-vacation-2019")
@DocumentCollection(collection = "vacation_homework_result")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181128")
public class VacationHomeworkResult extends BaseHomeworkResult implements Serializable {

    private static final long serialVersionUID = -4497933934163241179L;

    @DocumentId
    private String id;                      // packageId+weekRank+dayRank
    private Integer weekRank;               // 第几周
    private Integer dayRank;                // 第几天
    private String packageId;               // 假期作业包id
    @DocumentCreateTimestamp
    private Date createAt;                  // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                  // 作业更新时间

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"packageId", "weekRank", "dayRank", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -5023385150560604458L;

        private String packageId;
        private Integer weekRank;
        private Integer dayRank;
        private Long userId;

        @Override
        public String toString() {
            return packageId + "-" + weekRank + "-" + dayRank + "-" + userId;
        }
    }

    public VacationHomeworkResult.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String packageId = segments[0];
        Integer weekRank = SafeConverter.toInt(segments[1]);
        Integer dayRank = SafeConverter.toInt(segments[2]);
        Long uid = SafeConverter.toLong(segments[3]);
        return new VacationHomeworkResult.ID(packageId, weekRank, dayRank, uid);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = 2552375471484983061L;

        private String id;
        private Integer weekRank;               // 第几周
        private Integer dayRank;                // 第几天
        private String packageId;               // 假期作业包id
        private String homeworkId;              // 作业id，在假期作业里，这个id已经失去意义了
        private Subject subject;                // 学科
        private String actionId;                // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
        private Long clazzGroupId;              // 班组id
        private Long userId;                    // 用户id
        private Date createAt;                  // 作业生成时间
        private Date updateAt;                  // 作业更新时间
        private Date finishAt;
    }

    public VacationHomeworkResult.Location toLocation() {
        VacationHomeworkResult.Location location = new VacationHomeworkResult.Location();
        location.id = getId();
        location.weekRank = weekRank;
        location.dayRank = dayRank;
        location.packageId = packageId;
        location.homeworkId = homeworkId;
        location.subject = subject;
        location.actionId = actionId;
        location.clazzGroupId = clazzGroupId;
        location.userId = userId;
        location.createAt = createAt;
        location.updateAt = updateAt;
        location.finishAt = finishAt;
        return location;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(VacationHomeworkResult.class, id);
    }
}
