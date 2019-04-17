package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyAccomplishment;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;

@Named
@CacheBean(type = SelfStudyAccomplishment.class, useValueWrapper = true)
public class SelfStudyAccomplishmentDao extends DynamicMongoShardPersistence<SelfStudyAccomplishment, String> {
    @Override
    protected void calculateCacheDimensions(SelfStudyAccomplishment document, Collection<String> dimensions) {
        dimensions.add(SelfStudyAccomplishment.cacheKeyFromId(document.getId()));
    }

    @Override
    protected String calculateDatabase(String template, SelfStudyAccomplishment document) {
        String id = document.getId();
        if (id == null || id.trim().length() == 0) {
            return null;
        }
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) {
            return null;
        }
        String month = segments[0];
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SelfStudyAccomplishment document) {
        return null;
    }

    public void generateSelfStudyHomework(String id, Long studentId, String selfStudyHomeworkId) {
        if (StringUtils.isEmpty(id) || studentId == null || StringUtils.isEmpty(selfStudyHomeworkId)) {
            return;
        }
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update = update.set("details." + studentId + ".selfStudyHomeworkId", selfStudyHomeworkId);
        UpdateOptions options = new UpdateOptions().upsert(true);
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        UpdateResult result = $executeUpdateOne(createMongoConnection(namespace, id), criteria, update, options).getUninterruptibly();
        if (result.getUpsertedId() != null) {
            String ck = SelfStudyAccomplishment.cacheKeyFromId(id);
            getCache().delete(ck);
        } else {
            updateCache(id, studentId, selfStudyHomeworkId, null);
        }
    }

    public void finishedSelfStudyHomework(String id, Long studentId, String selfStudyHomeworkId, Date finishAt) {
        if (StringUtils.isEmpty(id) || studentId == null || StringUtils.isEmpty(selfStudyHomeworkId) || finishAt == null) {
            return;
        }
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update = update.set("details." + studentId + ".selfStudyHomeworkId", selfStudyHomeworkId);
        update = update.set("details." + studentId + ".finishAt", finishAt);
        UpdateOptions options = new UpdateOptions().upsert(true);
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        UpdateResult result = $executeUpdateOne(createMongoConnection(namespace, id), criteria, update, options).getUninterruptibly();
        if (result.getUpsertedId() != null) {
            String ck = SelfStudyAccomplishment.cacheKeyFromId(id);
            getCache().delete(ck);
        } else {
            updateCache(id, studentId, selfStudyHomeworkId, finishAt);
        }
    }

    private void updateCache(String id, Long studentId, String selfStudyHomeworkId, Date finishAt) {
        getCache().createCacheValueModifier()
                .key(SelfStudyAccomplishment.cacheKeyFromId(id))
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> {
                    if (!(currentValue instanceof SelfStudyAccomplishment)) {
                        throw new UnsupportedOperationException();
                    }
                    SelfStudyAccomplishment accomplishment = (SelfStudyAccomplishment) currentValue;
                    if (accomplishment.getDetails() == null) {
                        accomplishment.setDetails(new LinkedHashMap<>());
                    }
                    SelfStudyAccomplishment.Detail detail = accomplishment.getDetails()
                            .computeIfAbsent(studentId, k -> new SelfStudyAccomplishment.Detail());
                    detail.setSelfStudyHomeworkId(selfStudyHomeworkId);
                    if (finishAt != null) {
                        detail.setFinishAt(finishAt);
                    }
                    return accomplishment;
                })
                .execute();
    }
}
