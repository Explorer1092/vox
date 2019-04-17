package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.AccessDeniedRecord;

import javax.inject.Named;
import java.util.Collection;

@Named
public class AccessDeniedRecordPersistence extends StaticMySQLPersistence<AccessDeniedRecord, Long> {
    @Override
    protected void calculateCacheDimensions(AccessDeniedRecord document, Collection<String> dimensions) {
    }
}
