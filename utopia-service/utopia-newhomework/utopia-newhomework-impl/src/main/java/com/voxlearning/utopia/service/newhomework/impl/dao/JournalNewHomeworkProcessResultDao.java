package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.dao.mongo.dao.AlpsDateRangeMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;

import javax.inject.Named;
import java.util.Collection;

@Named
public class JournalNewHomeworkProcessResultDao extends AlpsDateRangeMongoDao<JournalNewHomeworkProcessResult> {

    @Override
    protected void calculateCacheDimensions(JournalNewHomeworkProcessResult source, Collection<String> dimensions) {
    }
}
