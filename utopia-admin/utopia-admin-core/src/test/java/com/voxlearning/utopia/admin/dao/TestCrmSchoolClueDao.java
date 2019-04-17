package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.CrmSchoolClueExternOrBoarder;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

/**
 * CrmSchoolClueDao的测试类
 * Created by yaguang.wang on 2017/1/4.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestCrmSchoolClueDao {
    @Inject
    private CrmSchoolClueDao crmSchoolClueDao;

    @Test
    public void testLoad() {
        CrmSchoolClue clue = new CrmSchoolClue();
        clue.setShowExternOrBoarder(CrmSchoolClueExternOrBoarder.EXTERN);
        clue.setSchoolId(1L);
        List<CrmSchoolClue> crmSchoolClues = crmSchoolClueDao.findBySchoolId(1L);
        clue = crmSchoolClues.get(0);
        System.out.println(clue.getShowExternOrBoarder());
    }
}
