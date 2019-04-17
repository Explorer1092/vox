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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.constant.AiUserVideoLevel;
import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户视频：分享，审核
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_user_video")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190401")
public class AIUserVideo implements Serializable {
    private static final long serialVersionUID = 0L;

    @DocumentId
    private String id;
    private Long userId;
    private String userName;
    @Deprecated
    private Long clazz;
    private String bookId;
    private String unitId;
    private String lessonId;
    private String lessonName;
    private ExamineStatus status;
    private String examiner; //审核人
    private Category category;
    private List<Label> labels;
    private String comment;
    private String commentAudio;
    private String video;  //合成后的视频
    private List<String> originalVideos;//要合成的视频
    private Map<String, Object> ext;
    private String description;
    private Boolean disabled;
    private AiUserVideoLevel remarkLevel;//一对一视频 对应的视频level
    private Boolean forRemark;//true 代表是筛选出来的一对一视频
    private Integer remarkLessonScore;//筛选出的一对一视频的lesson分数
    private Boolean forShare;//视频分享,是否是已分享视频
    private String remarkQRCid;//一对一视频点评对应的vox_ai_user_question_result_collection  id

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    public static AIUserVideo newInstance(Long userId, String userName, String bookId, String unitId, String lessonName) {
        AIUserVideo aiUserVideo = new AIUserVideo();
        aiUserVideo.setUnitId(unitId);
        aiUserVideo.setUserId(userId);
        aiUserVideo.setUserName(userName);
        aiUserVideo.setBookId(bookId);
        aiUserVideo.setLessonName(lessonName);
        aiUserVideo.setStatus(ExamineStatus.Waiting);
        aiUserVideo.setCreateTime(new Date());
        aiUserVideo.setUpdateTime(new Date());
        aiUserVideo.setDisabled(false);
        return aiUserVideo;
    }
    public static AIUserVideo newInstance(Long userId, String userName, String lessonId, String lessonName) {
        AIUserVideo aiUserVideo = new AIUserVideo();
        aiUserVideo.setLessonId(lessonId);
        aiUserVideo.setUserId(userId);
        aiUserVideo.setUserName(userName);
        aiUserVideo.setLessonName(lessonName);
        aiUserVideo.setStatus(ExamineStatus.Waiting);
        aiUserVideo.setCreateTime(new Date());
        aiUserVideo.setUpdateTime(new Date());
        aiUserVideo.setDisabled(false);
        return aiUserVideo;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Label {
        Pronunciation("发音好"), Express("表达好"), Fluency("流利"), ActionMatch("表演好"), UsingTools("使用工具"),
        Characteristic("有特色"), Expressiveness("表情好"), OldFriend("老朋友"), NewFriend("新朋友"), Other("其他"), PracticeMore("练习多"),
        Word("单词发音"), Whole("整体语音");
        @Getter
        private final String description;

        public static Label safeOf(String name) {
            try {
                return valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ExamineStatus {
        Waiting("待审核"), Examining("审核中"), Passed("通过"), Failed("违规驳回");
        @Getter
        private final String description;

        public static ExamineStatus safeOf(String name) {
            try {
                return valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Category {
        Common("普通视频"), Super("推荐视频"), Bad("劣质视频");
        @Getter
        private final String description;
        public static Category safeOf(String name) {
            try {
                return valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public List<String> fetchUserVideos() {
        return CollectionUtils.isNotEmpty(this.getOriginalVideos()) ?
                this.getOriginalVideos().stream().filter(e -> e.contains("https://17zy-homework.oss-cn-beijing.aliyuncs.com")).collect(Collectors.toList()) : Collections.emptyList();
    }
    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AIUserVideo.class, id);
    }

    //缓存key
    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AIUserVideo.class, "UID", userId);
    }

    //缓存key
    public static String ck_unit_id(String unitId) {
        return CacheKeyGenerator.generateCacheKey(AIUserVideo.class, "UNID", unitId);
    }

    public static String ck_unit_status(String unitId, ExamineStatus status) {
        return CacheKeyGenerator.generateCacheKey(AIUserVideo.class,
                new String[]{"UNID", "E"},
                new Object[]{unitId, status});
    }
}
