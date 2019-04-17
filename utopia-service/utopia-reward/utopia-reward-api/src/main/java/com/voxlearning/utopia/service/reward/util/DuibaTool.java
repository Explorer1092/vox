package com.voxlearning.utopia.service.reward.util;

import com.voxlearning.alps.logger.LoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;

public class DuibaTool {
    protected final static Logger logger = LoggerFactory.getLogger(DuibaTool.class);

    public static String buildUrlWithSign(String url, Map<String, String> params, DuibaApp app) {
        return buildUrlWithSign(url, params, app.getAppKey(), app.getAppSecret());
    }

    public static String buildUrlWithSign(String url, Map<String, String> params, String appKey, String appSecret) {
        Map<String, String> newparams = new HashMap<>(params);
        newparams.put("appKey", appKey);
        newparams.put("appSecret", appSecret);
        newparams.computeIfAbsent("timestamp", k -> System.currentTimeMillis() + "");
        String sign = sign(newparams);
        newparams.put("sign", sign);

        newparams.remove("appSecret");
        if (!url.endsWith("?")) {
            url += "?";
        }
        for (String key : newparams.keySet()) {
            try {
                if (newparams.get(key) == null || newparams.get(key).length() == 0) {
                    url += key + "=" + newparams.get(key) + "&";
                } else {
                    url += key + "=" + URLEncoder.encode(newparams.get(key), "utf-8") + "&";
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public static boolean signVerify(HttpServletRequest request, DuibaApp app){
        Map<String, String[]> map=request.getParameterMap();
        Map<String, String> data=new HashMap<>();
        for(String key:map.keySet()){
            data.put(key, map.get(key)[0]);
        }
        return signVerify(app.getAppSecret(), data);
    }

    public static boolean signVerify(String appSecret, Map<String, String> params){
        Map<String, String> map=new HashMap<>();
        map.put("appSecret", appSecret);

        for(String key:params.keySet()){
            if(!key.equals("sign")){
                map.put(key, params.get(key));
            }
        }
        String sign=sign(map);
        if(sign.equals(params.get("sign"))){
            return true;
        }
        logger.warn("mysign:{}, sign:{}", sign, params.get("sign"));
        return false;
    }
    private static String sign(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        String string = "";
        for (String s : keys) {
            string += params.get(s);
        }
        String sign;
        try {
            sign = toHexValue(encryptMD5(string.getBytes(Charset.forName("utf-8"))));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("md5 error");
        }
        return sign;
    }

    private static byte[] encryptMD5(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        return md5.digest();
    }

    private static String toHexValue(byte[] messageDigest) {
        if (messageDigest == null)
            return "";
        StringBuilder hexValue = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            int val = 0xFF & aMessageDigest;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum DuibaApp {
        TEST_XUEDOU(45647, "4PqtZp3riHqdY4QrJN5jCxgVqAq4", "49ktZd4Cc25VXGqsPg1kKuEc5Fkf"),
        TEST_YUANDINGDOU(46370, "4Pys75duZmSz2UFcqsogEGopDxVB", "2g2b1CcBRxbGFYeZk8iyWV98VK2e"),
        ONLINE_XUEDOU(47039, "47eGYZcMqA6dfM8Y9vYUhRnkinw3", "Rc3PSw7aRNvH1LBGF4yDdq23Z4G"),
        ONLINE_YUANDINGDOU(47038, "1ehHCNnuwS8QMbWJYnYQMxumxrF", "NwJhczA68HjbuGCfJ8zPovbsWAY");

        @Getter
        private final int appId;
        @Getter
        private final String appKey;
        @Getter
        private final String appSecret;
    }
}
