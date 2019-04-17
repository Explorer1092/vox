package com.voxlearning.utopia.admin.service;

import com.voxlearning.alps.runtime.RuntimeMode;

abstract public class AbstractAdminService extends AbstractCrmService {
    public String getMarketingUrl() {
        switch (RuntimeMode.current()) {
            case PRODUCTION:
                return "http://marketing.oaloft.com";
            case STAGING:
                return "http://marketing.staging.17zuoye.net";
            case TEST:
                return "http://marketing.test.17zuoye.net";
            case DEVELOPMENT:
                return "http://localhost:8083";
            default:
                return "http://marketing.test.17zuoye.net";
        }
    }
}
