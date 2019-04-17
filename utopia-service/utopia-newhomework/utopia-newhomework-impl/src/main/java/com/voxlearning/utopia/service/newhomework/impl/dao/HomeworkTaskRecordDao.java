package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.async.AsyncStaticMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkTaskRecord;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guoqiang.li
 * @since 2017/4/19
 */
@Named
@UtopiaCacheSupport(value = HomeworkTaskRecord.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class HomeworkTaskRecordDao extends AsyncStaticMongoDao<HomeworkTaskRecord, String> {
    @Override
    protected void calculateCacheDimensions(HomeworkTaskRecord document, Collection<String> dimensions) {
        dimensions.add(HomeworkTaskRecord.ck_id(document.getId()));
    }
}
