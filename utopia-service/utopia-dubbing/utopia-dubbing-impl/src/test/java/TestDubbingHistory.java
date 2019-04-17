import com.voxlearning.alps.dao.mysql.support.TruncateCommonVersionTable;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.dubbing.impl.dao.DubbingHistoryDao;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Created by jiang wei on 2017/9/5.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateCommonVersionTable
@DropMongoDatabase
public class TestDubbingHistory {

    @Inject
    private DubbingHistoryDao dubbingHistoryDao;

    @Test
    public void testInsertDubbingHistory() {
        DubbingHistory dubbingHistory = new DubbingHistory();
        dubbingHistory.setUserId(11111L);
        String categoryId = new ObjectId().toString();
        dubbingHistory.setCategoryId(categoryId);
        String dubbingId = new ObjectId().toString();
        dubbingHistory.setDubbingId(dubbingId);
        dubbingHistory.setClazzId(22222L);
        dubbingHistory.setVideoUrl("ccccccccc");
        dubbingHistory.setIsPublished(Boolean.TRUE);
        dubbingHistoryDao.insert(dubbingHistory);

//        dubbingHistoryDao.getDubbingHistoryByUserId(11111L);
//        dubbingHistoryDao.getDubbingHistoryByClazzIdAndDubbingId(22222L, dubbingId);
//        dubbingHistoryDao.getDubbingHistoryCountByUserId(11111L);
//        Long dubbingHistoryCountByUserId = dubbingHistoryDao.getDubbingHistoryCountByUserId(11111L);
//        Assert.assertEquals(1L, SafeConverter.toLong(dubbingHistoryCountByUserId));
//        dubbingHistoryDao.getDubbingHistoryCountByUserIdAndDubbingIds(11111L, Collections.singleton(dubbingId));
        Long dubbingHistoryCountByUserIdAndDubbingId = dubbingHistoryDao.getDubbingHistoryCountByUserIdAndDubbingIds(11111L, Collections.singleton(dubbingId)).get(dubbingId);
        dubbingHistoryDao.getDubbingHistoryCountByUserIdAndDubbingIds(11111L, Collections.singleton(dubbingId));
//        dubbingHistoryDao.getDubbingHistoryByUserId(11111L);
//        dubbingHistoryDao.getDubbingHistoryByClazzIdAndDubbingId(22222L, dubbingId);
        Assert.assertEquals(1L, SafeConverter.toLong(dubbingHistoryCountByUserIdAndDubbingId));
        dubbingHistory.setId("");
        dubbingHistoryDao.insert(dubbingHistory);
        dubbingHistoryCountByUserIdAndDubbingId = dubbingHistoryDao.getDubbingHistoryCountByUserIdAndDubbingIds(11111L, Collections.singleton(dubbingId)).get(dubbingId);
        Assert.assertEquals(2L, SafeConverter.toLong(dubbingHistoryCountByUserIdAndDubbingId));
//        dubbingHistoryDao.getDubbingHistoryCountByUserIdAndCategoryId(11111L, Collections.singleton(categoryId));
//        Integer dubbingHistoryCountByUserIdAndCategoryId = dubbingHistoryDao.getDubbingHistoryCountByUserIdAndCategoryId(11111L, Collections.singleton(categoryId)).get(categoryId);
//        Assert.assertEquals(1, SafeConverter.toLong(dubbingHistoryCountByUserIdAndCategoryId));
    }

}
