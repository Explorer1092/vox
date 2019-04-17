package com.voxlearning.utopia.service.newexam.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import java.text.DecimalFormat;

public class NewExamPaperHelper {
    public static String fetchPaperId(NewExamRegistration newExamRegistration, NewExam newExam, Long studentId) {
        if (newExamRegistration != null && StringUtils.isNotBlank(newExamRegistration.getPaperId())) {
            return newExamRegistration.getPaperId();
        } else {
            return newExam.fetchPaperId(studentId);
        }
    }

    public static String fetchPaperId(NewExamResult newExamResult, NewExam newExam, Long studentId) {
        if (newExamResult != null && StringUtils.isNotBlank(newExamResult.getPaperId())) {
            return newExamResult.getPaperId();
        } else {
            return newExam.fetchPaperId(studentId);
        }
    }

    public static String getClassOrderPaperIdKey(Long clazzId, String newExamId) {
        return "NEW_EXAM_CLASS_ORDER_TYPE_" + clazzId + "_" + newExamId;
    }



    public static double simple(double data) {
        DecimalFormat df = new DecimalFormat("###.##");
        return SafeConverter.toDouble(df.format(data));
    }
}
