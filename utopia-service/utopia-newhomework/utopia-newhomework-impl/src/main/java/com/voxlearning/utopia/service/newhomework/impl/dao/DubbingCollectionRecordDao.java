package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingCollectionRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;

/**
 * @Description: 趣味配音收藏
 * @author: Mr_VanGogh
 * @date: 2018/8/23 下午2:39
 */
@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class DubbingCollectionRecordDao extends StaticMongoShardPersistence<DubbingCollectionRecord, String> {

    @Override
    protected void calculateCacheDimensions(DubbingCollectionRecord document, Collection<String> dimensions) {
        dimensions.add(DubbingCollectionRecord.ck_id(document.getId()));
    }

    public DubbingCollectionRecord loadDubbingCollectionRecord(Long teacherId, Subject subject) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term.getKey(), subject, teacherId);
        DubbingCollectionRecord dubbingCollectionRecord = load(id);
        if (dubbingCollectionRecord != null) {
            return dubbingCollectionRecord;
        }
        return new DubbingCollectionRecord(id, teacherId, subject, year, term, new HashMap<>(), null, null);
    }
}
