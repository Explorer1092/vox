package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Getter
@Setter
@ToString
public class NewHomeworkResultAnswer implements Serializable {

    private static final long serialVersionUID = -4839888528067913918L;

    private LinkedHashMap<String, String> answers;          // <试题id, 作业明细id(homework_process的id)>
    private Double score;                                   // 本次作业形式的最终得分
    private Date finishAt;                                  // 作业形式的完成时间
    private Date correctedAt;                               // 批改完成时间，当作业中包含需要主观批改的作业（NewHomework.includeSubjective），并且已经批改完成。
    private Long duration;                                  // 本次作业形式的完成时长，（单位：毫秒）

    // 用于应用类和绘本类做题结果存储
    // app类 <{categoryId-lessonId}, NewHomeworkResultAppAnswer>
    // 绘本类 <{readingId}, NewHomeworkResultAppAnswer>
    // 重难点视频 <{videoId}, NewHomeworkResultAppAnswer>
    // 课文读背 <{QuestionBoxId}, NewHomeworkResultAppAnswer>
    // 趣味配音 <{DubbingId}, NewHomeworkResultAppAnswer>
    // 口语交际 <{stoneDataId}, NewHomeworkResultAppAnswer>
    // 字词讲练 <{stoneDataId}, NewHomeworkResultAppAnswer>
    public LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers;

    // 纸质口算答题详情，没有题，无法复用answers结构
    public List<String> ocrMentalAnswers;
    public Integer ocrMentalQuestionCount;          //  纸质口算识别出来的题目总数量
    public Integer ocrMentalCorrectQuestionCount;   //  纸质口算识别正确的题目总数量

    //英语纸质听写答题详情
    public List<String> ocrDictationAnswers;
    public Integer ocrDictationQuestionCount;          //  识别出来的题目总数量
    public Integer ocrDictationCorrectQuestionCount;   //  识别正确的题目总数量

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    @JsonIgnore
    public boolean isCorrected() {
        return correctedAt != null;
    }

    /**
     * @return 返回 newQuestionId newHomeworkProcessId Map
     */
    public LinkedHashMap<String, String> processAnswers() {

        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        if (this.answers != null) {
            answers.putAll(this.answers);
        }
        if (appAnswers != null) {
            appAnswers.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> {
                        LinkedHashMap<String, String> map = new LinkedHashMap<>();
                        if (o.getAnswers() != null) {
                            map.putAll(o.getAnswers());
                        }
                        if (o.getOralAnswers() != null) {
                            map.putAll(o.getOralAnswers());
                        }
                        if (o.getImageTextRhymeAnswers() != null) {
                            map.putAll(o.getImageTextRhymeAnswers());
                        }
                        return map;
                    })
                    .forEach(answers::putAll);
        }
        return answers;
    }

    /**
     * 字词讲练专用：不同题包下存在相同的题目
     * 获取该作业形式下processResultIds
     * @return
     */
    public LinkedList<String> processWordTeachAnswers() {
        LinkedList<String> processIds = new LinkedList<>();
        if (appAnswers != null) {
            appAnswers.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> {
                        LinkedList<String> processId = new LinkedList<>();
                        if (o.getAnswers() != null) {
                            processId.addAll(o.getAnswers().values());
                        }
                        if (o.getImageTextRhymeAnswers() != null) {
                            processId.addAll(o.getImageTextRhymeAnswers().values());
                        }
                        return processId;
                    })
                    .forEach(processIds::addAll);
        }
        return processIds;
    }

    public Integer processScore(ObjectiveConfigType type) {
        if (score == null) {
            return null;
        } else {
            int _score = new BigDecimal(getScore()).setScale(0, BigDecimal.ROUND_DOWN).intValue();
            // 口算训练在20190105之前计算的分数都有问题，需要使用四舍五入来计算
            if (ObjectiveConfigType.MENTAL_ARITHMETIC == type && finishAt != null && finishAt.before(NewHomeworkConstants.MENTAL_ARITHMETIC_ROUND_DOWN_SCORE_START_DATE)) {
                _score = new BigDecimal(getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            }
            // 同步习题在20190108之前计算的分数都有问题，需要使用四舍五入来计算
            if ((ObjectiveConfigType.EXAM == type || ObjectiveConfigType.INTELLIGENCE_EXAM == type) && finishAt != null && finishAt.before(NewHomeworkConstants.EXAM_ROUND_DOWN_SCORE_START_DATE)) {
                _score = new BigDecimal(getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            }
            // 新绘本阅读在20190114之前计算的分数都有问题，需要使用四舍五入来计算
            if (ObjectiveConfigType.LEVEL_READINGS == type && finishAt != null && finishAt.before(NewHomeworkConstants.LEVEL_READINGS_ROUND_DOWN_SCORE_START_DATE)) {
                _score = new BigDecimal(getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            }
            if (_score > 100) {
                _score = 100;
            }
            return _score;
        }
    }

    public Integer processDuration() {
        if (processConsumeTime() == null || processConsumeTime() == 0) {
            return getDuration() == null ? null : new BigDecimal(getDuration()).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).intValue();
        } else {
            return processConsumeTime();
        }
    }

    /**
     * 这个方法仅用于计算绘本的总耗时（阅读时间+Oral题目时间+应试题目时间）
     *
     * @return 计算后时间（单位：秒）
     */
    private Integer processConsumeTime() {
        Integer consumeTime = 0;
        if (MapUtils.isNotEmpty(appAnswers)) {
            Long consumeTimeLong = appAnswers
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(o -> o.processDuration() != null)
                    .mapToLong(NewHomeworkResultAppAnswer::processDuration)
                    .sum();
            consumeTime = new BigDecimal(consumeTimeLong).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).intValue();
        }
        return consumeTime;
    }
}
