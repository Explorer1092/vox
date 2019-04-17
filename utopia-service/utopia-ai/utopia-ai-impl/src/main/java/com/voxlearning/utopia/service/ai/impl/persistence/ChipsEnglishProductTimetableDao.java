package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guangqing
 * @since 2018/10/17
 */
@Named
@CacheBean(type = ChipsEnglishProductTimetable.class)
public class ChipsEnglishProductTimetableDao extends AlpsStaticMongoDao<ChipsEnglishProductTimetable, String> {
    @Override
    protected void calculateCacheDimensions(ChipsEnglishProductTimetable chipsEnglishCourse, Collection<String> collection) {
        collection.add(ChipsEnglishProductTimetable.ck_id(chipsEnglishCourse.getId()));
    }
}
