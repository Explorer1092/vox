package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserProgress;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = HomeworkUserProgress.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class HomeworkUserProgressDao extends AlpsStaticMongoDao<HomeworkUserProgress, String> {

    @Override
    protected void calculateCacheDimensions(HomeworkUserProgress document, Collection<String> dimensions) {
        dimensions.add(document.ckUserId());
        dimensions.add(document.ckId());
    }

    @CacheMethod
    public Collection<HomeworkUserProgress> loadByUserId(@CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria
                .where("userId").is(userId);
        return query(Query.query(criteria));
    }
}
