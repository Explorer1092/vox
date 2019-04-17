package com.voxlearning.utopia.service.newhomework.api.entity.basicreview;

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
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "basic_review_homework_report_{}", dynamic = true)
@UtopiaCacheExpiration(259200)
@UtopiaCacheRevision("20180926")
public class BasicReviewHomeworkReport implements Serializable {

    private static final long serialVersionUID = 4479399627869527272L;

    @DocumentId
    private String id;                  // 与resultId保持一致
    private String homeworkId;          // 作业id
    private String packageId;           // 作业包id
    private Subject subject;            // 学科
    private Long clazzGroupId;          // 班组id
    private Long userId;                // 用户id

    private LinkedHashMap<ObjectiveConfigType, BasicReviewHomeworkReportDetail> practices;

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -7858910543012743779L;
        private String day;
        private Subject subject;
        private String hid;
        private String userId;

        @Override
        public String toString() {
            return day + "-" + subject + "-" + hid + "-" + userId;
        }
    }

    public BasicReviewHomeworkReport.ID parseId() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new BasicReviewHomeworkReport.ID(day, subject, hid, uid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(BasicReviewHomeworkReport.class, id);
    }
}
