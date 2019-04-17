package com.voxlearning.utopia.service.newhomework.impl.dao.livecast;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author xuesong.zhang
 * @since 2016/12/23
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestLiveCastHomeworkProcessResultDao {

    @Inject private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;

    @Test
    @MockBinder(
            type = LiveCastHomeworkProcessResult.class,
            jsons = {
                    "{'id':'569efb8dd0c48b25de63d331-1453253106621','subject':'MATH','homeworkId':'h_1','questionId':'q_1','userId':3009}",
            },
            persistence = LiveCastHomeworkProcessResultDao.class
    )
    public void testUpdateCorrection() throws Exception {
        String id = "569efb8dd0c48b25de63d331-1453253106621";
        String qid = "q_1";
        String hid = "h_1";
        Long userId = 3009L;
        Double score = 30.25D;
        String teacherRemark = "做的还不错";
        String correctionImg = "www.163.com";

        liveCastHomeworkProcessResultDao.updateCorrection(id, hid, qid, userId, score, teacherRemark, correctionImg,"",0D);
        LiveCastHomeworkProcessResult result = liveCastHomeworkProcessResultDao.load(id);
        assertEquals(result.getScore(), score);
        assertEquals(result.getTeacherMark(), teacherRemark);
        assertEquals(result.getCorrectionImg(), correctionImg);

        score = 35.25D;
        teacherRemark = "222";
        correctionImg = "www.17zuoye.com";

        liveCastHomeworkProcessResultDao.updateCorrection(id, hid, qid, userId, score, teacherRemark, correctionImg,"",0D);
        result = liveCastHomeworkProcessResultDao.load(id);
        assertEquals(result.getScore(), score);
        assertEquals(result.getTeacherMark(), teacherRemark);
        assertEquals(result.getCorrectionImg(), correctionImg);

        score = -1D;
        teacherRemark = "222";
        correctionImg = "www.17zuoye.com";

        liveCastHomeworkProcessResultDao.updateCorrection(id, hid, qid, userId, score, teacherRemark, correctionImg,"",0D);
        result = liveCastHomeworkProcessResultDao.load(id);
        assertNotEquals(result.getScore(), score);
        assertEquals(result.getTeacherMark(), teacherRemark);
        assertEquals(result.getCorrectionImg(), correctionImg);

    }
}
