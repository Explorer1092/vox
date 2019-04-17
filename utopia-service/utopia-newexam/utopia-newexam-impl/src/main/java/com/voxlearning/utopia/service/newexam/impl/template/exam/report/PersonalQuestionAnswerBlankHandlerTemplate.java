package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestionContext;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class PersonalQuestionAnswerBlankHandlerTemplate extends PersonalQuestionAnswerHandlerTemplate{
    @Override
    NewExamQuestionType getNewExamQuestionType() {
        return NewExamQuestionType.Blank;
    }
    @Inject
    private ClazzQuestionAnswerChoiceHandlerTemplate clazzQuestionAnswerChoiceHandlerTemplate;
    @Override
    public void processSubQuestion(NewExamPersonalQuestionContext newExamPersonalQuestionContext) {
        NewExamProcessResult p = newExamPersonalQuestionContext.getNewExamProcessResult();
        int index = newExamPersonalQuestionContext.getIndex();
        if (p.getUserAnswers() == null || p.getUserAnswers().size() <= index) {
            return;
        }
        List<Boolean> baleen = newExamPersonalQuestionContext.getBaleen();
        NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion = newExamPersonalQuestionContext.getSubQuestion();
        boolean b = newExamPersonalQuestionContext.isGrasp();
        subQuestion.setPersonalGrasp(b);
        List<String> answerList = p.getUserAnswers().get(index);
        int i = 0;
        //提空小题每个空的做题信息
        for (boolean kl : baleen) {
            if (i < baleen.size() && i < answerList.size()) {
                String s = answerList.get(i);
                if (StringUtils.isBlank(s))
                    s = "未作答";
                subQuestion.getPersonalAnswerDetail().add(MapUtils.m(
                        "answer", s,
                        "grasp", kl
                ));
            }
            i++;
        }
    }

    @Override
    public void prepareSubQuestion(NewExamPersonalPrepareQuestionContext newExamPersonalPrepareQuestionContext) {
        NewQuestionsSubContents newQuestionsSubContents = newExamPersonalPrepareQuestionContext.getNewQuestionsSubContents();
        NewExamPersonalQuestion newExamDetailToQuestion = newExamPersonalPrepareQuestionContext.getNewExamDetailToQuestion();
        NewPaperQuestion question = newExamPersonalPrepareQuestionContext.getQuestion();
        int index = newExamPersonalPrepareQuestionContext.getIndex();
        double standardScore = newExamPersonalPrepareQuestionContext.getStandardScore();
        NewExamQuestionType newExamQuestionType = newExamPersonalPrepareQuestionContext.getNewExamQuestionType();
        Subject subject = newExamPersonalPrepareQuestionContext.getSubject();

        //每个小题
        NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion = new NewExamPersonalQuestion.NewExamPersonalSubQuestion();
        subQuestion.setAnalysis(newQuestionsSubContents.getAnalysis());
        newExamDetailToQuestion.getSubQuestions().add(subQuestion);
        subQuestion.setStandardScore(standardScore);
        subQuestion.setIndex(index);
        subQuestion.setQid(question.getId());
        subQuestion.setNewExamQuestionType(newExamQuestionType);
        subQuestion.setType(newExamQuestionType.getKey());

        //填空需要标准答案，而且是多答案的支持
        List<String> answerList = newQuestionsSubContents.getAnswerList(subject);
        String standardAnswer = clazzQuestionAnswerChoiceHandlerTemplate.pressAnswer(newQuestionsSubContents, answerList);
        subQuestion.setStandardAnswer(standardAnswer);

    }
}
