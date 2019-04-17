package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Comparator;

@Getter
@Setter
public class OcrMentalArithmeticStudent implements Serializable {
    private static final long serialVersionUID = 1982752960317643093L;

    private Long userId;
    private String userName;
    private int score;
    private int identifyCount;
    private int errorCount;
    private boolean manualCorrect;
    private boolean finished;
    private String resultUrl;
    public static Comparator<OcrMentalArithmeticStudent> comparator = (Comparator<OcrMentalArithmeticStudent>) (o1, o2) -> {
        int compare = Boolean.compare(o2.isFinished(), o1.isFinished());
        if (compare != 0) {
            return compare;
        }
        return Integer.compare(o2.getScore(), o1.getScore());
    };
}
