package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by Summer on 2018/3/26
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_user_lesson_result_history")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190412")
public class AIUserLessonResultHistory implements Serializable {

    private static final long serialVersionUID = 4364089815230239584L;
    public static final String ID_SEP = "-";

    @DocumentId
    private String id;
    private Long userId;
    private String unitId;
    private String bookId;
    private String lessonId;
    private Boolean finished;
    private Integer score;
    private Integer star;
    private Integer currentStar;//用户该次回答的星星数
    private LessonType lessonType;    // 课程类型  热身训练  情景对话  任务
    private String userVideo;         // 用户视频 - 目前只有情景对话有
    private String userVideoId;      // 用户视频id- 目前只有情景对话有
    private Map<String, Object> ext;

    //FIXME lesson上的下面维度的分数没有用
    @Deprecated
    private Integer independent; // 独立性维度分数
    @Deprecated
    private Integer listening; // 听力维度分数
    @Deprecated
    private Integer express; // 表达维度分数
    @Deprecated
    private BigDecimal fluency; // 流利性维度分数
    @Deprecated
    private BigDecimal pronunciation; // 发音维度分数

    @DocumentCreateTimestamp private Date createDate;
    @DocumentUpdateTimestamp private Date updateDate;
    private Boolean disabled;

    //生成id
/*    public static String generateId(Long userId, String lessonId) {
        return userId + ID_SEP + lessonId;
    }*/


    //缓存key
    public static String ck_userId_unitId(Long userId, String unitId) {
        return CacheKeyGenerator.generateCacheKey(AIUserLessonResultHistory.class, new String[]{"UID", "UNIT_ID"},
                new Object[]{userId, unitId});
    }



    //缓存key
    public static String ck_userId_lessonId(Long userId, String lessonId) {
        return CacheKeyGenerator.generateCacheKey(AIUserLessonResultHistory.class, new String[]{"UID", "LESSON_ID"},
                new Object[]{userId, lessonId});
    }


    public static AIUserLessonResultHistory build(String bookId, String unitId, String lessonId, LessonType lessonType, Long userId) {
        AIUserLessonResultHistory lessonResultHistory = new AIUserLessonResultHistory();
        lessonResultHistory.setUserId(userId);
        lessonResultHistory.setLessonId(lessonId);
        lessonResultHistory.setLessonType(lessonType);
        lessonResultHistory.setUnitId(unitId);
        lessonResultHistory.setBookId(bookId);
        lessonResultHistory.setFinished(true);
        lessonResultHistory.setDisabled(false);
        lessonResultHistory.setCreateDate(new Date());
        lessonResultHistory.setUpdateDate(new Date());
        return lessonResultHistory;
    }
}
