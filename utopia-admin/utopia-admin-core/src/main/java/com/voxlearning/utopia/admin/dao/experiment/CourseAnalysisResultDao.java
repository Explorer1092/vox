package com.voxlearning.utopia.admin.dao.experiment;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.entity.CourseAnalysisResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/7/18
 */
@Named
public class CourseAnalysisResultDao extends AlpsStaticMongoDao<CourseAnalysisResult, String> {

    @Override
    protected void calculateCacheDimensions(CourseAnalysisResult courseAnalysisResult, Collection<String> collection) {
    }

    /**
     * 从mongo数据库中查找所有的记录
     * 按照 endTime 降序排序
     * @return
     */
    public List<CourseAnalysisResult> findAllCourseAnalysisResult() {
        Query query = Query.query(new Criteria());
        query.with(new Sort(Sort.Direction.DESC, "updateDate"));
        return query(query);
    }

    public List<CourseAnalysisResult> findCourseAnalysisResultByGroupId(String expGroupId) {
        Query query = Query.query(Criteria.where("expGroupId").is(expGroupId));
        query.with(new Sort(Sort.Direction.DESC, "updateDate"));
        return query(query);
    }
}
