package com.voxlearning.utopia.service.newhomework.api.entity;

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
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 离线作业（作业单，以消息形式发给家长）
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "offline_homework")
@DocumentIndexes({
        @DocumentIndex(def = "{'newHomeworkId':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160907")
public class OfflineHomework implements Serializable {
    private static final long serialVersionUID = -7223015227402674228L;

    @DocumentId
    private String id;
    private String newHomeworkId;                               // 关联的在线作业id(允许为空，一份在线作业只允许布置一份离线作业)
    private List<OfflineHomeworkPracticeContent> practices;     // 离线作业内容(有关联在线作业的情况下内容允许为空)
    private Boolean needSign;                                   // 是否需要家长签字
    private Date endTime;                                       // 作业结束时间

    private SchoolLevel schoolLevel;                            // 学段
    private Subject subject;                                    // 学科
    private String actionId;                                    // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    private Long teacherId;                                     // 老师id
    private String teacherName;                                 // 老师姓名
    private Long clazzGroupId;                                  // 班组id
    private HomeworkSourceType source;                          // 布置作业来源

    @DocumentCreateTimestamp
    private Date createAt;                                      // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                      // 作业更新时间
    private Map<String, String> additions;                      // 扩展字段

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OfflineHomework.class, id);
    }

    public static String ck_newHomeworkId(String newHomeworkId) {
        return CacheKeyGenerator.generateCacheKey(OfflineHomework.class,
                new String[]{"NHID"},
                new Object[]{newHomeworkId});
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(OfflineHomework.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId});
    }
}
