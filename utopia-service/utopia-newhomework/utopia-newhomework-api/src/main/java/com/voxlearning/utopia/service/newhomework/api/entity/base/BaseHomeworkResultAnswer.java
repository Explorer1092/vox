package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 新结构，这个和以前的NewHomeworkResultAnswer的区别在于没有answers
 *
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Getter
@Setter
public class BaseHomeworkResultAnswer implements Serializable {

    private static final long serialVersionUID = 5345075769606506446L;

    private Double score;           // 本次作业形式的最终得分
    private Date finishAt;          // 作业形式的完成时间
    private Date correctedAt;       // 批改完成时间，当作业中包含需要主观批改的作业（NewHomework.includeSubjective），并且已经批改完成。
    private Long duration;          // 本次作业形式的完成时长，（单位：毫秒）

    // 用于应用类和绘本类做题结果存储
    // app类 <{categoryId-lessonId}, BaseHomeworkResultAppAnswer>
    // 绘本类 <{readingId}, BaseHomeworkResultAppAnswer>
    // 重难点视频 <{video}, BaseHomeworkResultAppAnswer>
    // 新朗读背诵 <{questionBoxId}, BaseHomeworkResultAppAnswer>
    // 巩固课程 <{courseId}, BaseHomeworkResultAppAnswer>
    private LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswers;

    // 纸质口算答题详情，没有题，无法复用answers结构
    public List<String> ocrMentalAnswers;
    public Integer ocrMentalQuestionCount;          //  纸质口算识别出来的题目总数量
    public Integer ocrMentalCorrectQuestionCount;   //  纸质口算识别正确的题目总数量

    public List<String> ocrDictationAnswers;
    public Integer ocrDictationQuestionCount;          //  英语纸质听写识别出来的题目总数量
    public Integer ocrDictationCorrectQuestionCount;   //  英语纸质听写识别正确的题目总数量

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    @JsonIgnore
    public boolean isCorrected() {
        return correctedAt != null;
    }

    public Integer processScore() {
        return getScore() == null ? null : new BigDecimal(getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }
}
