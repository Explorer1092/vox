package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourseStudentSub;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:08
 **/
@Named
@CacheBean(type = LiveCastCourseStudentSub.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class LiveCastCourseStudentSubDao extends AsyncStaticMongoPersistence<LiveCastCourseStudentSub, String> {
    @Override
    protected void calculateCacheDimensions(LiveCastCourseStudentSub document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }



}
