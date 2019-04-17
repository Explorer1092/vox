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

package com.voxlearning.utopia.service.psr.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.psr.api.DPPsrService;
import com.voxlearning.utopia.service.psr.entity.midtermreview.EnglishPackage;
import com.voxlearning.utopia.service.psr.impl.midtermreview.loader.PsrPackageLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Service(interfaceClass = DPPsrService.class)
@ExposeService(interfaceClass = DPPsrService.class)
public class DPPsrServiceImpl implements DPPsrService {

    @Inject private PsrPackageLoaderImpl psrPackageLoader;

    @Override
    public List<EnglishPackage> loadPackage(String bookId, Integer groupId) {
        return psrPackageLoader.loadPackage(bookId, groupId);
    }
}
