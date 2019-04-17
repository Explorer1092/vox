package com.voxlearning.utopia.service.guest.impl.utils;

import com.voxlearning.alps.core.util.StringUtils;

/**
 * @author lcy
 * @since 17/3/1
 */
public class GuestUtils {

    public static boolean isValidChineseName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }

        String tempUserName = userName.trim().replaceAll(" ", "").replaceAll("　", "");
        String regExStudentName = "^[\u4E00-\u9FA5]{0,5}$";

        return tempUserName.matches(regExStudentName);
    }

    // PS 暂时没想好放哪 先这样吧
    // 单位数组
    static String[] units = new String[]{"十", "百", "千", "万", "十", "百", "千", "亿"};
    // 中文大写数字数组
    static String[] numeric = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

    public static String fromNumberToCNNumber(String num) {
        // 遍历一行中所有数字
        String res = "";
        for (int k = -1; num.length() > 0; k++) {
            // 解析最后一位
            int j = Integer.parseInt(num.substring(num.length() - 1, num.length()));
            String rtemp = numeric[j];

            // 数值不是0且不是个位 或者是万位或者是亿位 则去取单位
            if (j != 0 && k != -1 || k % 8 == 3 || k % 8 == 7) {
                rtemp += units[k % 8];
            }

            // 拼在之前的前面
            res = rtemp + res;

            // 去除最后一位
            num = num.substring(0, num.length() - 1);
        }

        // 去除后面连续的零零..
        while (res.endsWith(numeric[0])) {
            res = res.substring(0, res.lastIndexOf(numeric[0]));
        }

        // 将零零替换成零
        while (res.contains(numeric[0] + numeric[0])) {
            res = res.replaceAll(numeric[0] + numeric[0], numeric[0]);
        }

        // 将 零+某个单位 这样的窜替换成 该单位 去掉单位前面的零
        for (int m = 1; m < units.length; m++) {
            res = res.replaceAll(numeric[0] + units[m], units[m]);
        }

        return res;
    }

    public static void main(String[] args) {
        System.out.println(fromNumberToCNNumber("12"));
    }
}
