package com.voxlearning.utopia.service.newhomework.api.entity.sub;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_result_answer_{}", dynamic = true)
@UtopiaCacheExpiration(345600)
@UtopiaCacheRevision("20190123")
@UtopiaCachePrefix(prefix = "SubHomeworkResultAnswer")
public class SubHomeworkResultAnswer implements Serializable {

    private static final long serialVersionUID = 1241986951133626351L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private String processId;   // 做题明细id
    private Boolean isOral;     // 绘本的跟读题标识
    private Boolean isImageTextRhyme;   // 字词讲练图文入韵标识
    private Boolean isChineseCourse;   // 字词讲练汉字文化标识
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 6172790552790980961L;
        private String day;
        private String hid;                 // 作业id
        private String userId;              // 学生id
        private ObjectiveConfigType type;   // 作业形式
        private Collection<String> joinKeys;// 组合key，基础训练（category+lesson）;绘本（阅读绘本和视频绘本）
        // private Long categoryId;            //
        // private String lessonId;            //
        // private String pvBookId;            //
        private String questionId;          // 题目id

        @Override
        public String toString() {
            List<Object> ids = new ArrayList<>();
            ids.add(day);
            ids.add(hid);
            ids.add(userId);
            ids.add(type);
            if (CollectionUtils.isNotEmpty(joinKeys)) {
                // 基础训练
                ids.add(StringUtils.join(joinKeys, "-"));
            }
            ids.add(questionId);
            return StringUtils.join(ids, "|");
        }
    }

    public SubHomeworkResultAnswer.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "|");
        if (segments.length != 6 && segments.length != 5) return null;
        String day = segments[0];
        String hid = segments[1];
        String uid = segments[2];
        ObjectiveConfigType type = ObjectiveConfigType.of(segments[3]);
        Collection<String> joinKeys = null;
        if (segments.length == 6) {
            // 6位的话有joinKey，5位的话没有这个属性
            String[] keys = StringUtils.split(segments[4], "-");
            joinKeys = Arrays.asList(keys);
        }
        String qid = segments[segments.length - 1];
        return new SubHomeworkResultAnswer.ID(day, hid, uid, type, joinKeys, qid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SubHomeworkResultAnswer.class, id);
    }
}
