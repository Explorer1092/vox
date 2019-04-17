/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.workflow;

import com.voxlearning.alps.runtime.RuntimeMode;

/**
 * Created by Alex on 14-8-8.
 */
public class AbstractWorkFlowProcessor implements WorkFlowProcessor {

    @Override
    public void agree(WorkFlowContext context) {
        // do nothing here
    }

    @Override
    public void reject(WorkFlowContext context) {
        // do nothing here
    }

    public String getAdminBaseUrl(){
        switch (RuntimeMode.current()) {
            case PRODUCTION:
                return "http://admin.17zuoye.net";
            case STAGING:
                return "http://admin.staging.17zuoye.net";
            case TEST:
                return "http://admin.test.17zuoye.net";
            case DEVELOPMENT:
                return "http://localhost:8082";
            default:
                return "http://admin.test.17zuoye.net";
        }
    }
}
