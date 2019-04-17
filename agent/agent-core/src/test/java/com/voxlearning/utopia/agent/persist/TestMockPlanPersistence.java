package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

/**
 * @Author: peng.zhang
 * @Date: 2018/8/6 19:44
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ExamPlanEntity.class)
public class TestMockPlanPersistence {

}
