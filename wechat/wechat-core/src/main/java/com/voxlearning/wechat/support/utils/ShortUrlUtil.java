package com.voxlearning.wechat.support.utils;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ShortUrlUtil {
    private static Logger logger = LoggerFactory.getLogger(ShortUrlUtil.class);

    public static String generatorWechatShortUrl(String url, String token) {
        if (StringUtils.isAnyBlank(url, token)) {
            return "";
        }
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("action", "long2short");
            map.put("long_url", "url");
            String responseStr = HttpRequestExecutor.defaultInstance()
                    .post("https://api.weixin.qq.com/cgi-bin/shorturl?access_token=" + token)
                    .json(map)
                    .execute().getResponseString();
            System.out.println(responseStr);
            Map<String, Object> resMap = JsonUtils.fromJson(responseStr);
            if (MapUtils.isEmpty(resMap) || resMap.get("errcode") == null || !resMap.get("errcode").equals(0) || resMap.get("short_url") == null) {
                logger.warn("wechat short url,response:{}", responseStr);
                return url;
            }
            return SafeConverter.toString(resMap.get("short_url"));
        } catch (Exception e) {
            return url;
        }
    }
}
