package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/2/14
 */
@Getter
@Setter
public class ClazzComparePojo implements Serializable{

    private static final long serialVersionUID = 404639170510398612L;
    //学习人数
    private int totalUserCount;
    //完课人数
    private int totalCompleteCount;
    //完课率
    private String totalCompleteRate;
    //第一个表数据
    private List<FirstTable> firstList;
    //完课点评人数
    private int totalRemarkCount;
    //完课点评率
    private String totalRemarkRate;
    //第二个统计表数据
    private List<SecondTable> secondList;
    //续费人数
    private int totalPaidCount;
    //续费率
    private String totalPaidRate;
    //第三个统计表数据
    private List<ThridTable> thridList;

    @Getter
    @Setter
    public static class FirstTable implements Serializable {

        private static final long serialVersionUID = 5952388404952125847L;
        private Long clazzId;
        private String productId;
        private String clazzName;
        private int userCount;//学习人数
        private int completeCount;//完课人数
        private double completeRate;//完课率
        private String completeRateStr;//完课率格式
        private int remindCount;//催补课完成人数
    }

    @Getter
    @Setter
    public static class SecondTable implements Serializable {

        private static final long serialVersionUID = 2534215327372489182L;
        private Long clazzId;
        private String productId;
        private String clazzName;
        private int completeCount;//完课人数
        private int remarkCount;//完课点评人数
        private double remarkRate;//完课率
        private String remarkRateStr;//完课率格式
    }

    @Getter
    @Setter
    public static class ThridTable implements Serializable {

        private static final long serialVersionUID = 8862154544533896874L;
        private Long clazzId;
        private String productId;
        private String clazzName;
        private int gradeCount;//定级人数
        private int paidCount;//续费人数
        private double paidRate;//续费率
        private String paidRateStr;//续费率格式
        private int paidRemindCount;//续费提醒完成人数
    }

}
