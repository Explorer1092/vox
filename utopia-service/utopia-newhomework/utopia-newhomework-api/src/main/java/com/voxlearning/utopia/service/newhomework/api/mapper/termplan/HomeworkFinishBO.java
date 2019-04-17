package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2018/3/12
 */

@Setter
@Getter
public class HomeworkFinishBO implements Serializable {
    private static final long serialVersionUID = 5525265876017086643L;

    private Integer unitAssignNum;
    private Boolean showCorrectFinishRate;
    private HomeworkFinishRate homeworkFinishRate;
    private CorrectFinishRate correctFinishRate;

    @Setter
    @Getter
    public static class HomeworkFinishRate implements Serializable {

        private static final long serialVersionUID = 4233849197020654833L;
        private Integer finishRate;                         //作业完成率
        private Integer cityFinishRate;                     // 全市平均作业完成率
        private Integer cityTopTenPercentFinishRate;        // 全市top10%作业完成率
    }

    @Setter
    @Getter
    public static class CorrectFinishRate implements Serializable {

        private static final long serialVersionUID = -1196450578052756115L;
        private Integer correctRate;                        //订正完成率
        private Integer cityCorrectRate;                    // 全市平均订正完成率
        private Integer cityTopTenPercentCorrectRate;       // 全市top10%订正完成率
    }
}
