package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultCollection;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * !!! 这个表写库操作比读库操作频繁，请不要盲目使用缓存 !!!
 */
@Named
@Slf4j
public class AIUserQuestionResultCollectionDao extends AlpsStaticMongoDao<AIUserQuestionResultCollection, String> {

    @Override
    protected void calculateCacheDimensions(AIUserQuestionResultCollection document, Collection<String> dimensions) {
    }


    public void disableOld(Long userId, String qid) {

        Criteria criteria = Criteria.where("userId").is(userId).and("qid").is(qid).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);

        updateMany(createMongoConnection(), criteria, update);
    }



    public List<AIUserQuestionResultCollection> loadByUidAndLessonId4Crm(Long userId, String lessonId, LessonType... excludeLessonTypes) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("lessonId").is(lessonId);
        if (excludeLessonTypes.length > 0) {
            criteria.and("lessonType").nin(Arrays.asList(excludeLessonTypes));
        }
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AIUserQuestionResultCollection> loadByUidAndUnitId(Long userId, String unitId, LessonType... excludeLessonTypes) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("unitId").is(unitId);
        if (excludeLessonTypes.length > 0) {
            criteria.and("lessonType").nin(Arrays.asList(excludeLessonTypes));
        }
        Query query = Query.query(criteria);
        return query(query);
    }
}
