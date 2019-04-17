package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.UploaderResourceLibrary;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamAnswer;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
@CacheBean(type = UploaderResourceLibrary.class, cacheName = "utopia-homework-cache")
public class UploaderResourceLibraryDao extends StaticMongoShardPersistence<UploaderResourceLibrary, String> {
    @Override
    protected void calculateCacheDimensions(UploaderResourceLibrary document, Collection<String> dimensions) {
        dimensions.add(UploaderResourceLibrary.ck_id(document.getId()));
    }
}
