package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.StringUtils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

/**
 *  关于汉字转拼音的一些工具
 * @author deliang.che
 * @since 2018/6/29
 */
public class Pinyin4jUtils {

    /**
     * 将中文汉字转为拼音
     * @param chinese
     * @return
     */
    public static String getFullSpell(String chinese) {
        if (StringUtils.isBlank(chinese)){
            return null;
        }
        StringBuffer sb = new StringBuffer();

        char[] arr = chinese.toCharArray();

        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 返回中文汉字的首字母（大写）
     * @param chinese
     * @return
     */
    public static String getFirstCapital(String chinese){
        try{
            String pinyin = Pinyin4jUtils.getFullSpell(chinese);
            if (StringUtils.isBlank(pinyin)){
                return "Z";
            }
            return pinyin.substring(0, 1).toUpperCase();
        }catch (Exception e){
            return "Z";
        }

    }

    public static void main(String[] args) {
        System.out.println("getFullSpell method result:"+Pinyin4jUtils.getFullSpell("车德亮"));
        System.out.println("getFirstCapital method result:"+Pinyin4jUtils.getFirstCapital("车德亮"));
    }
}
