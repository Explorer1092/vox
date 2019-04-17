package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.AITodayLesson;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
@CacheBean(type = AITodayLesson.class)
public class AITodayLessonDao extends AsyncStaticMongoPersistence<AITodayLesson, String> {

    @Override
    protected void calculateCacheDimensions(AITodayLesson document, Collection<String> dimensions) {
        dimensions.add(AITodayLesson.ck_id(document.getId()));
    }

    public List<AITodayLesson> loadByBookId(String bookId) {

        Criteria criteria = Criteria.where("bookId").is(bookId);
        return query(Query.query(criteria));
    }


    public List<AITodayLesson> loadByUnitId(String unitId) {

        Criteria criteria = Criteria.where("unitId").is(unitId);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        return query(Query.query(criteria).with(sort));
    }

    public List<AITodayLesson> loadByBookIdAndUnitId(String bookId,String unitId) {

        Criteria criteria = Criteria.where("unitId").is(unitId).and("bookId").is(bookId);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        return query(Query.query(criteria).with(sort));
    }

}
