package com.voxlearning.utopia.admin.dao.experiment;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.entity.BookCourseAnalysisResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Named
public class BookCourseAnalysisResultDao extends AlpsStaticMongoDao<BookCourseAnalysisResult, String> {

    @Override
    protected void calculateCacheDimensions(BookCourseAnalysisResult bookCourseAnalysisResult, Collection<String> collection) {

    }

    public List<BookCourseAnalysisResult> findAllBookCourseAnalysisResult() {
        Query query = Query.query(new Criteria());
        query.with(new Sort(Sort.Direction.DESC, "series_id"));
        return query(query);
    }

}
