package com.voxlearning.utopia.service.newhomework.impl.dao.livecast;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkBook;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
@UtopiaCacheSupport(LiveCastHomeworkBook.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class LiveCastHomeworkBookDao extends AlpsStaticMongoDao<LiveCastHomeworkBook, String> {
    @Override
    protected void calculateCacheDimensions(LiveCastHomeworkBook document, Collection<String> dimensions) {
        dimensions.add(LiveCastHomeworkBook.ck_id(document.getId()));
    }
}
