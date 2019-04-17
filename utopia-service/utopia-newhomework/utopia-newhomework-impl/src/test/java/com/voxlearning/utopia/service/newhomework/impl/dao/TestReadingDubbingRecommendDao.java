package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;

import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.ReadingDubbingRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

@DropMongoDatabase
public class TestReadingDubbingRecommendDao extends NewHomeworkUnitTestSupport {
    @Test
    @MockBinder(type = ReadingDubbingRecommend.class,
            jsons = {
                    "{'id':'1'}"
            },
            persistence = ReadingDubbingRecommendDao.class)
    public void testInsert() {
        ReadingDubbingRecommend readingDubbingRecommend = new ReadingDubbingRecommend();
        readingDubbingRecommend.setRecommendComment("Nice");
        readingDubbingRecommend.setType(ObjectiveConfigType.LEVEL_READINGS);
        ReadingDubbingRecommend.ReadingDubbing dubbing = new ReadingDubbingRecommend.ReadingDubbing();
        dubbing.setUserName("STUDENT1");
        dubbing.setUserId(12L);
        dubbing.setDuration(12L);
        dubbing.setScore(12);
        dubbing.setDubbingScoreLevel(AppOralScoreLevel.A);
        dubbing.setDubbingId("ds");List<ReadingDubbingRecommend.ReadingDubbing> readingDubbings = new LinkedList<>();
        readingDubbings.add(dubbing);
        readingDubbingRecommend.setReadingDubbings(readingDubbings);
        readingDubbingRecommend.setId("2");
        readingDubbingRecommendDao.insert(readingDubbingRecommend);
        ReadingDubbingRecommend readingDubbingRecommend1 = readingDubbingRecommendDao.load("2");
        logger.info("2");
    }
}
