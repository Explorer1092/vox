package com.voxlearning.utopia.service.newhomework.api.context.bonus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 这个属性参见题库的QuestionScoreResult
 *
 * @author lei.liu
 * @version 18-11-1
 */
@Setter
@Getter
@NoArgsConstructor
public class QuestionResult implements Serializable {

    private static final long serialVersionUID = 5709611078158696924L;
    // 试题ID
    private String questionId;
    // 试题得分,小数点后保留2位（跟读类应用需要根据引擎分数对应出处理后的分数）
    // totalScore这个属性并没有什么用处，酌情可以删掉
    private Double totalScore;
    // 小题是否全部正确,例如：填空题只要有一个空答错，此属性就是false
    private Boolean isRight;

    private List<List<Boolean>> subRight;

    public QuestionResult(String questionId, Double totalScore, Boolean isRight, List<List<Boolean>> subRight) {
        this.questionId = questionId;
        this.totalScore = totalScore;
        this.isRight = isRight;
        this.subRight = subRight;
    }
}
