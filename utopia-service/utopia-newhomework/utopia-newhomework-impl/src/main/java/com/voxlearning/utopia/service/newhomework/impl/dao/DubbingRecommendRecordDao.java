package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingRecommendRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;

@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class DubbingRecommendRecordDao extends StaticMongoShardPersistence<DubbingRecommendRecord, String> {

    @Override
    protected void calculateCacheDimensions(DubbingRecommendRecord document, Collection<String> dimensions) {
        dimensions.add(DubbingRecommendRecord.ck_id(document.getId()));
    }

    public DubbingRecommendRecord loadDubbingRecommendRecord(Long teacherId, Subject subject) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term.getKey(), subject, teacherId);
        DubbingRecommendRecord dubbingRecommendRecord = load(id);
        if (dubbingRecommendRecord != null) {
            return dubbingRecommendRecord;
        }
        return new DubbingRecommendRecord(id, teacherId, subject, year, term, new HashMap<>(), null, null);
    }
}
