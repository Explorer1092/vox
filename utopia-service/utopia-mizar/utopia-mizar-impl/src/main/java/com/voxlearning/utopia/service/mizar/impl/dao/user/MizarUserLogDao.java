package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserLog;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by wangshichao on 16/9/7.
 */
@Named
public class MizarUserLogDao extends AlpsStaticMongoDao<MizarUserLog, String> {
    @Override
    protected void calculateCacheDimensions(MizarUserLog document, Collection<String> dimensions) {

    }
}
