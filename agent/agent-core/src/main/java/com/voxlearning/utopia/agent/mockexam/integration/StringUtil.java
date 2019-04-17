package com.voxlearning.utopia.agent.mockexam.integration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符工具
 *
 * @author xiaolei.li
 * @version 2018/8/24
 */
public class StringUtil {

    /**
     * Unicode转 汉字字符串
     *
     * @param str \u6728
     * @return '木' 26408
     * @see <a href="https://blog.csdn.net/u013905744/article/details/74452012></a>
     */
    public static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            //group 6728
            String group = matcher.group(2);
            //ch:'木' 26408
            ch = (char) Integer.parseInt(group, 16);
            //group1 \u6728
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;

    }
}
