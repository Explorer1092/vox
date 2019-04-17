package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 新绘本学生配音
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "picture_book_plus_dubbing")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180303")
public class PictureBookPlusDubbing implements Serializable {
    private static final long serialVersionUID = 3318541896877458901L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                                      // 格式为 作业id + "__" + 绘本id "__" + 学生id;
    private List<Content> contents;                         // 绘本内容信息(整体结构同PictureBookPlus.contents，会多一些字段)
    private String screenMode;                              // 横竖屏
    private Double score;                                   // 配音得分
    private AppOralScoreLevel scoreLevel;                   // 配音得分等级

    @DocumentCreateTimestamp
    private Date createAt;                                  // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                  // 更新时间

    @Data
    public static class Content implements Serializable {

        private static final long serialVersionUID = -2290860679337044082L;

        private Integer rank;
        private String pictureUrl;
        private String pictureThumbnailUrl;
        private List<InteractArea> interactAreas;  // 热区
        private List<ContentParagraph> paragraphs; // 段落
    }

    /**
     * 热区
     */
    @Data
    public static class InteractArea implements Serializable {

        private static final long serialVersionUID = 7513276840302008351L;

        private Float posX;
        private Float posY;
        private Float radius;
        private String word;
        private String wordAudioUk;
        private String wordAudioUs;
    }

    /**
     * 内容段落
     */
    @Data
    public static class ContentParagraph implements Serializable {

        private static final long serialVersionUID = -4992152664153368318L;

        private Integer rank;
        List<ParagraphCamera> cameras;
        List<ParagraphSentence> sentences;
        ParagraphCoordinate coordinate;
    }

    /**
     * 镜头信息
     */
    @Data
    public static class ParagraphCamera implements Serializable {

        private static final long serialVersionUID = -936593402303062978L;

        private Integer rank;
        private Float posX;
        private Float posY;
        private Float scale;
        private Integer milliseconds;
    }

    /**
     * 句子信息
     */
    @Data
    public static class ParagraphSentence implements Serializable {

        private static final long serialVersionUID = 1406466857012441530L;

        private Integer rank;
        private String entext;
        private String cntext;
        private String voiceText;
        private String audioUrl;
        private String userAudioUrl;    // 音频地址
        private Integer macScore;       // 引擎分
        private Integer fluency;        // 流利程度
        private Integer integrity;      // 完整度
        private Integer pronunciation;  // 发音准确度
        private Integer oralScore;      // 转换后的得分，用它来计算平均分
        List<SentenceWord> words;
    }

    /**
     * 单词信息
     */
    @Data
    public static class SentenceWord implements Serializable {

        private static final long serialVersionUID = 6219531533237265320L;

        private String entext;
        private String cntext;
        private String audioUrlUk;
        private String audioUrlUs;
        private Boolean oftenUsed;
        private String imageUrl;
        private String soundmarkText;
        private String soundmarkAudio;
    }

    /**
     * 坐标信息
     */
    @Data
    public static class ParagraphCoordinate implements Serializable {

        private static final long serialVersionUID = -526122206028817820L;

        private Float posX;
        private Float posY;
        private Float width;
        private Float height;
        private Integer fontSize;
        private Boolean showNewline;
    }


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(PictureBookPlusDubbing.class, id);
    }

    @Data
    public static class ID implements Serializable {

        private static final long serialVersionUID = 700229482062115309L;

        private String homeworkId;          // 作业id
        private String pictureBookId;       // 绘本id
        private Long userId;                // 学生id

        public ID(String homeworkId, String pictureBookId, Long userId) {
            this.homeworkId = homeworkId;
            this.pictureBookId = pictureBookId;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return homeworkId + "__" + pictureBookId + "__" + userId;
        }
    }

    public PictureBookPlusDubbing.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = id.split("__");
        if (segments.length != 3) return null;
        String homeworkId = segments[0];
        String pictureBookId = segments[1];
        Long userId = SafeConverter.toLong(segments[2]);
        return new PictureBookPlusDubbing.ID(homeworkId, pictureBookId, userId);
    }
}
