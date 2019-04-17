package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuesong.zhang
 * @since 2017/10/27
 */
@DropMongoDatabase
public class TestSubHomeworkResultAnswerDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testInsert() throws Exception {
        String id = "20180111|201801_5a57551e777487bacefd6b5b_1|333908475|INTELLIGENCE_EXAM|Q_10209387508002-3";
        String processId = "5a5755b6af81a338587567c8-1515672862196";

        SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
        aid.setDay("20180111");
        aid.setHid("201801_5a57551e777487bacefd6b5b_1");
        aid.setUserId("333908475");
        aid.setType(ObjectiveConfigType.INTELLIGENCE_EXAM);
        aid.setQuestionId("Q_10209387508002-3");

        SubHomeworkResultAnswer answer = new SubHomeworkResultAnswer();
        answer.setId(aid.toString());
        answer.setProcessId(processId);
        answer.setIsOral(Boolean.FALSE);

        subHomeworkResultAnswerDao.insert(answer);
        answer = subHomeworkResultAnswerDao.load(id);
        Assert.assertEquals(answer.getProcessId(), processId);
    }
}
