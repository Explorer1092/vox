package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.TeachingResourceBook;

import javax.inject.Named;
import java.util.Collection;

@Named
public class TeachingResourceBookPersistence extends StaticMySQLPersistence<TeachingResourceBook, Long> {
    @Override
    protected void calculateCacheDimensions(TeachingResourceBook document, Collection<String> dimensions) {
        dimensions.add(TeachingResourceBook.ck_id(document.getId()));
    }
}
