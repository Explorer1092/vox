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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
　* @Description: 任务对话实体
　* @author zhiqi.yao
　* @date 2018/4/12 20:28
*/

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_dialogue_task_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180925")
public class AIDialogueTaskConfig implements Serializable {
    private static final long serialVersionUID = -959062123698255711L;
    @DocumentId
    private String id;
    private String title;
    private Boolean allNpc;
    private List<Npc> npcs;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;
    private Boolean disabled;

    @Getter
    @Setter
    public static class Npc implements Serializable {
        private static final long serialVersionUID = -6008882690772954174L;
        private String npcName;
        /**
         * npc对话的状态success 或者 no
         */
        private String status;
        /**
         * 如果status equals success 为空
         * 否则 填写引导语
         */
        private String rightTip;
        private String backgroundImage;
        private String roleImage;
        private AITaskLesson begin;
        private AITaskLesson end;
        private List<Topic> topic;
    }

    @Getter
    @Setter
    public static class Topic implements Serializable {
        private static final long serialVersionUID = 3903837987251388982L;
        private AITaskLessonTopicBegin begin;
        private Help help;
        private Knowledge knowledge;
        private List<Feedback> contents;
    }

    @Getter
    @Setter
    public static class Feedback implements Serializable {
        private static final long serialVersionUID = 5353602918065455757L;
        private String pattern;
        private String jsgfText;
        private List<AITaskLessonTopicContent> feedback;
    }

    @Getter
    @Setter
    public static class Help implements Serializable {
        private static final long serialVersionUID = 5736502738168720019L;
        private String helpTitle;
        private String helpCn;
        private String helpEn;
        private String helpAudio;
        private String helpStyle;
    }

    @Getter
    @Setter
    public static class Knowledge implements Serializable {

        private static final long serialVersionUID = 6390272808639994528L;
        private String explain;
        private String explainAudio;
        private List<KnowledgeSentence> sentences;
    }

    @Getter
    @Setter
    public static class KnowledgeSentence implements Serializable {

        private static final long serialVersionUID = -8345405999781522165L;
        private String sentence;
        private String sentenceAudio;
    }

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AIDialogueTaskConfig.class, id);
    }
}
