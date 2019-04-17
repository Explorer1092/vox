package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.activity.NewYearWish;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by Summer on 2016/12/20.
 */
@Named
@CacheBean(type = NewYearWish.class)
public class NewYearWishDao extends AlpsStaticMongoDao<NewYearWish, Long> {

    @Override
    protected void calculateCacheDimensions(NewYearWish document, Collection<String> dimensions) {
        dimensions.add(NewYearWish.ck_id(document.getId()));
    }
}
