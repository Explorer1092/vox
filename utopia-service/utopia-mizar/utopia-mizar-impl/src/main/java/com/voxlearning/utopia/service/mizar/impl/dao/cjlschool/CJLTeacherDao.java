package com.voxlearning.utopia.service.mizar.impl.dao.cjlschool;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataDao;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2017/07/15.
 */
@Named
@CacheBean(type = CJLTeacher.class)
public class CJLTeacherDao extends StaticCacheDimensionDocumentMongoDao<CJLTeacher, String> implements CJLDataDao<CJLTeacher> {

    @CacheMethod
    public CJLTeacher findByLoginName(@CacheParameter("L") String loginName) {
        Criteria criteria = Criteria.where("loginName").is(loginName);

        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @Override
    public CJLTeacher syncOne(CJLTeacher entity) {
        return upsert(entity);
    }

    @Override
    public void syncBatch(List<CJLTeacher> entityList) {
        entityList.forEach(this::upsert);
    }

    @Override
    public List<CJLTeacher> $findAllForJob() {
        return query();
    }

}
