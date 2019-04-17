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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用于保存应用类部分的做题结果
 *
 * @author xuesong.zhang
 * @since 2016-06-24
 */
@Getter
@Setter
public class NewHomeworkResultAppAnswer implements Serializable {

    private static final long serialVersionUID = -5077986859734844061L;

    private LinkedHashMap<String, String> answers;      // <试题id, 作业明细id(homework_process的id)>
    private Double score;                               // 本次应用的得分
    private Date finishAt;                              // 本次应用的完成时间
    private Long duration;                              // 本次应用的完成时长，（单位：毫秒）

    private Integer categoryId;                         // 练习类型（VOX_PRACTICE_TYPE中的）
    private Long practiceId;                            // 应用id（VOX_PRACTICE_TYPE中的主键）
    private String practiceName;                        // 应用名称（VOX_PRACTICE_TYPE中的practice_name）
    private String lessonId;                            // 我是一个很奇怪的属性。新课文朗读背诵必填字段

    // 绘本专用
    private String pictureBookId;                       // 阅读绘本id
    private String videoId;                             // 视频id
    private Long consumeTime;                           // 绘本、配音的总花费时长（单位：毫秒，包括阅读时间+做题时间）
    private LinkedHashMap<String, String> oralAnswers;  // 口语部分的<试题id, 作业明细id(homework_process的id)>

    private String questionBoxId;                       // 题库id 新课文朗读背诵必填字段
    private QuestionBoxType questionBoxType;            // 题库类型 新课文朗读背诵必填字段
    private Integer standardNum;                        // 朗读包或者背诵包里达标的段落个数
    private Integer appQuestionNum;                     // 朗读包或者背诵包里题目总数

    private String fileUrl;                             // 课外拓展任务1的假主观题，学生录音文件

    private String dubbingId;                           // 配音id
    private String videoUrl;                            // 配音作品url
    private Boolean skipUploadVideo;                    // 是否跳过上传视频

    private Double dubbingScore;                        // 新绘本阅读配音得分
    private AppOralScoreLevel dubbingScoreLevel;        // 新绘本阅读配音得分等级

    private String stoneId;                             //口语交际|字词讲练 使用的题包id
    private OralCommunicationContentType stoneType;     //口语交际 : 类型
    private String roleTopicId;                         //口语交际 : 人机交互（独有），用于主题角色的保存

    private LinkedHashMap<String, String> imageTextRhymeAnswers;  // 字词讲练中图文入韵的<试题id, 作业明细id(homework_process的id)
    private LinkedHashMap<String, String> chineseCourses;   // 字词讲练中汉字文化的<课程id, 作业明细id(homework_process的id)
    private Double wordExerciseScore;                   // 字词讲练：字词训练模块分数
    private Double imageTextRhymeScore;                 // 字词讲练：图文入韵模块分数

    private Map<String, Long> durations;                // 新绘本阅读各模块使用时长

    private Correction correction;                      // 批改信息

    private CorrectType correctType;

    private String teacherMark;                         // 评语

    private Boolean review;                             // 已阅true，未阅false

    private Date correctAt;                             // 批改时间

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
}
