package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author shiwe.liao
 * @since 2016-8-9
 */
@Named
@CacheBean(type = NewHomeworkStudyMaster.class,useValueWrapper = true)
@CacheDimension(value = CacheDimensionDistribution.ID_FIELD)
public class NewHomeworkStudyMasterDao extends AlpsStaticMongoDao<NewHomeworkStudyMaster,String> {
    @Override
    protected void calculateCacheDimensions(NewHomeworkStudyMaster document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }
}
