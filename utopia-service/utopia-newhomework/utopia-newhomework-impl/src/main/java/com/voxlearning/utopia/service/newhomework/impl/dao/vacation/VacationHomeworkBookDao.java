package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkBook;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guoqiang.li
 * @since 2017/6/5
 */
@Named
@CacheBean(type = VacationHomeworkBook.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class VacationHomeworkBookDao extends StaticMongoShardPersistence<VacationHomeworkBook, String> {
    @Override
    protected void calculateCacheDimensions(VacationHomeworkBook document, Collection<String> dimensions) {
        dimensions.add(VacationHomeworkBook.ck_id(document.getId()));
    }
}
