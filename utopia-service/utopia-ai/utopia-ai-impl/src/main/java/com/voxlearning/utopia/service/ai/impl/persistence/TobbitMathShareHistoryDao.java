package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.TobbitMathShareHistory;

import javax.inject.Named;
import java.util.Collection;

@Named
public class TobbitMathShareHistoryDao extends AlpsStaticMongoDao<TobbitMathShareHistory, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathShareHistory document, Collection<String> dimensions) {
        dimensions.add(TobbitMathShareHistory.ck_id(document.getId()));
        dimensions.add(TobbitMathShareHistory.ck_uid(document.getUid()));
        dimensions.add(TobbitMathShareHistory.ck_openId(document.getOpenId()));
    }

}
