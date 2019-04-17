package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.crm.CrmMainSubAccountApply;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Yuechen.Wang on 2016/7/28.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestCrmMainSubAccountApplyDao {

    @Inject private CrmMainSubAccountApplyDao crmMainSubAccountApplyDao;

    @Test
    public void testFindByPage() throws Exception {
        List<CrmMainSubAccountApply> mockList = new ArrayList<>();
        for (int i = 0; i < 13; ++i) {
            CrmMainSubAccountApply mock = new CrmMainSubAccountApply();
            mock.setTeacherId(1L);
            mock.setApplicantId((long) i);
            mock.setTeacherId((long) i);
            mock.setDisabled(false);
            mockList.add(mock);
        }
        crmMainSubAccountApplyDao.inserts(mockList);

        Pageable pageable1 = new PageRequest(0, 5);
        Page<CrmMainSubAccountApply> page1 = crmMainSubAccountApplyDao.findByPage(pageable1, 1L, null, null, null, null, null);
        assertEquals(5, page1.getContent().size());
        assertEquals(3, page1.getTotalPages());
        FlightRecorder.dot("==============================");
        Pageable pageable2 = new PageRequest(2, 3);
        Page<CrmMainSubAccountApply> page2 = crmMainSubAccountApplyDao.findByPage(pageable2, 1L, null, null, null, null, null);
        assertEquals(3, page2.getContent().size());
        assertEquals(5, page2.getTotalPages());
    }

    @Test
    public void testFindByApplicant() throws Exception {
        for (int i = 0; i < 5; ++i) {
            CrmMainSubAccountApply mock = new CrmMainSubAccountApply();
            mock.setApplicantId((long) i);
            mock.setTeacherId((long) i);
            mock.setDisabled(i % 2 == 0);
            crmMainSubAccountApplyDao.insert(mock);
        }
        List<CrmMainSubAccountApply> records = crmMainSubAccountApplyDao.findByApplicant(Arrays.asList(0L, 2L, 3L));
        assertEquals(1, records.size());
    }

    @Test
    public void testFindByTeacherId() throws Exception {
        for (int i = 0; i < 5; ++i) {
            CrmMainSubAccountApply mock = new CrmMainSubAccountApply();
            mock.setApplicantId((long) i);
            mock.setTeacherId(1L);
            mock.setDisabled(i % 2 == 0);
            crmMainSubAccountApplyDao.insert(mock);
        }
        List<CrmMainSubAccountApply> records = crmMainSubAccountApplyDao.findByTeacherId(1L);
        assertEquals(2, records.size());
    }

    @Test
    public void testFindByPeriod() throws Exception {
        Date date = DateUtils.stringToDate("2016-04-03", DateUtils.FORMAT_SQL_DATE);
        for (int i = 0; i < 10; ++i) {
            CrmMainSubAccountApply mock = new CrmMainSubAccountApply();
            mock.setDisabled(false);
            mock.setCreateTime(DateUtils.calculateDateDay(date, i));
            crmMainSubAccountApplyDao.insert(mock);
        }
        Date start = DateUtils.stringToDate("2016-04-03 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        Date end = DateUtils.stringToDate("2016-04-03 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        List<CrmMainSubAccountApply> records = crmMainSubAccountApplyDao.findByPeriod(start, end);
        assertEquals(1, records.size());
        end = DateUtils.stringToDate("2016-04-07 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        FlightRecorder.dot("==============================");
        records = crmMainSubAccountApplyDao.findByPeriod(start, end);
        assertEquals(5, records.size());
    }
}