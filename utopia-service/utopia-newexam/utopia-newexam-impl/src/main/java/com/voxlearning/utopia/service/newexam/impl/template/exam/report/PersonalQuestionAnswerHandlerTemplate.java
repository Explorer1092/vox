package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestionContext;

abstract public class PersonalQuestionAnswerHandlerTemplate {
    abstract NewExamQuestionType getNewExamQuestionType();

    abstract public void processSubQuestion(NewExamPersonalQuestionContext newExamPersonalQuestionContext);

    abstract public void prepareSubQuestion(NewExamPersonalPrepareQuestionContext newExamPersonalPrepareQuestionContext);

}
