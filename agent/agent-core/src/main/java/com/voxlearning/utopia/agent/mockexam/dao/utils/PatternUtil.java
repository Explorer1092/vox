package com.voxlearning.utopia.agent.mockexam.dao.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 *
 * @Author: peng.zhang
 * @Date: 2018/8/24
 */
public class PatternUtil {

    public static boolean isNumber(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
