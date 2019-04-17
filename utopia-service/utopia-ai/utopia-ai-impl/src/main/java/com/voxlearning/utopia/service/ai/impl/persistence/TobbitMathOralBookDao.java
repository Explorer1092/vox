package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathOralBook;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TobbitMathOralBookDao extends AlpsStaticMongoDao<TobbitMathOralBook, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathOralBook document, Collection<String> dimensions) {
        dimensions.add(TobbitMathOralBook.ck_id(document.getId()));
    }


    @CacheMethod
    public List<TobbitMathOralBook> load() {
        Criteria criteria = Criteria.where("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }


}
