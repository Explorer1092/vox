package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusHistory;

import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type = PictureBookPlusHistory.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class PictureBookPlusHistoryDao extends AsyncStaticMongoPersistence<PictureBookPlusHistory, String> {
    @Override
    protected void calculateCacheDimensions(PictureBookPlusHistory document, Collection<String> dimensions) {
        dimensions.add(PictureBookPlusHistory.ck_id(document.getId()));
    }

    public PictureBookPlusHistory load(Long teacherId, Subject subject) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        int year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, subject, teacherId);
        return load(id);
    }

    public void updatePictureBookPlusHistory(Long teacherId, Subject subject, Set<String> pictureBookPlusIds) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        int year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, subject, teacherId);
        if (CollectionUtils.isNotEmpty(pictureBookPlusIds)) {
            Date current = new Date();
            LinkedHashMap<String, Date> pictureBookInfo = new LinkedHashMap<>();
            for (String pictureBookPlusId : pictureBookPlusIds) {
                pictureBookInfo.put(pictureBookPlusId, current);
            }
            PictureBookPlusHistory history = load(id);
            if (history == null) {
                history = new PictureBookPlusHistory();
                history.setId(id);
                history.setYear(year);
                history.setTerm(term);
                history.setSubject(subject);
                history.setTeacherId(teacherId);
                history.setPictureBookInfo(pictureBookInfo);
                insertIfAbsent(id, history);
            } else {
                if (MapUtils.isNotEmpty(history.getPictureBookInfo())) {
                    for (Map.Entry<String, Date> info : history.getPictureBookInfo().entrySet()) {
                        if (!pictureBookInfo.containsKey(info.getKey())) {
                            pictureBookInfo.put(info.getKey(), info.getValue());
                        }
                    }
                }
                Criteria criteria = Criteria.where("_id").is(id);
                Update update = new Update();
                update.set("pictureBookInfo", pictureBookInfo);
                update.set("updateAt", current);

                FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                        .upsert(false)
                        .returnDocument(ReturnDocument.AFTER);

                PictureBookPlusHistory modified = $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
                if (modified != null) {
                    getCache().createCacheValueModifier()
                            .key(PictureBookPlusHistory.ck_id(id))
                            .expiration(getDefaultCacheExpirationInSeconds())
                            .modifier(currentValue -> modified)
                            .execute();
                }
            }
        }
    }
}
