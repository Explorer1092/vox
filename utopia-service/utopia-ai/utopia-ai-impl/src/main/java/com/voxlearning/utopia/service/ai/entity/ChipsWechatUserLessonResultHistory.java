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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_wechat_user_lesson_result_history")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181119")
public class ChipsWechatUserLessonResultHistory implements Serializable {

    private static final long serialVersionUID = 4364089815230239584L;
    @DocumentId
    private String id;
    private Long userId;
    private String unitId;
    private String bookId;
    private String lessonId;
    private Boolean finished;
    private Integer score;
    private Integer star;
    private LessonType lessonType;    // 课程类型  热身训练  情景对话  任务
    private String userVideo;         // 用户视频 - 目前只有情景对话有
    private String userVideoId;      // 用户视频id- 目前只有情景对话有
    private Map<String, Object> ext;

    @DocumentCreateTimestamp private Date createDate;
    @DocumentUpdateTimestamp private Date updateDate;
    private Boolean disabled;




    //缓存key
    public static String ck_userId_unitId(Long userId, String unitId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserLessonResultHistory.class, new String[]{"UID", "UNIT_ID"},
                new Object[]{userId, unitId});
    }



    //缓存key
    public static String ck_userId_lessonId(Long userId, String lessonId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserLessonResultHistory.class, new String[]{"UID", "LESSON_ID"},
                new Object[]{userId, lessonId});
    }


    public static ChipsWechatUserLessonResultHistory build(String bookId, String unitId, String lessonId, LessonType lessonType, Long userId) {
        ChipsWechatUserLessonResultHistory lessonResultHistory = new ChipsWechatUserLessonResultHistory();
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
