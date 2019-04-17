package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Collection;

/**
 * 作业活动表
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Named
@CacheBean(type = Activity.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
@Slf4j
public class HomeworkActivityDao extends AlpsStaticMongoDao<Activity, String> {

    /**
     * 缓存维度
     *
     * @param document
     * @param dimensions
     */
    @Override
    protected void calculateCacheDimensions(Activity document, Collection<String> dimensions) {
        dimensions.add(document.ckId());
    }

}
