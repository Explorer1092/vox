package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusRecommendRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;

@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class PictureBookPlusRecommendRecordDao extends StaticMongoShardPersistence<PictureBookPlusRecommendRecord, String> {

    @Override
    protected void calculateCacheDimensions(PictureBookPlusRecommendRecord source, Collection<String> dimensions) {
        dimensions.add(PictureBookPlusRecommendRecord.ck_id(source.getId()));
    }

    public PictureBookPlusRecommendRecord loadPictureBookRecommendRecord(Subject subject, Long teacherId) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term.getKey(), subject, teacherId);

        PictureBookPlusRecommendRecord pictureBookRecommendRecord = load(id);
        if (pictureBookRecommendRecord != null) {
            return pictureBookRecommendRecord;
        }
        return new PictureBookPlusRecommendRecord(id, teacherId, subject, year, term, new HashMap<>(), null, null);
    }
}
