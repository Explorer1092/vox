package com.voxlearning.utopia.service.ai.util;

import com.voxlearning.alps.core.util.StringUtils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.security.MessageDigest;


public class StringExtUntil {

    private StringExtUntil() {}


    public static String getPinyinString(String chineseLanguage) {
        if (StringUtils.isBlank(chineseLanguage)) {
            return "";
        }

        char[] cl_chars = chineseLanguage.trim().toCharArray();
        String hanyupinyin = "";
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 输出拼音全部小写
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不带声调
        try {
            for (int i = 0; i < cl_chars.length; i++) {
                String str = String.valueOf(cl_chars[i]);
                if (str.matches("[\u4e00-\u9fa5]+")) {// 如果字符是中文,则将中文转为汉语拼音,并取第一个字母
                    hanyupinyin += PinyinHelper.toHanyuPinyinStringArray(
                            cl_chars[i], defaultFormat)[0];
                } else if (str.matches("[0-9]+")) {// 如果字符是数字,取数字
                    hanyupinyin += cl_chars[i];
                } else if (str.matches("[a-zA-Z]+")) {// 如果字符是字母,取字母
                    hanyupinyin += cl_chars[i];
                } else {// 否则不转换
                }
            }
        } catch (Exception e) {
        }
        return hanyupinyin;
    }

    public static String md5(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            msgd.update(text.getBytes("UTF-8"));
            byte[] bs = msgd.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bs) {
                sb.append(Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }

    }
}
