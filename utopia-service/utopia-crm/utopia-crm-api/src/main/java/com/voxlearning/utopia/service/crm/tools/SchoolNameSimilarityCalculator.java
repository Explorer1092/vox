package com.voxlearning.utopia.service.crm.tools;

import java.util.Objects;

public class SchoolNameSimilarityCalculator {

    public static Double calculateSimilarityValue(String schoolNameUgc, String schoolNameSys, String city, String type) {
        if (type.equals("初中") || type.equals("高中") || type.equals("小学") || type.equals("学前")) {
            if (Objects.equals(schoolNameSys, schoolNameUgc)) {
                return 1.0;
            } else {
                String schoolNameUgc_filter = SchoolNameFilter.getTempResult(schoolNameUgc, city, type);
                String schoolNameSys_filter = SchoolNameFilter.getTempResult(schoolNameSys, city, type);
                return SimilarityCalculator.getSimilarityValue(schoolNameUgc_filter, schoolNameSys_filter, type);
            }
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
//        System.out.println(SchoolNameSimilarityCalculator.calculateSimilarityValue("乌鲁木齐市四十一小学英语实验学校","乌鲁木齐市四十一小学外语实验学校","乌鲁木齐市","小学"));
        System.out.println(SchoolNameSimilarityCalculator.calculateSimilarityValue("道滘镇中心幼儿园", "东莞塘厦中心幼儿园", "东莞市", "学前"));
    }

}
