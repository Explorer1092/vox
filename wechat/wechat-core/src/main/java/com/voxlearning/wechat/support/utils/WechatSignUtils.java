package com.voxlearning.wechat.support.utils;

import com.voxlearning.alps.core.util.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Xin Xin
 * @since 11/6/15
 */
public class WechatSignUtils {
//    private static final String PARTNER_KEY = "4f5991b4540fd8270aea368eed5c65da";

    private static final String Chips_Key = "jAR2QMsf8eI0gyxOB5aMTFMOEu7ejzr6";

    public static final String md5Sign(Map<String, Object> params) {
        String str = join(params) + "&key=" + Chips_Key;
        return DigestUtils.md5Hex(str);
    }

    public static final String sha1Sign(Map<String, Object> params) {
        return DigestUtils.sha1Hex(join(params));
    }

    private static final String join(Map<String, Object> params) {
        if (!(params instanceof TreeMap)) {
            Map<String, Object> sortedMap = new TreeMap<>();
            sortedMap.putAll(params);
            params = sortedMap;
        }

        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (!StringUtils.isEmpty(params.get(key).toString()) && !"sign".equals(key) && !"key".equals(key)) {
                if (sb.length() > 0) sb.append("&");
                sb.append(key).append("=").append(params.get(key));
            }
        }
        return sb.toString();
    }
}
