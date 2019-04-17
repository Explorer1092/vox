package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUpdateLog;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2019/1/7
 */
@Named
@CacheBean(type = ChipsEnglishClassUpdateLog.class)
public class ChipsEnglishClassUpdateLogDao extends AsyncStaticMongoPersistence<ChipsEnglishClassUpdateLog, String> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishClassUpdateLog chipsEnglishClassUpdateLog, Collection<String> collection) {

    }
}
