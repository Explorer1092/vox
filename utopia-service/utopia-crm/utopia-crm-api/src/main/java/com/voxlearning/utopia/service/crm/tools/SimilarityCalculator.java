package com.voxlearning.utopia.service.crm.tools;


import com.voxlearning.alps.core.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class SimilarityCalculator {

    //提取出字符串中的数字
    private static String extractNumber(String schoolName) {
        String result = "";
        for (int i = 0; i < schoolName.length(); i++) {
            List<Character> list = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
            for (char cmpChar : list) {
                if (Objects.equals(schoolName.charAt(i), cmpChar)) {
                    result += cmpChar;
                }
            }
        }
        return result;
    }

    //判断字符串是否包含数字1-9
    private static boolean containNumber(String answer) {
        if (answer == null || answer.length() == 0) {
            return false;
        }
        List<Character> list = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9');
        char[] answer_char = answer.toCharArray();
        for (char one : answer_char) {
            if (list.contains(one)) {
                return false;
            }
        }
        return true;
    }

    public static double getSimilarityValue(String ugcName, String sysName, String type) {
        //修改3 提取预判是否相等
        if (Objects.equals(ugcName, sysName)) {
            return 1.0;
        }

        //修改4  去掉这步
//        boolean ugcIsPrimary = ugcName.contains("小学");
//        boolean sysIsPrimary = sysName.contains("小学");
//
//        boolean ugcIsJunior = ugcName.contains("初中");
//        boolean sysIsJunior = sysName.contains("初中");
//
//        boolean ugcIsHigh = ugcName.contains("高中");
//        boolean sysIsHigh = sysName.contains("高中");
//
//        if ((ugcIsPrimary && sysIsPrimary) || (ugcIsJunior && sysIsJunior) || (ugcIsHigh && sysIsHigh)) {
//        } else {
//            return  0.3;
//        }
        if(StringUtils.isBlank(sysName)){
            return 0.3;
        }

        String ugcExtractNum = extractNumber(ugcName);
        String sysExtractNum = extractNumber(sysName);

        if (ugcExtractNum.length() >= 0 && sysExtractNum.length() >= 0) {
            if (!ugcExtractNum.equals(sysExtractNum))
                return 0.0;
        }


        if (type.equals("高中")) {
            ugcName = ugcName.replace("高中", "");
            sysName = sysName.replace("高中", "");
        }

        if (type.equals("初中")) {
            ugcName = ugcName.replace("初中", "");
            sysName = sysName.replace("初中", "");
        }

        if (type.equals("小学")) {
            ugcName = ugcName.replace("小学", "");
            sysName = sysName.replace("小学", "");
        }


        int ugcCount = ugcName.length();
        int sysCount = sysName.length();
        int maxCount = ugcCount;
        if (sysCount > ugcCount) {
            maxCount = sysCount;
        }
        boolean ugcContainNum = containNumber(ugcName);
        boolean sysContainNum = containNumber(sysName);

        if (ugcCount > sysCount && ugcName.contains(sysName)) {
            return 0.750;
        } else if (ugcCount < sysCount && sysName.contains(ugcName)) {
            return 0.750;
        } else if ((sysContainNum && !ugcContainNum) || (!sysContainNum && ugcContainNum)) {
            return 0.45;
        } else {
            char[] ugcName_char = ugcName.toCharArray();
            int matchCount = 0;
            for (char one : ugcName_char) {
                String one_string = "" + one;
                if (sysName.contains(one_string)) {
                    matchCount += 1;
                }
            }

            if (maxCount == 0) {
                return 0.3;
            }
            return (double) matchCount * 1.0 / maxCount;
        }

    }

}
