package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Getter
@Setter
public class BaseHomeworkResultAppAnswer implements Serializable {

    private static final long serialVersionUID = -2789146691760531584L;

    public Double score;                               // 本次应用的得分
    public Date finishAt;                              // 本次应用的完成时间
    public Long duration;                              // 本次应用的完成时长，（单位：毫秒）

    public Integer categoryId;                         // 练习类型（VOX_PRACTICE_TYPE中的）
    public Long practiceId;                            // 应用id（VOX_PRACTICE_TYPE中的主键）
    public String practiceName;                        // 应用名称（VOX_PRACTICE_TYPE中的practice_name）
    public String lessonId;                            // 我是一个很奇怪的属性。新课文朗读背诵必填字段
    public String courseId;                            // 巩固课程ID

    // 绘本专用
    public String pictureBookId;                       // 阅读绘本id
    public String videoId;                             // 视频id
    public Long consumeTime;                           // 绘本的总花费时长（单位：毫秒，包括阅读时间+做题时间）

    public String questionBoxId;                       // 题库id 新课文朗读背诵必填字段
    public QuestionBoxType questionBoxType;            // 题库类型 新课文朗读背诵必填字段
    public Integer standardNum;                        // 朗读包或者背诵包里达标的段落个数
    public Integer appQuestionNum;                     // 朗读包或者背诵包里题目总数

    private String stoneId;                            // 字词讲练
    private Double wordExerciseScore;                  // 字词讲练：字词训练模块分数
    private Double imageTextRhymeScore;                // 字词讲练：图文入韵模块分数

    private String dubbingId;                          // 配音id（新绘本阅读也用到了这个字段，表示配音作品id）
    private String videoUrl;                           // 配音作品url
    private Boolean skipUploadVideo;                    // 是否跳过上传视频

    private OralCommunicationContentType stoneType;     //口语交际 : 类型
    private String roleTopicId;                         //口语交际 : 人机交互（独有），用于主题角色的保存

    private Double dubbingScore;                       // 新绘本阅读配音得分
    private AppOralScoreLevel dubbingScoreLevel;       // 新绘本阅读配音得分等级

    private Map<String, Long> durations;               // 新绘本阅读各模块使用时长

    public Correction correction;                      // 批改信息

    public CorrectType correctType;

    public String teacherMark;                         // 评语

    public Boolean review;                             // 已阅true，未阅false

    public Date correctAt;                             // 批改时间


    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    @JsonIgnore
    public boolean isCorrected() {
        return correctAt != null;
    }

    public Long processDuration() {
        if (consumeTime != null && consumeTime > 0 && consumeTime < 3600000) {
            return consumeTime;
        } else {
            return duration != null ? duration : 0;
        }
    }

    @JsonIgnore
    public boolean isGrasp() {
        return score != null && score == 100d;
    }
}
