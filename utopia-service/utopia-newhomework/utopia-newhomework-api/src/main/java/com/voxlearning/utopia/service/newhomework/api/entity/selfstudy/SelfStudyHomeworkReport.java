package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/3/22
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "self_study_homework_report")
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20170322")
public class SelfStudyHomeworkReport implements Serializable {

    private static final long serialVersionUID = 1251249339595301723L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;              // 自学作业id

    // DIAGNOSTIC_CORRECTIONS --> Map<作业形式, Map<自学作业的做题id，做题的简要信息>>
    // DIAGNOSTIC_INTERVENTIONS --> Map<作业形式, Map<courseId|自学作业的做题id，做题的简要信息>>
    // ORAL_INTERVENTIONS --> Map<作业形式, Map<courseId，课程简要信息(冒充是一道题)>>
    public LinkedHashMap<ObjectiveConfigType, LinkedHashMap<String, SelfStudyHomeworkReportQuestion>> practices;

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    // 以下5个属性为冗余属性
    private String homeworkId;      // 原作业id
    private String selfStudyId;     // 自学作业id
    private Subject subject;        // 学科
    private Long groupId;           // 班组id
    private Long studentId;         // 学生id

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -5548124941546932340L;
        private String day;
        private String hid;
        private String userId;

        @Override
        public String toString() {
            return day + "_" + hid + "_" + userId;
        }
    }

    public SelfStudyHomeworkReport.ID parseID() {
        if (StringUtils.isBlank(id)) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) return null;
        String day = segments[0];
        String hid = segments[1];
        String uid = segments[2];
        return new SelfStudyHomeworkReport.ID(day, hid, uid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SelfStudyHomeworkReport.class, id);
    }
}
