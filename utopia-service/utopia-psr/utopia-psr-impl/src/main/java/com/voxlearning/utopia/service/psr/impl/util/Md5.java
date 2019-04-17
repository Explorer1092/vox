package com.voxlearning.utopia.service.psr.impl.util;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by Administrator on 2016/8/26.
 */
public class Md5 {
    public  static String hexdigest(String message){
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(message.getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            String hd = hash.toString(16); // BigInteger strips leading 0's
            while (hd.length() < 32) {
                hd = "0" + hd;
            } // pad with leading 0's
            return hd;
        } catch (Exception ex) {
            return null;
        }
    }
}