package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Summer on 2018/3/27
 */
@Named
@CacheBean(type = AIUserLessonResultHistory.class)
public class AIUserLessonResultHistoryDao extends AsyncStaticMongoPersistence<AIUserLessonResultHistory, String> {
    @Override
    protected void calculateCacheDimensions(AIUserLessonResultHistory document, Collection<String> dimensions) {
        dimensions.add(AIUserLessonResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
        dimensions.add(AIUserLessonResultHistory.ck_userId_lessonId(document.getUserId(), document.getLessonId()));
    }

    @CacheMethod
    public List<AIUserLessonResultHistory> loadByUserIdAndUnitId(@CacheParameter("UID") Long userId, @CacheParameter("UNIT_ID") String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<AIUserLessonResultHistory> loadByUserIdAndUnitIdWithDisabled( Long userId,  String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId);
        return query(Query.query(criteria));
    }

    @Override
    @Deprecated
    public AIUserLessonResultHistory load(String s) {
        return super.load(s);
    }


    public AIUserLessonResultHistory loadById(String id) {
       return $load(id).getUninterruptibly();
    }

    @CacheMethod
    public AIUserLessonResultHistory load(@CacheParameter("UID") Long userId, @CacheParameter("LESSON_ID") String lessonId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("lessonId").is(lessonId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<AIUserLessonResultHistory> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void disableOld(Long userId, String lessonId) {

        Criteria criteria = Criteria.where("userId").is(userId).and("lessonId").is(lessonId).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);

        $executeUpdateMany(createMongoConnection(), criteria, update);
        cleanCache(userId, lessonId);
    }


    private void cleanCache(Long userId, String lessonId) {
        AIUserLessonResultHistory document = load(userId, lessonId);
        if (document == null) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(AIUserLessonResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
        cacheIds.add(AIUserLessonResultHistory.ck_userId_lessonId(document.getUserId(), document.getLessonId()));
        getCache().deletes(cacheIds);

    }
}
