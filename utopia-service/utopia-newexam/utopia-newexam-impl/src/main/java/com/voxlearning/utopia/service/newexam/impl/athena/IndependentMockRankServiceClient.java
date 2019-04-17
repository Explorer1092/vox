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

package com.voxlearning.utopia.service.newexam.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.IndependentMockRankService;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/5/12
 */
@Named("com.voxlearning.utopia.service.newexam.impl.athena.IndependentMockRankServiceClient")
public class IndependentMockRankServiceClient {

    @Getter
    @ImportService(interfaceClass = IndependentMockRankService.class)
    private IndependentMockRankService independentMockRankService;
}