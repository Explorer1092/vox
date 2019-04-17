package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamClazz;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamCounty;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamSchool;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamUnitTestSupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author zhangbin
 * @since 2017/10/19
 */

@DropMongoDatabase
public class TestRptMockNewExamDao extends NewExamUnitTestSupport {

    @Test
    public void testInsertStudent(){
        RptMockNewExamStudent rptMockNewExamStudent = new RptMockNewExamStudent();
        String id = "20171019|student101010|E_20300000002320|P_20300029496151";
        rptMockNewExamStudent.setId(id);
        rptMockNewExamStudent.setExamId("E_20300000002320");
        rptMockNewExamStudent.setPaperDocId("P_20300029496151");

        rptMockNewExamStudentDao.insert(rptMockNewExamStudent);
        assertEquals(rptMockNewExamStudentDao.load(id).getId(), id);
        assertEquals("E_20300000002320", rptMockNewExamStudentDao.load(id).getExamId());
    }

    @Test
    public void testInsertClass(){
        RptMockNewExamClazz rptMockNewExamClazz = new RptMockNewExamClazz();
        String id = "20171019|class101010|E_20300000002320|P_20300029496151";
        rptMockNewExamClazz.setId(id);
        rptMockNewExamClazz.setExamId("E_20300000002320");
        rptMockNewExamClazz.setPaperDocId("P_20300029496151");

        rptMockNewExamClazzDao.insert(rptMockNewExamClazz);
        assertEquals(rptMockNewExamClazzDao.load(id).getId(), id);
        assertEquals("E_20300000002320", rptMockNewExamClazzDao.load(id).getExamId());

    }

    @Test
    public void testInsertSchool(){
        RptMockNewExamSchool rptMockNewExamSchool = new RptMockNewExamSchool();
        String id = "20171019|school101010|E_20300000002320|P_20300029496151";
        rptMockNewExamSchool.setId(id);
        rptMockNewExamSchool.setExamId("E_20300000002320");
        rptMockNewExamSchool.setPaperDocId("P_20300029496151");

        rptMockNewExamSchoolDao.insert(rptMockNewExamSchool);
        assertEquals(rptMockNewExamSchoolDao.load(id).getId(), id);
        assertEquals("E_20300000002320", rptMockNewExamSchoolDao.load(id).getExamId());
    }

    @Test
    public void testInsertCounty(){
        RptMockNewExamCounty rptMockNewExamCounty = new RptMockNewExamCounty();
        String id = "20171019|county101010|E_20300000002320|P_20300029496151";
        rptMockNewExamCounty.setId(id);
        rptMockNewExamCounty.setExamId("E_20300000002320");
        rptMockNewExamCounty.setPaperDocId("P_20300029496151");

        rptMockNewExamCountyDao.insert(rptMockNewExamCounty);
        assertEquals(rptMockNewExamCountyDao.load(id).getId(), id);
        assertEquals("E_20300000002320", rptMockNewExamCountyDao.load(id).getExamId());
    }

}
