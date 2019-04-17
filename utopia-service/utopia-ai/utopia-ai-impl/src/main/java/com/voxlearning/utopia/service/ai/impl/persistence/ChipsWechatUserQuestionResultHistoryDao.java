package com.voxlearning.utopia.service.ai.impl.persistence;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserQuestionResultHistory;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsWechatUserQuestionResultHistory.class)
public class ChipsWechatUserQuestionResultHistoryDao extends AsyncStaticMongoPersistence<ChipsWechatUserQuestionResultHistory, String> {

    @Override
    protected void calculateCacheDimensions(ChipsWechatUserQuestionResultHistory chipsWechatUserQuestionResultHistory, Collection<String> collection) {
        collection.add(ChipsWechatUserQuestionResultHistory.ck_userId_lessonId(chipsWechatUserQuestionResultHistory.getUserId(), chipsWechatUserQuestionResultHistory.getLessonId()));
        collection.add(ChipsWechatUserQuestionResultHistory.ck_userId_qid(chipsWechatUserQuestionResultHistory.getUserId(), chipsWechatUserQuestionResultHistory.getQid()));
        collection.add(ChipsWechatUserQuestionResultHistory.ck_userId_unitId(chipsWechatUserQuestionResultHistory.getUserId(), chipsWechatUserQuestionResultHistory.getUnitId()));
    }

    public void disabled(ChipsWechatUserQuestionResultHistory questionResultHistory) {
        Criteria criteria = Criteria.where("_id").is(questionResultHistory.getId());
        Update update = new Update();
        update.set("updateDate", new Date());
        update.set("disabled", true);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        ChipsWechatUserQuestionResultHistory modified = $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            cleanCache(modified);
        }

    }

    @CacheMethod
    public List<ChipsWechatUserQuestionResultHistory> loadByLessonId(@CacheParameter("UID") Long user, @CacheParameter("LID") String lessonId) {
        Criteria criteria = Criteria.where("userId").is(user).and("lessonId").is(lessonId).and("disabled").is(false);
        return query(Query.query(criteria));
    }


    @CacheMethod
    public List<ChipsWechatUserQuestionResultHistory> loadByUnitId(@CacheParameter("UID") Long user, @CacheParameter("UNIT_ID") String unitId) {
        Criteria criteria = Criteria.where("userId").is(user).and("unitId").is(unitId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    private void cleanCache(ChipsWechatUserQuestionResultHistory history) {
        Set<String> collection = new HashSet<>();
        collection.add(ChipsWechatUserQuestionResultHistory.ck_userId_lessonId(history.getUserId(), history.getLessonId()));
        collection.add(ChipsWechatUserQuestionResultHistory.ck_userId_qid(history.getUserId(), history.getQid()));
        collection.add(ChipsWechatUserQuestionResultHistory.ck_userId_unitId(history.getUserId(), history.getUnitId()));
        getCache().delete(collection);
    }
}
