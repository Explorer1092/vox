package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;

/**
 * @author jiangpeng
 * @since 2018-12-21 3:54 PM
 **/
public class CourseStructRouter {

    private static String cdnUrl;
    private static String requestUrl;

    static {
        Mode current = RuntimeMode.current();
        switch (current) {
            case PRODUCTION:
                requestUrl = "https://www.17zuoye.com";
                cdnUrl = "https://cdn-17pmc.17zuoye.cn/";
                break;
            case STAGING:
                requestUrl = "https://www.staging.17zuoye.net";
                cdnUrl = "https://cdn-17pmc.17zuoye.cn/";
                break;
            case TEST:
                requestUrl = "https://www.test.17zuoye.net";
                cdnUrl = "https://17-pmc-test.oss-cn-beijing.aliyuncs.com/";
                break;
            case DEVELOPMENT:
                requestUrl = "https://www.test.17zuoye.net";
                cdnUrl = "https://17-pmc-test.oss-cn-beijing.aliyuncs.com/";
                break;
            default:
                requestUrl = "https://www.17zuoye.com";
                cdnUrl = "https://cdn-17pmc.17zuoye.cn/";
                break;
        }
    }

    public static String getRealHttpUrl(String url) {
        if (url.startsWith("/")) {
            return requestUrl + url;
        } else {
            return requestUrl + "/" + url;
        }
    }

    public static String getHostHttpUrl() {
        return requestUrl;
    }

    public static String getRealCdnUrl(String url) {
        return cdnUrl + url;
    }
}
