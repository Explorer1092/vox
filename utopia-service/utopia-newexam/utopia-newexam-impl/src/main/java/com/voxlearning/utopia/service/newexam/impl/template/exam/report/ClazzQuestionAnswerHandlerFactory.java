package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class ClazzQuestionAnswerHandlerFactory extends SpringContainerSupport {
    private Map<NewExamQuestionType, ClazzQuestionAnswerHandlerTemplate> templateMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Map<String, ClazzQuestionAnswerHandlerTemplate> beans = applicationContext.getBeansOfType(ClazzQuestionAnswerHandlerTemplate.class);
        for (ClazzQuestionAnswerHandlerTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewExamQuestionType(), bean);
        }
    }


    public ClazzQuestionAnswerHandlerTemplate getTemplate(NewExamQuestionType newExamQuestionType) {
        if (newExamQuestionType == null) {
            return null;
        }
        if (!this.templateMap.containsKey(newExamQuestionType)) {
            return null;
        }
        return this.templateMap.get(newExamQuestionType);
    }

}
