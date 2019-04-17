package com.voxlearning.utopia.service.newexam.api.utils;

public class NewExamUtils {
    public static String changeRateToLevel(int score) {
        if (score == 100) {
            return "A+";
        }
        if (score >= 90) {
            return "A";
        }
        if (score >= 80) {
            return "A-";
        }
        if (score >= 70) {
            return "B+";
        }
        if (score >= 60) {
            return "B";
        }
        if (score >= 40) {
            return "C+";
        }
        if (score >= 20) {
            return "C";
        }
        return "D";
    }

    /**
     * 数字转换成大写字母
     * @param number
     * @return 大写字母(A,B,C)
     */
    public static String convertToCapital(int number) {
        return String.valueOf((char) (number + 65));
    }
}
