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

package com.voxlearning.washington.hotfix;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.api.bootstrap.PostBootstrapModule;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.spi.core.HttpClientType;

@Install
final public class TriggerSSLSocketInitialization implements PostBootstrapModule {

    @Override
    public void postBootstrapModule() {
        HttpRequestExecutor.instance(HttpClientType.POOLING)
                .get("https://www.17zuoye.com").turnOffLogException().execute();
        HttpRequestExecutor.instance(HttpClientType.BASIC)
                .get("https://www.17zuoye.com").turnOffLogException().execute();
    }
}
