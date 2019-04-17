package com.voxlearning.utopia.admin.dao.experiment;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.entity.BookVariantCourseAnalysisResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Named
public class BookVariantCourseAnalysisResultDao extends AlpsStaticMongoDao<BookVariantCourseAnalysisResult, String> {
    @Override
    protected void calculateCacheDimensions(BookVariantCourseAnalysisResult bookBariantCourseAnalysisResult, Collection<String> collection) {

    }

    public List<BookVariantCourseAnalysisResult> finaBookVariantCourseAnalysisResultBySeriesIdBookId(String seriesId, String bookId) {
        Criteria criteria = Criteria.where("series_id").is(seriesId).and("book_id").is(bookId);
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "series_id"));
        return query(query);
    }
}
