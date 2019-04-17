package com.voxlearning.utopia.service.newhomework.impl.dao.bonus;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * @author lei.liu
 * @version 18-11-6
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class AbilityExamDaoTest {

    @Inject private AbilityExamBasicDao abilityExamBasicDao;
    @Inject private AbilityExamQuestionDao abilityExamQuestionDao;
    @Inject private AbilityExamAnswerDao abilityExamAnswerDao;


    @Test
    public void test() {
        abilityExamAnswerDao.load("30010");
    }

}
