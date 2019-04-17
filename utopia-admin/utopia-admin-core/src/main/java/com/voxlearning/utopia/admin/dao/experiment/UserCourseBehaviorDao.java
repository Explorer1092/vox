package com.voxlearning.utopia.admin.dao.experiment;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.entity.UserCourseBehavior;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/7/18
 */
@Named
public class UserCourseBehaviorDao extends AlpsStaticMongoDao<UserCourseBehavior,String>{

    @Override
    protected void calculateCacheDimensions(UserCourseBehavior userCourseBehavior, Collection<String> collection) {
    }

    /**
     * 根据实验id和课程id查询所有
     * 按照page 升序
     * @return
     */
    public List<UserCourseBehavior> findUserCourseBehaviorByExpGroupIdAndExpIdAndCourseId(String expGroupId,String expId, String courseId) {
        Criteria criteria = Criteria.where("expGroupId").is(expGroupId).and("expId").is(expId).and("courseId").is(courseId);
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "page"));
        return query(query);
    }

}
