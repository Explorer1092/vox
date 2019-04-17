package com.voxlearning.utopia.service.dubbing.impl.dao;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingRaw;

import javax.inject.Named;
import java.util.Collection;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/10/12
 */
@Named
public class DubbingRawDao extends AlpsStaticMongoDao<DubbingRaw, String> {
    /**
     * Calculate cache dimensions based on specified document.
     *
     * @param document   the non null document.
     * @param dimensions put calculated result into this
     */
    @Override
    protected void calculateCacheDimensions(DubbingRaw document, Collection<String> dimensions) {

    }
}
