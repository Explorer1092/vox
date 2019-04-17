package com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class BaseVoiceRecommend implements Serializable {
    private static final long serialVersionUID = -148994586144425996L;


    private String homeworkId;                          // 作业id
    private Long teacherId;                             // 老师id
    private Date homeworkCreateTime;                    // 作业创建时间
    private Long groupId;                               // 班组id
    private List<RequestParent> requestParentList;      // 请求推荐家长列表
    private List<RecommendVoice> recommendVoiceList;    // 推荐音频列表
    private List<ReadReciteVoice> readReciteVoices;     // 课文读背推荐音频
    private List<DubbingWithScore> excellentDubbingStu; // 优秀配音推荐
    private List<ImageText> imageTextList;              // 图文入韵推荐
    private String recommendComment;                    // 推荐评语


    @Getter
    @Setter
    public static class RequestParent implements Serializable {
        private static final long serialVersionUID = 4402370219206975023L;

        private Long parentId;          // 家长id
        private String parentName;      // 家长姓名
        private Date requestTime;       // 请求时间
    }

    @Getter
    @Setter
    public static class RecommendVoice implements Serializable {
        private static final long serialVersionUID = 1687158527395629316L;

        private Long studentId;         // 学生id
        private String studentName;     // 学生姓名
        private String categoryName;    // 应用类型
        private List<String> voiceList; // 音频列表
    }

    @Getter
    @Setter
    public static class ReadReciteVoice implements Serializable {
        private static final long serialVersionUID = 4146026358102047633L;

        private Long studentId;         // 学生id
        private String studentName;     // 学生姓名
        private String lessonName;      // 课文名称
        private String paragraph;       // 第n段
        private QuestionBoxType type;   // 朗读或者背诵
        private String voice;           // 推荐语音
    }

    @Getter
    @Setter
    public static class DubbingWithScore implements Serializable {
        private static final long serialVersionUID = -6472551868868525186L;

        private String videoName;       //音频名称
        private String dubbingId;       //音频id
        private Long userId;         // 学生id
        private String userName;     // 学生姓名
        private int score;           //分数
        private long duration;       //时长
        private String studentVideoUrl;//音频
        private String coverUrl; //封面
    }

    @Getter
    @Setter
    public static class ImageText implements Serializable {
        private static final long serialVersionUID = -2650373087948098174L;

        private Long studentId;         // 学生id
        private String studentName;     // 学生姓名
        private String stoneId;         // 石头堆id
        private String chapterId;       // 篇章id
        private String coverImageUrl;   // 作品封面
        private Integer star;           // 作品星级
        private Integer score;          // 作品分数
        private String flashvarsUrl;    // 预览地址
    }

    public boolean hasRecommend() {
        return recommendVoiceList != null && !recommendVoiceList.isEmpty();
    }

    public boolean hasRecommendDubbing() {
        return excellentDubbingStu != null && !excellentDubbingStu.isEmpty();
    }
}
