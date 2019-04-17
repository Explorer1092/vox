package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.AiChipsEnglishTeacher;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/11/7
 */
@Named
@CacheBean(type = AiChipsEnglishTeacher.class)
public class AiChipsEnglishTeacherDao extends AsyncStaticMongoPersistence<AiChipsEnglishTeacher, String> {

    @Override
    protected void calculateCacheDimensions(AiChipsEnglishTeacher aiChipsEnglishTeacher, Collection<String> collection) {
        collection.add(AiChipsEnglishTeacher.ck_name(aiChipsEnglishTeacher.getName()));
    }

    public List<AiChipsEnglishTeacher> loadAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AiChipsEnglishTeacher> loadByName(@CacheParameter(value = "name") String name) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("name").is(name);
        return query(Query.query(criteria));
    }

}
