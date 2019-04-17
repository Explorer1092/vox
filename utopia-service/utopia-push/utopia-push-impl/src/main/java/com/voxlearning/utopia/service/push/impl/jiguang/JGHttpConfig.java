package com.voxlearning.utopia.service.push.impl.jiguang;

import lombok.Getter;
import lombok.Setter;

import java.lang.invoke.MethodType;

/**
 * 极光配置
 *
 * @author Wenlong Meng
 * @since Mar 13, 2019
 */
@Getter
@Setter
public class JGHttpConfig {
    private String url;
    private String appKey;
    private String secret;
    private String method;

    public JGHttpConfig(String url, String method) {
        this.url = url;
        this.method = method;
    }
}
