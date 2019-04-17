package com.voxlearning.utopia.service.mizar.impl.dao.settlement;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TestSchoolSettlementDao
 *
 * @author song.wang
 * @date 2017/6/28
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSchoolSettlementDao {

    @Inject
    private SchoolSettlementDao  schoolSettlementDao;

    @Test
    public void testLoadBySchoolId() throws Exception{
        Collection<SchoolSettlement> schoolSettlements = new ArrayList<>();
        SchoolSettlement schoolSettlement = new SchoolSettlement();
        schoolSettlement.initData();
        schoolSettlement.setSchoolId(527836L);
        schoolSettlement.setSchoolName("测试学校");
        schoolSettlement.setMonth(201706);
        schoolSettlement.setSettlementDay(20170627);

        schoolSettlements.add(schoolSettlement);

        SchoolSettlement schoolSettlement2 = new SchoolSettlement();
        schoolSettlement2.initData();
        schoolSettlement2.setSchoolId(527837L);
        schoolSettlement2.setSchoolName("测试学校2");
        schoolSettlement2.setMonth(201705);
        schoolSettlement2.setSettlementDay(20170525);

        schoolSettlements.add(schoolSettlement2);

        schoolSettlementDao.inserts(schoolSettlements);

        SchoolSettlement t = schoolSettlementDao.loadBySchoolId(527836L, 201706);
        System.out.println(t.getSchoolName() + "   " + t.getBasicSettlementAmount());



    }
}
