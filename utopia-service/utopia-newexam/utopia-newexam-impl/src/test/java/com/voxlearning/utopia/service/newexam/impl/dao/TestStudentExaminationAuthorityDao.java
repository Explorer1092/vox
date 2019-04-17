package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newexam.api.entity.StudentExaminationAuthority;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamUnitTestSupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@DropMongoDatabase
public class TestStudentExaminationAuthorityDao extends NewExamUnitTestSupport {

    @Test
    public void testLoadStudentExaminationAuthority() throws Exception {
        StudentExaminationAuthority studentExaminationAuthority = new StudentExaminationAuthority();
        studentExaminationAuthority.setId("201606-ENGLISH-E_10300000012149-363874564");
        studentExaminationAuthority.setDisabled(false);
        studentExaminationAuthorityDao.insert(studentExaminationAuthority);
        studentExaminationAuthority = studentExaminationAuthorityDao.load("201606-ENGLISH-E_10300000012149-363874564");
        assertEquals(studentExaminationAuthority.getId(), "201606-ENGLISH-E_10300000012149-363874564");
        assertEquals(studentExaminationAuthority.getDisabled(), false);
    }

    @Test
    public void testUpdateStudentExaminationAuthorityDisabled() throws Exception {
        StudentExaminationAuthority studentExaminationAuthority = new StudentExaminationAuthority();
        studentExaminationAuthority.setId("201606-ENGLISH-E_10300000012149-363874564");
        studentExaminationAuthority.setDisabled(false);
        studentExaminationAuthorityDao.insert(studentExaminationAuthority);
        studentExaminationAuthorityDao.updateStudentExaminationAuthorityDisabled("201606-ENGLISH-E_10300000012149-363874564", true);
        studentExaminationAuthority = studentExaminationAuthorityDao.load("201606-ENGLISH-E_10300000012149-363874564");
        assertEquals(studentExaminationAuthority.getId(), "201606-ENGLISH-E_10300000012149-363874564");
        assertEquals(studentExaminationAuthority.getDisabled(), true);
    }


}
