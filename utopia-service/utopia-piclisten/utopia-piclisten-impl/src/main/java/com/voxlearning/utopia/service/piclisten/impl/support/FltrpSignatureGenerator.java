package com.voxlearning.utopia.service.piclisten.impl.support;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 7/13/17.
 */
@Slf4j
public class FltrpSignatureGenerator {

    public static String sign(Map<String, String> params, String secret) {
        List<String> parameters = new ArrayList<>();
        for (Map.Entry entry : params.entrySet()) {
            try {
                parameters.add(URLEncoder.encode(entry.getKey() + "=" + entry.getValue(), "UTF-8").replace("\\+", "%20"));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }

        Collections.sort(parameters);

        StringBuilder sb = new StringBuilder();
        for (String p : parameters) {
            if (sb.length() > 0) {
                sb.append("&").append(p);
            } else {
                sb.append(p);
            }
        }
        return md5(sb.toString() + secret);
    }

    private static String md5(String text) {
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
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
}
