package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.crm.PushTask;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by wangshichao on 16/8/23.
 */

@Named
public class PushTaskDao  extends AlpsStaticMongoDao<PushTask, String> {

    @Override
    protected void calculateCacheDimensions(PushTask document, Collection<String> dimensions) {

    }
}
