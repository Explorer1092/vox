package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 原作业到自学错题巩固的映射关系
 * 按原作业的创建时间分库，不分表
 *
 * @author xuesong.zhang
 * @since 2017/3/22
 */
@Setter
@Getter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_self_study_ref")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170322")
public class HomeworkSelfStudyRef implements Serializable {

    private static final long serialVersionUID = -379902389478682661L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;              // 原作业id_studentId
    private String homeworkId;      // 原作业id
    private String selfStudyId;     // 自学错题订正作业，对应self_study_homework
    private Subject subject;        // 学科
    private Long groupId;           // 班组id
    private Long studentId;         // 学生id

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 8768181749267163076L;
        private String hid;
        private Long userId;

        public String getDay() {
            String[] segments = StringUtils.split(hid, "_");
            if (segments.length <= 0) {
                return "";
            }
            return segments[0];
        }

        @Override
        public String toString() {
            return hid + "_" + userId;
        }
    }

    public HomeworkSelfStudyRef.ID parseID() {
        if (StringUtils.isBlank(id)) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 4) return null;
        String day = segments[0];
        String oid = segments[1];
        String version = segments[2];
        Long uid = SafeConverter.toLong(segments[3], 0);
        String hid = day + "_" + oid + "_" + version;
        return new HomeworkSelfStudyRef.ID(hid, uid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(HomeworkSelfStudyRef.class, id);
    }

    // 预留一下，尽量不使用
//    public static String ck_homework(String homeworkId) {
//        return CacheKeyGenerator.generateCacheKey(HomeworkSelfStudyRef.class,
//                new String[]{"HID"},
//                new Object[]{homeworkId});
//    }
}
