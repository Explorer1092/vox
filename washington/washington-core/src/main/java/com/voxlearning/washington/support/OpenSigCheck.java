package com.voxlearning.washington.support;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * OpenAPI 生成签名类
 * 参考了腾讯开放平台第三方应用签名参数sig算法
 *
 * @author Junjie Zhang
 * @since 2014-01-15
 */

public class OpenSigCheck {

    // 编码方式
    private static final String CONTENT_CHARSET = "UTF-8";

    // HMAC算法
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    /**
     * URL编码 (符合FRC1738规范)
     *
     * @param input 待编码的字符串
     * @return 编码后的字符串
     * @throws java.io.UnsupportedEncodingException 不支持指定编码时抛出异常。
     */
    public static String encodeUrl(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, CONTENT_CHARSET).replace("+", "%20").replace("*", "%2A");
    }

    /* 生成签名
     *
     * @param method HTTP请求方法 "GET" / "POST"
     * @param url_path CGI名字, eg: /api/user/get_purchase_info.vpage
     * @param params URL请求参数
     * @param secret 密钥
     * @return 签名值
     * @throws Exception 不支持指定编码以及不支持指定的加密方法时抛出异常。
     */

    public static String makeSig(String method, String url_path, Map<String, String> params, String secret)
            throws Exception {

        Mac mac = Mac.getInstance(HMAC_ALGORITHM);

        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(CONTENT_CHARSET), mac.getAlgorithm());

        mac.init(secretKey);

        String mk = makeSource(method, url_path, params);

        byte[] hash = mac.doFinal(mk.getBytes(CONTENT_CHARSET));

        // base64
        String sig = new String(Base64.encodeBase64(hash));

        return sig;
    }

    /* 生成签名所需源串
     *
     * @param method HTTP请求方法 "GET" / "POST"
     * @param url_path CGI名字, eg: /api/user/get_purchase_info.vpage
     * @param params URL请求参数
     * @return 签名所需源串
     */
    public static String makeSource(String method, String url_path, Map<String, String> params)
            throws Exception {
        Object[] keys = params.keySet().toArray();

        Arrays.sort(keys);

        StringBuilder buffer = new StringBuilder(128);

        buffer.append(method.toUpperCase()).append("&").append(encodeUrl(url_path)).append("&");

        StringBuilder buffer2 = new StringBuilder();

        for (int i = 0; i < keys.length; i++) {
            buffer2.append(keys[i]).append("=").append(params.get(keys[i]));

            if (i != keys.length - 1) {
                buffer2.append("&");
            }
        }

        buffer.append(encodeUrl(buffer2.toString()));

        return buffer.toString();
    }

    public static boolean verifySig(String method, String url_path, Map<String, String> params, String secret, String sig) throws Exception {
        // 确保不含sig
        params.remove("sig");

        // 按照回调接口的编码规则对value编码
        encodeParamValues(params);

        // 计算签名
        String sig_new = makeSig(method, url_path, params, secret);

        // 对比和返回的签名
        return sig_new.equals(sig);
    }

    /**
     * 应用URL接口对回调传来的参数value值先进行一次编码方法，用于验签
     * (编码规则为：除了 0~9 a~z A~Z !*() 之外其他字符按其ASCII码的十六进制加%进行表示，例如“-”编码为“%2D”)
     *
     * @param params 回调传参Map (key,value);
     */
    public static void encodeParamValues(Map<String, String> params) {
        Set<String> keySet = params.keySet();
        Iterator<String> itr = keySet.iterator();

        while (itr.hasNext()) {
            String key = itr.next();
            String value = params.get(key);
            value = encodeValue(value);
            params.put(key, value);
        }
    }

    /**
     * 应用URL接口的编码规则
     *
     * @param s
     * @return
     */
    public static String encodeValue(String s) {
        String rexp = "[0-9a-zA-Z!*\\(\\)]";
        StringBuffer sb = new StringBuffer(s);
        StringBuffer sbRtn = new StringBuffer();
        Pattern p = Pattern.compile(rexp);
        char temp;
        String tempStr;

        for (int i = 0; i < sb.length(); i++) {
            temp = sb.charAt(i);
            tempStr = String.valueOf(temp);
            Matcher m = p.matcher(tempStr);

            boolean result = m.find();
            if (!result) {
                tempStr = hexString(tempStr);
            }
            sbRtn.append(tempStr);
        }

        return sbRtn.toString();
    }

    /**
     * URL十六进制编码
     *
     * @param s
     * @return
     */
    private static String hexString(String s) {
        byte[] b = s.getBytes();
        String retStr = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            retStr = "%" + hex.toUpperCase();
        }
        return retStr;
    }

}
