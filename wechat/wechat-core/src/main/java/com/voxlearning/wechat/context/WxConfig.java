package com.voxlearning.wechat.context;

import com.voxlearning.wechat.support.utils.WechatSignUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by xinxin on 4/1/2016.
 * Wechat Jsapi wxconfig
 */
@Getter
@Setter
public class WxConfig {
    private final String WX_CONFIG_TICKET = "jsapi_ticket";
    private final String WX_CONFIG_NONCE = "noncestr";
    private final String WX_CONFIG_TIMESTAMP = "timestamp";
    private final String WX_CONFIG_URL = "url";

    private String ticket;
    private String nonce;
    private Long timestamp;
    private String url;

    public WxConfig(String url, String ticket) {
        this.url = url;
        this.ticket = ticket;
        this.nonce = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        this.timestamp = new Date().getTime() / 1000;
    }

    private Map<String, Object> toMap() {
        Map<String, Object> params = new TreeMap<>();
        params.put(WX_CONFIG_TICKET, ticket);
        params.put(WX_CONFIG_NONCE, nonce);
        params.put(WX_CONFIG_TIMESTAMP, timestamp);
        params.put(WX_CONFIG_URL, url);
        return params;
    }

    public String sha1Sign() {
        return WechatSignUtils.sha1Sign(toMap());
    }
}
