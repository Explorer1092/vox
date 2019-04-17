package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BasicReviewHomeworkPersonalMathReport implements Serializable {
    private static final long serialVersionUID = 3585407247487122539L;
    private String stageName;
    private Subject subject;
    private CalculationPart calculationPart;

    @Getter
    @Setter
    public static class CalculationPart implements Serializable {
        private static final long serialVersionUID = 5060577574182524693L;

        private int totalQuestionNum;
        private int personalWrongNum;
        private int bestWrongNum;

    }

}
