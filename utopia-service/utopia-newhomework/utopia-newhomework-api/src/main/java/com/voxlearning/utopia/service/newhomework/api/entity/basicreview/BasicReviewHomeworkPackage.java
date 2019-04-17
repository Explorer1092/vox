package com.voxlearning.utopia.service.newhomework.api.entity.basicreview;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.BasicReviewContentType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "basic_review_homework_package")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'bookId':1}", background = true),
        @DocumentIndex(def = "{'disabled':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181124")
public class BasicReviewHomeworkPackage implements Serializable {

    private static final long serialVersionUID = -6294756446233368382L;

    @DocumentId
    private String id;                                  // 主键
    private String actionId;                            // 布置id，“teacherId_${批量布置时间点}”
    private Subject subject;                            // 学科
    private HomeworkSourceType source;                  // 作业的布置来源
    private Long teacherId;                             // 老师id
    private Long clazzGroupId;                          // 班组id
    private String bookId;                              // 教材id
    private Integer homeworkDays;                       // 作业天数
    private List<BasicReviewStage> stages;              // 对应的关卡信息
    private List<BasicReviewContentType> contentTypes;  // 内容类型
    private Boolean disabled;                           // 默认false，删除true

    @DocumentCreateTimestamp
    private Date createAt;                              // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                              // 更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(BasicReviewHomeworkPackage.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(BasicReviewHomeworkPackage.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId});
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }
}
