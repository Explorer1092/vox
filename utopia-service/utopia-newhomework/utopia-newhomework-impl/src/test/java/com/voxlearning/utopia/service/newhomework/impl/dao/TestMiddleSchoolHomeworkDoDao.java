package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomeworkDo;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @author xuesong.zhang
 * @since 2016/8/5
 */
@DropMongoDatabase
public class TestMiddleSchoolHomeworkDoDao extends NewHomeworkUnitTestSupport {

    @Inject MiddleSchoolHomeworkDoDao middleSchoolHomeworkDodao;

    @Test
    @MockBinder(
            type = MiddleSchoolHomeworkDo.class,
            jsons = {
                    "{'id':'20160101-ENGLISH-123-1','homeworkId':'123'}",
                    "{'id':'20160101-ENGLISH-123-2','homeworkId':'123'}",
                    "{'id':'20160101-ENGLISH-123-3','homeworkId':'123'}",
                    "{'id':'20160101-ENGLISH-123-4','homeworkId':'123'}",
                    "{'id':'20160101-ENGLISH-123-5','homeworkId':'123'}"
            },
            persistence = MiddleSchoolHomeworkDoDao.class
    )
    public void testFindByMiddleSchoolHomework() throws Exception {
        MiddleSchoolHomeworkDo homeworkDo = middleSchoolHomeworkDodao.load("20160101-ENGLISH-123-1");
        Assert.assertEquals("123", homeworkDo.getHomeworkId());
    }
}
