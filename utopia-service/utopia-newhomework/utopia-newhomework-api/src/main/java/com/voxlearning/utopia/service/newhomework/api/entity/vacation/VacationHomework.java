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
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomework;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/11/24
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-winter-vacation-2019")
@DocumentCollection(collection = "vacation_homework")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'studentId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'packageId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181128")
public class VacationHomework extends BaseHomework implements Serializable {

    private static final long serialVersionUID = -6382677225729637230L;

    @DocumentId
    private String id;                      // packageId+weekRank+dayRank+studentId
    private Integer weekRank;               // 第几周
    private Integer dayRank;                // 第几天
    private String packageId;               // 假期作业包id
    private Long studentId;                 // 假期作业特有属性
    @DocumentCreateTimestamp
    private Date createAt;                  // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                  // 作业更新时间

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"packageId", "weekRank", "dayRank", "studentId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -6902101908522973275L;

        private String packageId;
        private Integer weekRank;
        private Integer dayRank;
        private Long studentId;

        @Override
        public String toString() {
            return packageId + "-" + weekRank + "-" + dayRank + "-" + studentId;
        }
    }

    public VacationHomework.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String packageId = segments[0];
        Integer weekRank = SafeConverter.toInt(segments[1]);
        Integer dayRank = SafeConverter.toInt(segments[2]);
        Long studentId = SafeConverter.toLong(segments[3]);
        return new VacationHomework.ID(packageId, weekRank, dayRank, studentId);
    }

    public VacationHomework.Location toLocation() {
        VacationHomework.Location location = new VacationHomework.Location();
        location.id = id;
        location.type = type == null ? NewHomeworkType.WinterVacation : type;
        location.weekRank = weekRank;
        location.dayRank = dayRank;
        location.packageId = packageId;
        location.teacherId = (teacherId == null ? 0 : teacherId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.studentId = (studentId == null ? 0 : studentId);
        location.createTime = (createAt == null ? 0 : createAt.getTime());
        location.startTime = (startTime == null ? 0 : startTime.getTime());
        location.endTime = (endTime == null ? 0 : endTime.getTime());
        location.actionId = actionId;
        location.subject = subject;
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = -7256152393463899448L;
        private String id;
        private String packageId;
        private Integer weekRank;
        private Integer dayRank;
        private NewHomeworkType type;
        private long teacherId;
        private long clazzGroupId;
        private long studentId;
        private long createTime;
        private long startTime;
        private long endTime;
        private String actionId;
        private Subject subject;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(VacationHomework.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(VacationHomework.class, "CG", clazzGroupId);
    }

    public static String ck_packageId(String packageId) {
        return CacheKeyGenerator.generateCacheKey(VacationHomework.class, "VP", packageId);
    }
}
