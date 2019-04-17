package com.voxlearning.utopia.service.ai.impl.persistence;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by Summer on 2018/3/27
 * 此类只能自己写update方法，用父类的upsert，load，会有问题
 */
@Named
@CacheBean(type = AIUserQuestionResultHistory.class)
public class AIUserQuestionResultHistoryDao extends AsyncDynamicMongoPersistence<AIUserQuestionResultHistory, String> {

    @Override
    protected String calculateDatabase(String template, AIUserQuestionResultHistory document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, AIUserQuestionResultHistory document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());
        long mod;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            mod = document.getUserId() % 2;
        } else {
            mod = document.getUserId() % 50;
        }
        return StringUtils.formatMessage(template, mod);

    }

    @Override
    protected void calculateCacheDimensions(AIUserQuestionResultHistory document, Collection<String> dimensions) {
        dimensions.add(AIUserQuestionResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
        dimensions.add(AIUserQuestionResultHistory.ck_userId_lessonId(document.getUserId(), document.getLessonId()));
        dimensions.add(AIUserQuestionResultHistory.ck_userId_qid(document.getUserId(), document.getQid()));
    }

    @CacheMethod
    public AIUserQuestionResultHistory loadByUidAndQid(@CacheParameter("UID") Long userId, @CacheParameter("QID") String qid) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("qid").is(qid)
                .and("disabled").is(false);
        return $executeQuery(createMongoConnection(getDocumentTableName(userId)),
                Query.query(criteria)).getUninterruptibly().stream().findFirst().orElse(null);
    }

    public void upsertByUser(Long userId, AIUserQuestionResultHistory document) {
        Update update = new Update();
        getDocumentMapping().createTimeFields
                .forEach(f -> {
                    Object val = f.getAccessor().get(document);
                    if (val != null) {
                        update.setOnInsert(f.getName(), val);
                    }
                });
        getDocumentMapping().updateTimeFields
                .forEach(f -> {
                    Object val = f.getAccessor().get(document);
                    if (val != null) {
                        update.set(f.getName(), val);
                    }
                });
        getDocumentMapping().normalFields
                .forEach(f -> {
                    Object val = f.getAccessor().get(document);
                    if (val != null) {
                        update.set(f.getName(), val);
                    }
                });
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        Criteria criteria = Criteria.where("_id").is(document.getId());
        AIUserQuestionResultHistory modified = $executeFindOneAndUpdate(createMongoConnection(getDocumentTableName(userId)),
                criteria, update, options).getUninterruptibly();
        if (modified != null) {
            modifyFormCache(modified);
        }
    }

    private MongoNamespace getDocumentTableName(Long userId) {
        AIUserQuestionResultHistory mock = new AIUserQuestionResultHistory();
        mock.setUserId(userId);
        return calculateDocumentMongoNamespace(mock);
    }

    public AIUserQuestionResultHistory loadById(Long userId, String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        return $executeQuery(createMongoConnection(getDocumentTableName(userId)),
                Query.query(criteria)).getUninterruptibly().stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<AIUserQuestionResultHistory> loadByUidAndLessonId(@CacheParameter("UID") Long userId, @CacheParameter("LID") String lessonId) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("lessonId").is(lessonId)
                .and("disabled").is(false);
        return $executeQuery(createMongoConnection(getDocumentTableName(userId)), Query.query(criteria)).getUninterruptibly();
    }

    public List<AIUserQuestionResultHistory> loadByUidAndLessonId4Crm(Long userId,String lessonId,List<LessonType> excludeLessonTypes) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("lessonId").is(lessonId).nin(excludeLessonTypes);
        return $executeQuery(createMongoConnection(getDocumentTableName(userId)), Query.query(criteria)).getUninterruptibly();
    }

    @CacheMethod
    public List<AIUserQuestionResultHistory> loadByUidAndUnitId(@CacheParameter("UID") Long userId, @CacheParameter("UNIT_ID") String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("unitId").is(unitId)
                .and("disabled").is(false);
        return $executeQuery(createMongoConnection(getDocumentTableName(userId)), Query.query(criteria)).getUninterruptibly();
    }

    /**
     * 这个方法只在生成定级报告的时候用，没有缓存，慎用
     * @param userId
     * @param lessonType
     * @return
     */
    public List<AIUserQuestionResultHistory> loadByUidAndType(Long userId, LessonType lessonType) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("lessonType").is(lessonType.name());
        return $executeQuery(createMongoConnection(getDocumentTableName(userId)), Query.query(criteria)).getUninterruptibly();
    }

    public void updateUserVideo(AIUserQuestionResultHistory document, String video) {
        Criteria criteria = Criteria.where("_id").is(document.getId());
        Update update = new Update();
        update.set("updateDate", new Date()).set("userVideo", video);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        AIUserQuestionResultHistory modified = $executeFindOneAndUpdate(createMongoConnection(getDocumentTableName(document.getUserId())),
                criteria, update, options).getUninterruptibly();
        if (modified != null) {
            modifyFormCache(modified);
        }
    }

    public AIUserQuestionResultHistory disableOld(AIUserQuestionResultHistory oldHis) {
        Criteria criteria = Criteria.where("_id").is(oldHis.getId());
        Update update = new Update();
        update.set("updateDate", new Date());
        update.set("disabled", true);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        AIUserQuestionResultHistory modified = $executeFindOneAndUpdate(createMongoConnection(getDocumentTableName(oldHis.getUserId())),
                criteria, update, options).getUninterruptibly();
        if (modified != null) {
            disableFormCache(modified);
        }
        return modified;
    }

    private void disableFormCache(AIUserQuestionResultHistory modified) {
        // cache
        String ck_userId_lessonId = AIUserQuestionResultHistory.ck_userId_lessonId(modified.getUserId(), modified.getLessonId());
        _disableCache(ck_userId_lessonId, modified);

        // cache
        String ck_userId_unitId = AIUserQuestionResultHistory.ck_userId_unitId(modified.getUserId(), modified.getUnitId());
        _disableCache(ck_userId_unitId, modified);

        // cache
        String ck_userId_qid = AIUserQuestionResultHistory.ck_userId_qid(modified.getUserId(), modified.getQid());
        _disableCache(ck_userId_qid, modified);
    }

    private void _disableCache(String key, AIUserQuestionResultHistory modified) {
        if (getCache().load(key) != null) {
            ChangeCacheObject<List<AIUserQuestionResultHistory>> modifier = currents -> {
                currents.stream().filter(e -> Objects.equals(e.getId(), modified.getId())).findAny().ifPresent(currents::remove);
                return currents;
            };
            CacheValueModifierExecutor<List<AIUserQuestionResultHistory>> executor = getCache().createCacheValueModifier();
            executor.key(key)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(modifier)
                    .execute();
        }
    }

    private void modifyFormCache(AIUserQuestionResultHistory modified) {
        // cache
        String ck_userId_lessonId = AIUserQuestionResultHistory.ck_userId_lessonId(modified.getUserId(), modified.getLessonId());
        _modifyCache(ck_userId_lessonId, modified);

        // cache
        String ck_userId_unitId = AIUserQuestionResultHistory.ck_userId_unitId(modified.getUserId(), modified.getUnitId());
        _modifyCache(ck_userId_unitId, modified);

        // cache
        String ck_userId_qid = AIUserQuestionResultHistory.ck_userId_qid(modified.getUserId(), modified.getQid());
        _modifyCache(ck_userId_qid, modified);
    }
    private void _modifyCache(String key, AIUserQuestionResultHistory modified) {
        if (getCache().load(key) != null) {
            ChangeCacheObject<List<AIUserQuestionResultHistory>> modifier = currents -> {
                currents.stream().filter(e -> Objects.equals(e.getId(), modified.getId())).findAny().ifPresent(currents::remove);
                currents.add(modified);
                return currents;
            };
            CacheValueModifierExecutor<List<AIUserQuestionResultHistory>> executor = getCache().createCacheValueModifier();
            executor.key(key)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(modifier)
                    .execute();
        }
    }
}
