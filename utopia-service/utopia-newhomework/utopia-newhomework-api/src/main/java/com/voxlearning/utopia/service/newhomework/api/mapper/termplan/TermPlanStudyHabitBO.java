package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2018/3/6
 */

@Setter
@Getter
public class TermPlanStudyHabitBO implements Serializable {
    private static final long serialVersionUID = 4572570874545475914L;

    private Integer unitAssignNum;
    private Boolean showCorrectFinishRate;
    private HomeworkFinishRate homeworkFinishRate;
    private CorrectFinishRate correctFinishRate;

    @Setter
    @Getter
    public static class HomeworkFinishRate implements Serializable {

        private static final long serialVersionUID = 2757303513087547799L;

        private Integer finishRate;
        private Integer cityFinishRate;
        private Integer cityTopTenFinishRate;

    }

    @Setter
    @Getter
    public static class CorrectFinishRate implements Serializable {

        private static final long serialVersionUID = 8164442742379904165L;

        private Integer correctRate;
        private Integer cityCorrectRate;
        private Integer cityTopTenCorrectRate;

    }
}
