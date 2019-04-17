package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2017/1/18
 */
@DropMongoDatabase
public class TestSubHomeworkProcessResultDao extends NewHomeworkUnitTestSupport {

    @Test
    @MockBinder(
            type = SubHomeworkProcessResult.class,
            jsons = {
                    "{'id':'569efb8dd0c48b25de63d331-1453253106621','subject':'MATH','homeworkId':'h_1','questionId':'q_1','userId':3009}"
            },
            persistence = SubHomeworkProcessResultAsyncDao.class
    )
    public void testUpdateCorrection() throws Exception {
        String id = "569efb8dd0c48b25de63d331-1453253106621";

        subHomeworkProcessResultDao.updateCorrection(id, true,
                CorrectType.CORRECT,
                Correction.EXCELLENT,
                "做的还不错", false);

        SubHomeworkProcessResult result = subHomeworkProcessResultDao.load(id);

        assertEquals(result.getCorrection(), Correction.EXCELLENT);


        subHomeworkProcessResultDao.updateCorrection(id, true,
                CorrectType.CORRECT,
                Correction.FAIL,
                "做错了还上传", false);
        result = subHomeworkProcessResultDao.load(id);
        assertEquals(result.getCorrection(), Correction.FAIL);


        subHomeworkProcessResultDao.updateCorrection(id, true,
                CorrectType.CORRECT,
                Correction.EXCELLENT,
                "测试一道不存在的题", false);
        result = subHomeworkProcessResultDao.load(id);
        assertEquals(result.getCorrection(), Correction.FAIL);
    }
}
