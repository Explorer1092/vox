package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourse;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:07
 **/
@Named
@CacheBean(type = LiveCastCourse.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class LiveCastCourseDao extends AsyncStaticMongoPersistence<LiveCastCourse, String> {

    @Override
    protected void calculateCacheDimensions(LiveCastCourse document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }
}
