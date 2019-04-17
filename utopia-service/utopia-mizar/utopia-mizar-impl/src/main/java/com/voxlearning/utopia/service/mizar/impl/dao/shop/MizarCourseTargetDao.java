package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourseTarget;

import javax.inject.Named;
import java.util.*;

/**
 * Created by Summer Yang on 2016/9/21.
 */
@Named
@CacheBean(type = MizarCourseTarget.class)
public class MizarCourseTargetDao extends AlpsStaticMongoDao<MizarCourseTarget, String> {

    @Override
    protected void calculateCacheDimensions(MizarCourseTarget document, Collection<String> dimensions) {
        dimensions.add(MizarCourseTarget.ck_courseId(document.getCourseId()));
    }

    public long clearTarget(String courseId, Integer type) {
//        Update update = Update.update("disabled", true);  直接删除
        Criteria criteria = Criteria.where("courseId").is(courseId).and("targetType").is(type);
        long rows = executeRemove(createMongoConnection(), Query.query(criteria));
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            cacheKeys.add(MizarCourseTarget.ck_courseId(courseId));
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    @CacheMethod
    public List<MizarCourseTarget> loadByCourseId(@CacheParameter(value = "courseId") String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("courseId").is(courseId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
