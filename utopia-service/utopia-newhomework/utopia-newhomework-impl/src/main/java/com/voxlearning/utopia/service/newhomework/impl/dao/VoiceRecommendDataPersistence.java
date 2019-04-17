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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendData;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xuesong.zhang
 * @since 2016-06-01
 */
@Named
@CacheBean(type = VoiceRecommendData.class)
public class VoiceRecommendDataPersistence extends StaticMySQLPersistence<VoiceRecommendData, Long> {

    public VoiceRecommendDataPersistence() {
        registerBeforeInsertListener(collection -> collection.stream()
                .filter(e -> e.getDisabled() == null).forEach(e -> e.setDisabled(false)));
    }

    @Override
    protected void calculateCacheDimensions(VoiceRecommendData document, Collection<String> dimensions) {
        dimensions.add(VoiceRecommendData.ck_id(document.getId()));
    }
}
