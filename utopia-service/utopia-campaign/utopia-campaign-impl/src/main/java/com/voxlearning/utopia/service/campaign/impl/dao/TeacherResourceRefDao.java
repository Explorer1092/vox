package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherResourceRef;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherResourceRef.class)
public class TeacherResourceRefDao extends AlpsStaticJdbcDao<TeacherResourceRef, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherResourceRef document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public List<TeacherResourceRef> getByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public boolean updateCoursewareUrl(String coursewardId, String resourceUrl) {
        Criteria criteria = Criteria.where("RESOURCE_ID").is(coursewardId).and("RESOURCE_TYPE").is(TeacherResourceRef.Type.COURSEWARE.name());
        Update update = new Update().set("URL", resourceUrl);
        long line = super.executeUpdate(update, criteria, getTableName());
        if (line > 0) {
            long count = count(Query.query(criteria));
            if (count <= 100000) {
                List<TeacherResourceRef> query = query(Query.query(criteria));
                evictDocumentCache(query);
            } else {
                // 不更新缓存,等待当天缓存过期
            }
        }
        return line > 0;
    }

}
