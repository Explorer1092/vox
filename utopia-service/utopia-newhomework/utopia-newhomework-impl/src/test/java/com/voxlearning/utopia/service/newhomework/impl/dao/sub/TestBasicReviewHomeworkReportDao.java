package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkReport;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

/**
 * @author guoqiang.li
 * @since 2017/11/16
 */
@DropMongoDatabase
public class TestBasicReviewHomeworkReportDao extends NewHomeworkUnitTestSupport {
    @Test
    public void testCreateBasicReviewHomeworkReport() throws Exception {
        DayRange day = DayRange.current();
        BasicReviewHomeworkReport basicReviewHomeworkReport = new BasicReviewHomeworkReport();
        String id = new BasicReviewHomeworkReport.ID(day.toString(), Subject.ENGLISH, RandomUtils.nextObjectId(), "111").toString();
        basicReviewHomeworkReport.setId(id);
        basicReviewHomeworkReportDao.insert(basicReviewHomeworkReport);
    }
}
