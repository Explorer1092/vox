package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathCourse;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TobbitMathCourseDao extends AlpsStaticMongoDao<TobbitMathCourse, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathCourse document, Collection<String> dimensions) {
        dimensions.add(TobbitMathCourse.ck_id(document.getId()));
    }

    @CacheMethod
    public List<TobbitMathCourse> load() {
        Criteria criteria = Criteria.where("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.ASC, "seq");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

}
