package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class PersonalQuestionAnswerHandlerFactory extends SpringContainerSupport {
    private Map<NewExamQuestionType, PersonalQuestionAnswerHandlerTemplate> templateMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Map<String, PersonalQuestionAnswerHandlerTemplate> beans = applicationContext.getBeansOfType(PersonalQuestionAnswerHandlerTemplate.class);
        for (PersonalQuestionAnswerHandlerTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewExamQuestionType(), bean);
        }
    }


    public PersonalQuestionAnswerHandlerTemplate getTemplate(NewExamQuestionType newExamQuestionType) {
        if (newExamQuestionType == null) {
            return null;
        }
        if (!this.templateMap.containsKey(newExamQuestionType)) {
            return null;
        }
        return this.templateMap.get(newExamQuestionType);
    }
}
