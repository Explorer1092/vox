package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.campaign.api.entity.WarmHeartPlanActivity;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = WarmHeartPlanActivity.class, useValueWrapper = true)
public class WarmHeartPlanActivityDao extends StaticMySQLPersistence<WarmHeartPlanActivity, Long> {

    @Override
    protected void calculateCacheDimensions(WarmHeartPlanActivity document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

}