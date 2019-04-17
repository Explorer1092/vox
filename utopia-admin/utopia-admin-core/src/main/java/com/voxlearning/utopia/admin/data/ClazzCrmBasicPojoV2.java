package com.voxlearning.utopia.admin.data;

import com.voxlearning.utopia.service.ai.entity.ChipsClassStatistics;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author guangqing
 * @since 2018/8/22
 */
@Data
public class ClazzCrmBasicPojoV2 {
    private Integer classRankNum;//本班定级人数
    private Integer classPaidNum;//本班续费人数
    private Integer classUserNum;//本班应到学生数
    private Integer totalRankNum;//本期定级人数
    private Integer totalPaidNum;//本期续费人数
    private Integer totalUserNum;//本期应到学生数
    private String classPaidRate;
    private String totalPaidRate;

    public static ClazzCrmBasicPojoV2 valueOf(ChipsClassStatistics statistics) {
        ClazzCrmBasicPojoV2 pojo = new ClazzCrmBasicPojoV2();
        pojo.setClassRankNum(statistics.getClassRankNum());
        pojo.setClassPaidNum(statistics.getClassPaidNum());
        pojo.setClassUserNum(statistics.getClassUserNum());
        pojo.setTotalRankNum(statistics.getTotalRankNum());
        pojo.setTotalPaidNum(statistics.getTotalPaidNum());
        pojo.setTotalUserNum(statistics.getTotalUserNum());
        pojo.setClassPaidRate(formatRatePercent(statistics.getClassPaidNum(), statistics.getClassUserNum()));
        pojo.setTotalPaidRate(formatRatePercent(statistics.getTotalPaidNum(), statistics.getTotalUserNum()));
        return pojo;
    }

    public static ClazzCrmBasicPojoV2 valueOf(ChipsClassStatistics statistics, int classPaidNum, int totalPaidNum) {
        ClazzCrmBasicPojoV2 pojo = new ClazzCrmBasicPojoV2();
        pojo.setClassRankNum(statistics.getClassRankNum());
        pojo.setClassPaidNum(classPaidNum);
        pojo.setClassUserNum(statistics.getClassUserNum());
        pojo.setTotalRankNum(statistics.getTotalRankNum());
        pojo.setTotalPaidNum(totalPaidNum);
        pojo.setTotalUserNum(statistics.getTotalUserNum());
        pojo.setClassPaidRate(formatRatePercent(classPaidNum, statistics.getClassUserNum()));
        pojo.setTotalPaidRate(formatRatePercent(totalPaidNum, statistics.getTotalUserNum()));
        return pojo;
    }
    /**
     * @param numerator   分子
     * @param denominator 分母
     */
    private static String formatRatePercent(Integer numerator, Integer denominator) {
        if (numerator == null || numerator == 0 || denominator == null || denominator == 0) {
            return "0.00%";
        }
        double val = new BigDecimal(numerator * 100).divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return val + "%";
    }

}
