package com.voxlearning.utopia.admin.dao.experiment;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.entity.CoursePageAnalysisResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Named
public class CoursePageAnalysisResultDao extends AlpsStaticMongoDao<CoursePageAnalysisResult, String> {
    @Override
    protected void calculateCacheDimensions(CoursePageAnalysisResult coursePageAnalysisResult, Collection<String> collection) {

    }

    public List<CoursePageAnalysisResult> findCoursePageAnalysisResult(String seriesId, String bookId, String unitId, String sectionId,
                                                                       String variantId, String courseId, String preId, String postId) {
        Criteria criteria = Criteria.where("series_id").is(seriesId).and("book_id").is(bookId).and("unit_id").is(unitId).and("section_id").is(sectionId)
                .and("variant_id").is(variantId).and("course_id").is(courseId).and("pre_id").is(preId).and("post_id").is(postId);
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.DESC,"course_id")).with(new Sort(Sort.Direction.ASC, "page"));
        return query(query);
    }
}
