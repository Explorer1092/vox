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

package com.voxlearning.utopia.service.newexam.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationParentCacheManager;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationTeacherOpenReportCacheManager;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationTeacherShareCacheManager;
import lombok.Getter;


import javax.inject.Named;

@Named
public class NewExamCacheClient extends SpringContainerSupport {

    public _NewExamCacheSystem cacheSystem;

    @Getter private EvaluationParentCacheManager evaluationParentCacheManager;
    @Getter private EvaluationTeacherShareCacheManager evaluationTeacherShareCacheManager;
    @Getter private EvaluationTeacherOpenReportCacheManager evaluationTeacherOpenReportCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        cacheSystem = new _NewExamCacheSystem();
        cacheSystem.CBS = new CBS_Container();
        cacheSystem.CBS.flushable = CacheSystem.CBS.getCacheBuilder().getCache("flushable");
        cacheSystem.CBS.unflushable = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
        cacheSystem.CBS.storage = CacheSystem.CBS.getCacheBuilder().getCache("storage");
        evaluationParentCacheManager = new EvaluationParentCacheManager(cacheSystem.CBS.flushable);
        evaluationTeacherShareCacheManager = new EvaluationTeacherShareCacheManager(cacheSystem.CBS.flushable);
        evaluationTeacherOpenReportCacheManager = new EvaluationTeacherOpenReportCacheManager(cacheSystem.CBS.storage);
    }

    public static class _NewExamCacheSystem {
        public CBS_Container CBS;
    }

    public static class CBS_Container {
        public UtopiaCache flushable;
        public UtopiaCache unflushable;
        public UtopiaCache storage;
    }
}
