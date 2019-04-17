package com.voxlearning.wechat.support.utils;

import com.voxlearning.alps.core.util.StringUtils;
import java.security.MessageDigest;


public class StringExtUntil {

    private StringExtUntil() {}

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
