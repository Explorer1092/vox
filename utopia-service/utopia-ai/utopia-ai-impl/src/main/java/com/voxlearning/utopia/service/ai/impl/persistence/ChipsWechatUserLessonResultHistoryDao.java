package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserLessonResultHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
@CacheBean(type = ChipsWechatUserLessonResultHistory.class)
public class ChipsWechatUserLessonResultHistoryDao extends AsyncStaticMongoPersistence<ChipsWechatUserLessonResultHistory, String> {
    @Override
    protected void calculateCacheDimensions(ChipsWechatUserLessonResultHistory document, Collection<String> dimensions) {
        dimensions.add(ChipsWechatUserLessonResultHistory.ck_userId_lessonId(document.getUserId(), document.getLessonId()));
        dimensions.add(ChipsWechatUserLessonResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
    }

    @Override
    public ChipsWechatUserLessonResultHistory load(String s) {
        return super.$load(s).getUninterruptibly();
    }

    @CacheMethod
    public ChipsWechatUserLessonResultHistory load(@CacheParameter("UID") Long user, @CacheParameter("LESSON_ID") String lessonId) {
        Criteria criteria = Criteria.where("userId").is(user).and("lessonId").is(lessonId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<ChipsWechatUserLessonResultHistory> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @CacheMethod
    public List<ChipsWechatUserLessonResultHistory> loadByUnit(@CacheParameter("UID") Long user, @CacheParameter("UNIT_ID")String unitId) {
        Criteria criteria = Criteria.where("userId").is(user).and("unitId").is(unitId).and("disabled").is(false);
        return query(Query.query(criteria));
    }


    public void disableOld(Long userId, String lessonId, String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("lessonId").is(lessonId).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);

        $executeUpdateMany(createMongoConnection(), criteria, update);
        cleanCache(userId, lessonId, unitId);
    }

    private void cleanCache(Long userId, String lessonId, String unitId) {
        Set<String> cacheSet = new HashSet<>();
        cacheSet.add(ChipsWechatUserLessonResultHistory.ck_userId_lessonId(userId, lessonId));
        cacheSet.add(ChipsWechatUserLessonResultHistory.ck_userId_unitId(userId, unitId));
        getCache().delete(cacheSet);
    }
}
