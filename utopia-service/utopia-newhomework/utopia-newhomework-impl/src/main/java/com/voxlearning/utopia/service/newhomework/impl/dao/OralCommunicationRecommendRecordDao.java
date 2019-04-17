package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.google.common.collect.Maps;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationRecommendRecord;

import javax.inject.Named;
import java.util.*;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/3
 * \* Time: 8:10 PM
 * \* Description:口语交际
 * \
 */
@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class OralCommunicationRecommendRecordDao extends StaticMongoShardPersistence<OralCommunicationRecommendRecord, String> {

    @Override
    protected void calculateCacheDimensions(OralCommunicationRecommendRecord source, Collection<String> dimensions) {
        dimensions.add(OralCommunicationRecommendRecord.ck_id(source.getId()));
    }

    public OralCommunicationRecommendRecord loadOralCommunicationRecommendRecord(Subject subject, Long teacherId) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term.getKey(), subject, teacherId);

        OralCommunicationRecommendRecord oralCommunicationRecommendRecord = load(id);
        if (oralCommunicationRecommendRecord != null) {
            return oralCommunicationRecommendRecord;
        }
        return new OralCommunicationRecommendRecord(id, teacherId, subject, year, term, new HashMap<>(), null, null);
    }

    public void updateOralCommunicationHistory(Long teacherId, Subject subject, List<String> stoneIds) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term.getKey(), subject, teacherId);
        OralCommunicationRecommendRecord recommendRecord = load(id);
        String day = DayRange.current().toString();
        if (recommendRecord != null) {
            Date current = new Date();
            Map<String, String> recommendInfo = recommendRecord.getOralCommunicationRecommendInfo();
            stoneIds.forEach(s -> recommendInfo.put(s, day));
            Criteria criteria = Criteria.where("_id").is(id);
            Update update = new Update();
            update.set("oralCommunicationRecommendInfo", recommendInfo);
            update.set("updateAt", current);
            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .upsert(false)
                    .returnDocument(ReturnDocument.AFTER);
            OralCommunicationRecommendRecord modified = $executeFindOneAndUpdate(createMongoConnection(calculateIdMongoNamespace(id), id), criteria, update, options).getUninterruptibly();
            if (modified != null) {
                getCache().createCacheValueModifier()
                        .key(OralCommunicationRecommendRecord.ck_id(id))
                        .expiration(getDefaultCacheExpirationInSeconds())
                        .modifier(currentValue -> modified)
                        .execute();
            }
            return;
        }
        recommendRecord = new OralCommunicationRecommendRecord(id, teacherId, subject, year, term, new HashMap<>(), null, null);
        Map<String, String> recommendInfoMap = Maps.newHashMap();
        stoneIds.forEach(s -> recommendInfoMap.put(s, day));
        recommendRecord.setOralCommunicationRecommendInfo(recommendInfoMap);
        insertIfAbsent(id, recommendRecord);
    }
}
