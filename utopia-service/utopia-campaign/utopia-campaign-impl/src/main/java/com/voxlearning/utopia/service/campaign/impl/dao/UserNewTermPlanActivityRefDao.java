package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.campaign.api.entity.NewTermPlanActivity;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = NewTermPlanActivity.class, useValueWrapper = true)
public class UserNewTermPlanActivityRefDao extends StaticMySQLPersistence<NewTermPlanActivity, Long> {

    @Override
    protected void calculateCacheDimensions(NewTermPlanActivity document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }
}