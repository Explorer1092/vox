package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Comparator;

@Getter
@Setter
public class MentalArithmeticStudent implements Serializable {
    private static final long serialVersionUID = 6110481203043388790L;
    private Long userId;
    private String userName;
    private int score;
    private int duration;
    private String durationStr;
    private boolean repair;
    private boolean finished;
    public static Comparator<MentalArithmeticStudent> comparator = (Comparator<MentalArithmeticStudent>) (o1, o2) -> {
        int compare = Boolean.compare(o2.isFinished(), o1.isFinished());
        if (compare != 0) {
            return compare;
        }
        compare = Integer.compare(o2.getScore(), o1.getScore());
        if (compare != 0) {
            return compare;
        }
        return Integer.compare(o1.getDuration(), o2.getDuration());
    };
}
