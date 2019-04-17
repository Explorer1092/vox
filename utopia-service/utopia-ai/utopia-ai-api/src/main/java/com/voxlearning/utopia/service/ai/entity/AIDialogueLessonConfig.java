package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_dialogue_lesson_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181030")
public class AIDialogueLessonConfig implements Serializable {
    private static final long serialVersionUID = 6360502172352329795L;
    @DocumentId
    private String id;
    private String title;
    private AIDialogueLesson begin;
    private List<Topic> topic;
    private AIDialogueLesson end;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;
    private Boolean disabled;

    @Getter
    @Setter
    public static class Topic implements Serializable {
        private static final long serialVersionUID = -6008892275770282001L;
        private AIDialogueLessonTopicBegin begin;
        private Help help;
        private Knowledge knowledge;
        private List<Feedback> contents;
    }

    @Getter
    @Setter
    public static class Feedback implements Serializable {
        private static final long serialVersionUID = 3944578106008417079L;
        private String pattern;
        private String jsgfText;
        private List<AIDialogueLessonTopicContent> feedback;
    }

    @Getter
    @Setter
    public static class Help implements Serializable {
        private static final long serialVersionUID = -4936139641116207277L;
        private String helpTitle;
        private String helpCn;
        private String helpEn;
        private String helpAudio;
        private String helpStyle;
    }

    @Getter
    @Setter
    public static class Knowledge implements Serializable {
        private static final long serialVersionUID = 0L;
        private String explain;
        private String explainAudio;
        private List<KnowledgeSentence> sentences;
    }

    @Getter
    @Setter
    public static class KnowledgeSentence implements Serializable {
        private static final long serialVersionUID = 0L;
        private String sentence;
        private String sentenceAudio;

    }

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AIDialogueLessonConfig.class, id);
    }
}
