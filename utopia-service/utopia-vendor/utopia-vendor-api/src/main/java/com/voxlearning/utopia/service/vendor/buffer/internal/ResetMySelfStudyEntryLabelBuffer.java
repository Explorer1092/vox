/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.buffer.internal;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.alps.api.context.NamedBean;
import com.voxlearning.alps.spi.test.AbstractTestExecutionListener;
import com.voxlearning.alps.spi.test.TestContext;
import com.voxlearning.utopia.service.vendor.buffer.MySelfStudyEntryLabelBuffer;

@Install
final public class ResetMySelfStudyEntryLabelBuffer extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        ApplicationContextScanner.getInstance()
                .getBeansOfType(ApplicationContextScanner.Scope.ALL, MySelfStudyEntryLabelBuffer.Aware.class)
                .stream()
                .map(NamedBean::getBean)
                .forEach(MySelfStudyEntryLabelBuffer.Aware::resetMySelfStudyEntryLabelBuffer);
    }
}
