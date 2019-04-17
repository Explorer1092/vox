package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.admin.entity.CrmSchoolEvaluate;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 学校评分
 * Created by yaguang.wang on 2017/1/6.
 */
@Named
public class CrmSchoolEvaluateDao extends AlpsStaticMongoDao<CrmSchoolEvaluate, String> {
    @Override
    protected void calculateCacheDimensions(CrmSchoolEvaluate document, Collection<String> dimensions) {
        dimensions.add(CrmSchoolEvaluate.ck_sid(document.getSchoolId()));
    }

    @CacheMethod
    public List<CrmSchoolEvaluate> findBySchoolId(@CacheParameter("sid") Long schoolId) {
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        return query(Query.query(criteria));
    }

}
