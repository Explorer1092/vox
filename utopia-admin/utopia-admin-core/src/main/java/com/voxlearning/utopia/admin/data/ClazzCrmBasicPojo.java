package com.voxlearning.utopia.admin.data;

import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassStatistics;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassStatisticsLatest;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author guangqing
 * @since 2018/8/22
 */
@Data
public class ClazzCrmBasicPojo {
    //课次 第几节课
    private Integer lesson;
    //单元id
    private String unitId;
    //本班到课人数
    private Integer attendance_c;
    //本班完课人数
    private Integer complete_c;
    //本班到课率 = 本班到课人数/本班用户数
    private String attendance_ratio_c;
    //本班净完课率 = 本班完课人数/本班到课人数 改成 本班完课人数/本班应到用户数
    private String complete_ratio_c;
    //本期到课人数
    private Integer attendance_a;
    //本期完课人数
    private Integer complete_a;
    //本期到课率 = 本期到课人数/本产品下的用户数
    private String attendance_ratio_a;
    //本期净完课率 = 本期完课人数/本期到课人数  改成 本期完课人数/本期应到用户数
    private String complete_ratio_a;
    //单元名
    private String unitName;
    //本班主动服务人数
    private Integer active_c;
    //本期主动服务人数
    private Integer active_a;
    //本班主动服务率 = 本班主动服务人数/本班用户数
    private String active_ratio_c;
    //本期主动服务率 = 本期主动服务人数/本产品下的用户数
    private String active_ratio_a;
    //本班完课点评量
    private Integer classRemarkNum;
    //本班完课点评率，完课点评量／（本班完课量－本班未加微信学生数）＊100%
    private String classRemarkRate;
    //本期完课点评量
    private Integer periodRemarkNum;
    //本期完课点评率，本期完课点评量／（本期完课量－本期未加微信学生数）＊100%
    private String periodRemarkRate;
    //本班应到用户数
    private Integer clazzNum;
    //本期应到用户数
    private Integer totalNum;

    public static ClazzCrmBasicPojo valueOf(int lessonNum, String unitId, String unitName,
                                            ChipsEnglishClassStatisticsLatest clazzStatisticsLatest) {
        ClazzCrmBasicPojo pojo = new ClazzCrmBasicPojo();
        pojo.setLesson(lessonNum);
        pojo.setUnitId(unitId);
        pojo.setUnitName(unitName);
        Integer attendC = clazzStatisticsLatest.getClassAttendNum();
        pojo.setAttendance_c(attendC == null ? 0 : attendC);
        Integer finishC = clazzStatisticsLatest.getClassFinishNum();
        pojo.setComplete_c(finishC == null ? 0 : finishC);
        Integer attendA = clazzStatisticsLatest.getTotalAttendNum();
        pojo.setAttendance_a(attendA == null ? 0 : attendA);
        Integer finishA = clazzStatisticsLatest.getTotalFinishNum();
        pojo.setComplete_a(finishA == null ? 0 : finishA);
        pojo.setAttendance_ratio_c(formatRatePercent(attendC, clazzStatisticsLatest.getClassNum()));
        pojo.setComplete_ratio_c(formatRatePercent(finishC, clazzStatisticsLatest.getClassNum()));
        pojo.setAttendance_ratio_a(formatRatePercent(attendA, clazzStatisticsLatest.getTotalNum()));
        pojo.setComplete_ratio_a(formatRatePercent(finishA, clazzStatisticsLatest.getTotalNum()));
        Integer classActiveNum = clazzStatisticsLatest.getClassActiveNum();
        Integer totalActiveNum = clazzStatisticsLatest.getTotalActiveNum();
        pojo.setActive_c(classActiveNum == null ? 0 : classActiveNum);
        pojo.setActive_a(totalActiveNum == null ? 0 : totalActiveNum);
        pojo.setActive_ratio_c(formatRatePercent(classActiveNum, clazzStatisticsLatest.getClassNum()));
        pojo.setActive_ratio_a(formatRatePercent(totalActiveNum, clazzStatisticsLatest.getTotalNum()));
        pojo.setClassRemarkNum(clazzStatisticsLatest.getClassRemarkNum());
        pojo.setPeriodRemarkNum(clazzStatisticsLatest.getPeriodRemarkNum());
        pojo.setClassRemarkRate(formatRatePercent(clazzStatisticsLatest.getClassRemarkNum(), (finishC - (clazzStatisticsLatest.getClassNotwxNum() == null ? 0 : clazzStatisticsLatest.getClassNotwxNum()))));
        pojo.setPeriodRemarkRate(formatRatePercent(clazzStatisticsLatest.getPeriodRemarkNum(), (finishA - (clazzStatisticsLatest.getPeriodNotwxNum() == null ? 0 : clazzStatisticsLatest.getPeriodNotwxNum()))));
        pojo.setClazzNum(clazzStatisticsLatest.getClassNum());
        pojo.setTotalNum(clazzStatisticsLatest.getTotalNum());
        return pojo;
    }


    public static ClazzCrmBasicPojo valueOf(int lessonNum, String unitId, String unitName,
                                            ChipsEnglishClassStatistics clazzStatistics) {
        ClazzCrmBasicPojo pojo = new ClazzCrmBasicPojo();
        pojo.setUnitId(unitId);
        pojo.setLesson(lessonNum);
        pojo.setUnitName(unitName);
        Integer attendC = clazzStatistics.getClassAttendNum();
        pojo.setAttendance_c(attendC == null ? 0 : attendC);
        Integer finishC = clazzStatistics.getClassFinishNum();
        pojo.setComplete_c(finishC == null ? 0 : finishC);
        Integer attendA = clazzStatistics.getTotalAttendNum();
        pojo.setAttendance_a(attendA == null ? 0 : attendA);
        Integer finishA = clazzStatistics.getTotalFinishNum();
        pojo.setComplete_a(finishA == null ? 0 : finishA);
        pojo.setAttendance_ratio_c(formatRatePercent(attendC, clazzStatistics.getClassNum()));
        pojo.setComplete_ratio_c(formatRatePercent(finishC, clazzStatistics.getClassNum()));
        pojo.setAttendance_ratio_a(formatRatePercent(attendA, clazzStatistics.getTotalNum()));
        pojo.setComplete_ratio_a(formatRatePercent(finishA, clazzStatistics.getTotalNum()));
        Integer classActiveNum = clazzStatistics.getClassActiveNum();
        Integer totalActiveNum = clazzStatistics.getTotalActiveNum();
        pojo.setActive_c(classActiveNum == null ? 0 : classActiveNum);
        pojo.setActive_a(totalActiveNum == null ? 0 : totalActiveNum);
        pojo.setActive_ratio_c(formatRatePercent(classActiveNum, clazzStatistics.getClassNum()));
        pojo.setActive_ratio_a(formatRatePercent(totalActiveNum, clazzStatistics.getTotalNum()));
        pojo.setClassRemarkNum(clazzStatistics.getClassRemarkNum());
        pojo.setPeriodRemarkNum(clazzStatistics.getPeriodRemarkNum());
        pojo.setClassRemarkRate(formatRatePercent(clazzStatistics.getClassRemarkNum(),(finishC - (clazzStatistics.getClassNotwxNum() == null ? 0 : clazzStatistics.getClassNotwxNum()))));
        pojo.setPeriodRemarkRate(formatRatePercent(clazzStatistics.getPeriodRemarkNum(),(finishA - (clazzStatistics.getPeriodNotwxNum() == null ? 0 : clazzStatistics.getPeriodNotwxNum()))));
        pojo.setClazzNum(clazzStatistics.getClassNum());
        pojo.setTotalNum(clazzStatistics.getTotalNum());
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
