package com.voxlearning.utopia.service.newhomework.impl.strategy.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/7/26
 */
@Named
public class DiagnoseReportFactory {

    @Inject private EnglishOralInterventionsReportImpl englishOralInterventionsReport;
    @Inject private EnglishDiagnosticInterventionsReportImpl englishDiagnosticInterventionsReport;
    @Inject private MathDiagnosticInterventionsReportImpl mathDiagnosticInterventionsReport;

    /**
     * 根据学科和作业形式获取策略
     * @param subject
     * @param configType
     * @return
     */
    public DiagnoseReportStrategy getDiagnoseReportStrategy(Subject subject, ObjectiveConfigType configType) {
        if (subject.equals(Subject.ENGLISH) && configType.equals(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING)) {
            return englishOralInterventionsReport;
        } else if (subject.equals(Subject.ENGLISH) && configType.equals(ObjectiveConfigType.INTELLIGENT_TEACHING)) {
            return englishDiagnosticInterventionsReport;
        } else {
            return mathDiagnosticInterventionsReport;
        }
    }

}
