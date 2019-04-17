/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.business.api.entity.RSEnglishSchoolHomeworkBehaviorStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author changyuan.liu
 * @since 2015/3/19
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSEnglishSchoolHomeworkBehaviorStatDao {

    @Autowired private RSEnglishSchoolHomeworkBehaviorStatDao rsEnglishSchoolHomeworkBehaviorStatDao;

    @Test
    public void testFindBySchoolIds_withoutIdSet() throws Exception {
        RSEnglishSchoolHomeworkBehaviorStat stat1 = new RSEnglishSchoolHomeworkBehaviorStat();
        stat1.setId("2015_2_1");
        stat1.setSchoolId("1");
        rsEnglishSchoolHomeworkBehaviorStatDao.insert(stat1);

        RSEnglishSchoolHomeworkBehaviorStat stat2 = new RSEnglishSchoolHomeworkBehaviorStat();
        stat2.setId("2015_2_2");
        stat2.setSchoolId("2");
        rsEnglishSchoolHomeworkBehaviorStatDao.insert(stat2);

        List<RSEnglishSchoolHomeworkBehaviorStat> result = rsEnglishSchoolHomeworkBehaviorStatDao.findBySchoolIds_withoutIdSet(Arrays.asList("1", "2"), 2015, Term.下学期);
        assertTrue(result.size() == 2);
    }

    @Test
    public void testFindSchoolDataByCityCodes_withoutIdSet() {
        RSEnglishSchoolHomeworkBehaviorStat stat1 = new RSEnglishSchoolHomeworkBehaviorStat();
        stat1.setId("2015_1_1");
        stat1.setCcode(1L);
        rsEnglishSchoolHomeworkBehaviorStatDao.insert(stat1);

        RSEnglishSchoolHomeworkBehaviorStat stat2 = new RSEnglishSchoolHomeworkBehaviorStat();
        stat2.setId("2015_1_2");
        stat2.setCcode(2L);
        rsEnglishSchoolHomeworkBehaviorStatDao.insert(stat2);

        List<RSEnglishSchoolHomeworkBehaviorStat> result = rsEnglishSchoolHomeworkBehaviorStatDao.findSchoolDataByCityCodes_withoutIdSet(Arrays.asList(1L, 2L), 2015, Term.上学期);
        assertTrue(result.size() == 2);
    }

    @Test
    public void testFindSchoolDataByAreaCodes_withoutIdSet() {
        RSEnglishSchoolHomeworkBehaviorStat stat1 = new RSEnglishSchoolHomeworkBehaviorStat();
        stat1.setId("2015_1_1");
        stat1.setAcode(1L);
        rsEnglishSchoolHomeworkBehaviorStatDao.insert(stat1);

        RSEnglishSchoolHomeworkBehaviorStat stat2 = new RSEnglishSchoolHomeworkBehaviorStat();
        stat2.setId("2015_1_2");
        stat2.setAcode(2L);
        rsEnglishSchoolHomeworkBehaviorStatDao.insert(stat2);

        List<RSEnglishSchoolHomeworkBehaviorStat> result = rsEnglishSchoolHomeworkBehaviorStatDao.findSchoolDataByAreaCodes_withoutIdSet(Arrays.asList(1L, 2L), 2015, Term.上学期);
        assertTrue(result.size() == 2);
    }

    @Test
    public void testUpdateStudentAndTeacherDataById() throws Exception {
        List<Long> stuIds = Arrays.asList(1L, 2L, 3L);
        List<Long> teacherIds = Arrays.asList(4L, 5L, 6L);

        RSEnglishSchoolHomeworkBehaviorStat rsEngSchoolHomeworkBehaviorStat = new RSEnglishSchoolHomeworkBehaviorStat();
        rsEngSchoolHomeworkBehaviorStat.setId("2015_1_1");
        rsEngSchoolHomeworkBehaviorStat.setSchoolId("1");
        rsEngSchoolHomeworkBehaviorStat.setSchoolName("test school");
        rsEngSchoolHomeworkBehaviorStat.setAcode(2L);
        rsEngSchoolHomeworkBehaviorStat.setAreaName("test area");
        rsEngSchoolHomeworkBehaviorStat.setCcode(3L);
        rsEngSchoolHomeworkBehaviorStat.setCityName("test city");
        rsEngSchoolHomeworkBehaviorStat.setPcode(4L);
        rsEngSchoolHomeworkBehaviorStat.setProvinceName("test province");
        rsEngSchoolHomeworkBehaviorStat.setStuIds(new HashSet<>(stuIds));
        rsEngSchoolHomeworkBehaviorStat.setTeacherIds(new HashSet<>(teacherIds));
        rsEngSchoolHomeworkBehaviorStat.setStuNum(3);
        rsEngSchoolHomeworkBehaviorStat.setStuTimes(3L);
        rsEngSchoolHomeworkBehaviorStat.setTeacherNum(3);
        rsEngSchoolHomeworkBehaviorStat.setTeacherTimes(3L);
        rsEngSchoolHomeworkBehaviorStat.setCreateAt(new Date());

        String id = rsEnglishSchoolHomeworkBehaviorStatDao.insert(rsEngSchoolHomeworkBehaviorStat);

        rsEnglishSchoolHomeworkBehaviorStatDao.updateStudentAndTeacherDataById("1",
                Arrays.asList(3L, 4L, 5L), Arrays.asList(6L, 5L, 7L), 3L, 3L, 2015, Term.上学期);

        RSEnglishSchoolHomeworkBehaviorStat ret = rsEnglishSchoolHomeworkBehaviorStatDao.load(id);
        assertTrue(ret != null);
        assertEquals(6, (long)ret.getStuTimes());
        assertEquals(6, (long)ret.getTeacherTimes());
        assertEquals(5, ret.getStuIds().size());
        assertEquals(4, ret.getTeacherIds().size());
        assertEquals(5, (long)ret.getStuNum());
        assertEquals(4, (long)ret.getTeacherNum());
    }
}
