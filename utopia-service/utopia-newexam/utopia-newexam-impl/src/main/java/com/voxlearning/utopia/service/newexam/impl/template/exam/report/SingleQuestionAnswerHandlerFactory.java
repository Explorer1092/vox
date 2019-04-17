package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
public class SingleQuestionAnswerHandlerFactory extends SpringContainerSupport {
    private Map<ExamReportAnswerStatType, SingleQuestionAnswerHandlerTemplate> templateMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Map<String, SingleQuestionAnswerHandlerTemplate> beans = applicationContext.getBeansOfType(SingleQuestionAnswerHandlerTemplate.class);
        for (SingleQuestionAnswerHandlerTemplate bean : beans.values()) {
            this.templateMap.put(bean.getExamReportAnswerStatType(), bean);
        }
    }


    public SingleQuestionAnswerHandlerTemplate getTemplate(ExamReportAnswerStatType examReportAnswerStatType) {
        if (examReportAnswerStatType == null) {
            return null;
        }
        if (!this.templateMap.containsKey(examReportAnswerStatType)) {
            return null;
        }
        return this.templateMap.get(examReportAnswerStatType);
    }
}
