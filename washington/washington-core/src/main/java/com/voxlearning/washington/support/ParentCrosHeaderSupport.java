package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import org.springframework.http.HttpHeaders;

public class ParentCrosHeaderSupport {

    public static void setCrosHeader(UtopiaHttpRequestContext context) {
        String origin = context.getRequest().getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.isBlank(origin)) {
            return;
        }
        
        String domain = getParentDomain();
        if (RuntimeMode.current() == Mode.TEST || RuntimeMode.current() == Mode.DEVELOPMENT) {
            domain = origin;
        }

        context.getResponse().addHeader("Access-Control-Allow-Origin", domain);
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with,Content-Type");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");

    }


    private static String getParentDomain() {
        String domain = "https://parent";
        if (RuntimeModeLoader.getInstance().isProduction()) {
            domain += ".17zuoye.com";
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            domain += ".staging.17zuoye.net";
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            domain += ".test.17zuoye.net";
        } else {
            domain += ".test.17zuoye.net";
        }

        return domain;
    }
}
