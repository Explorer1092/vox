package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.dao.mongo.dao.AlpsDateRangeMongoDao;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xuesong.zhang
 * @since 2017/6/21
 */
@Named
public class AvengerHomeworkDao extends AlpsDateRangeMongoDao<AvengerHomework> {
    @Override
    protected void calculateCacheDimensions(AvengerHomework document, Collection<String> dimensions) {

    }

    public void saveEntity(AvengerHomework entity) {
        $insert(entity);
    }

    public void saveEntities(Collection<AvengerHomework> entities) {
        $inserts(entities);
    }
}
