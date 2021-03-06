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

package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.activity.ActivityMothersDayCard;

import javax.inject.Named;
import java.util.Collection;

@Named("com.voxlearning.utopia.service.campaign.impl.dao.ActivityMothersDayCardPersistence")
@CacheBean(type = ActivityMothersDayCard.class)
public class ActivityMothersDayCardPersistence extends AlpsStaticJdbcDao<ActivityMothersDayCard, Long> {

    @Override
    protected void calculateCacheDimensions(ActivityMothersDayCard document, Collection<String> dimensions) {
        dimensions.add(ActivityMothersDayCard.ck_id(document.getStudentId()));
    }

    public boolean updateSended(Long id) {
        ActivityMothersDayCard card = new ActivityMothersDayCard();
        card.setStudentId(id);
        card.setSended(true);
        return replace(card) != null;
    }

    public boolean updateShared(Long id) {
        ActivityMothersDayCard card = new ActivityMothersDayCard();
        card.setStudentId(id);
        card.setShared(true);
        return replace(card) != null;
    }
}
