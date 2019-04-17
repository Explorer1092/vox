package com.voxlearning.utopia.service.newhomework.impl.dao;


import com.voxlearning.alps.dao.mongo.dao.AlpsDateRangeMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;

import javax.inject.Named;
import java.util.Collection;

@Named
public class JournalStudentHomeworkDao extends AlpsDateRangeMongoDao<JournalStudentHomework> {
    @Override
    protected void calculateCacheDimensions(JournalStudentHomework source, Collection<String> dimensions) {

    }

    /**
     * 这个数据不需要缓存
     */
    public void saveJournalStudentHomework(JournalStudentHomework entity) {
        $insert(entity);
    }
}
