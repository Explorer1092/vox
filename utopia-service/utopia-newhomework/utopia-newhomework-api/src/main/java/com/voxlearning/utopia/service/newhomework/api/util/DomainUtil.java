package com.voxlearning.utopia.service.newhomework.api.util;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;

public class DomainUtil {

    private static String cdnUrl;

    static {
        Mode current = RuntimeMode.current();
        switch (current) {
            case PRODUCTION:
                cdnUrl = "https://cdn-17pmc.17zuoye.cn/";
                break;
            case STAGING:
                cdnUrl = "https://cdn-17pmc.17zuoye.cn/";
                break;
            case TEST:
                cdnUrl = "https://17-pmc-test.oss-cn-beijing.aliyuncs.com/";
                break;
            case DEVELOPMENT:
                cdnUrl = "https://17-pmc-test.oss-cn-beijing.aliyuncs.com/";
                break;
            default:
                cdnUrl = "https://cdn-17pmc.17zuoye.cn/";
                break;
        }
    }

    public static String getRealCdnUrl(String url) {
        return cdnUrl + url;
    }

    public static String getRealCdnUrl() {
        return cdnUrl;
    }
}
