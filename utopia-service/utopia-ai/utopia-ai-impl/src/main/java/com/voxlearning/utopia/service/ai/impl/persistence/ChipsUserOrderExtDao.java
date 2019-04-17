package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;

import javax.inject.Named;
import java.util.Collection;

@Named
public class ChipsUserOrderExtDao extends AlpsStaticMongoDao<ChipsUserOrderExt, String> {

    @Override
    protected void calculateCacheDimensions(ChipsUserOrderExt chipsUserOrderExt, Collection<String> collection) {
    }
}
