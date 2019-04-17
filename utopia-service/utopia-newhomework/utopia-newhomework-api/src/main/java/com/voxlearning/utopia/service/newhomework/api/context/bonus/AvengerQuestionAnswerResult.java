package com.voxlearning.utopia.service.newhomework.api.context.bonus;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamAnswer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AvengerQuestionAnswerResult implements Serializable {
    private static final long serialVersionUID = -8088634647911152633L;
    private String id;
    private Long studentId;                             // 学生ID
    private Integer grade;                              // 年级
    private String paperId;                             // 试卷ID
    private String questionId;                          // 试题ID

    private Boolean grasp;                              // 试题结果
    private List<List<Boolean>> subGrasp;               // 小题结果
    private List<List<String>> answer;                  // 用户答案
    private Long createdDate;                           // 开始作答时间戳
    private Long finishedDate;                          // 提交答案时间戳
    private Long duration;                              // 作业时常
    private String studyType;                           // 阿分题/作业

    public static AvengerQuestionAnswerResult newInstance(AbilityExamAnswer abilityExamAnswer) {
        AvengerQuestionAnswerResult avengerQuestionAnswerResult = new AvengerQuestionAnswerResult();
        avengerQuestionAnswerResult.setId(RandomUtils.nextObjectId());
        avengerQuestionAnswerResult.setStudyType(AbilityExamConstant.ABILITY_EXAM_STUDY_TYPE);

        avengerQuestionAnswerResult.setStudentId(abilityExamAnswer.fetchUserId());
        avengerQuestionAnswerResult.setGrade(abilityExamAnswer.getGrade());
        avengerQuestionAnswerResult.setPaperId(abilityExamAnswer.getPaperId());
        avengerQuestionAnswerResult.setQuestionId(abilityExamAnswer.getQuestionId());

        avengerQuestionAnswerResult.setAnswer(abilityExamAnswer.getQuestionDataAnswer().getAnswer());

        avengerQuestionAnswerResult.setGrasp(abilityExamAnswer.getGrasp());
        avengerQuestionAnswerResult.setSubGrasp(abilityExamAnswer.getSubGrasp());
        avengerQuestionAnswerResult.setFinishedDate(abilityExamAnswer.getCt().getTime());
        avengerQuestionAnswerResult.setCreatedDate(abilityExamAnswer.getCt().getTime() - abilityExamAnswer.getQuestionDataAnswer().getDuration());
        avengerQuestionAnswerResult.setDuration(abilityExamAnswer.getQuestionDataAnswer().getDuration());

        return avengerQuestionAnswerResult;
    }
}