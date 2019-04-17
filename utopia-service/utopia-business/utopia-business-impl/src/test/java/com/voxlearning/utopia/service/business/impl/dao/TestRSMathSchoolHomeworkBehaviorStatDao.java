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
import com.voxlearning.utopia.service.business.api.entity.RSMathSchoolHomeworkBehaviorStat;
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
public class TestRSMathSchoolHomeworkBehaviorStatDao {

    @Autowired private RSMathSchoolHomeworkBehaviorStatDao rsMathSchoolHomeworkBehaviorStatDao;

    @Test
    public void testFindBySchoolIds_withoutIdSet() throws Exception {
        RSMathSchoolHomeworkBehaviorStat stat1 = new RSMathSchoolHomeworkBehaviorStat();
        stat1.setId("2015_1_1");
        stat1.setSchoolId("1");
        rsMathSchoolHomeworkBehaviorStatDao.insert(stat1);

        RSMathSchoolHomeworkBehaviorStat stat2 = new RSMathSchoolHomeworkBehaviorStat();
        stat2.setId("2015_1_2");
        stat2.setSchoolId("2");
        rsMathSchoolHomeworkBehaviorStatDao.insert(stat2);

        List<RSMathSchoolHomeworkBehaviorStat> result = rsMathSchoolHomeworkBehaviorStatDao.findBySchoolIds_withoutIdSet(Arrays.asList("1", "2"), 2015, Term.上学期);
        assertTrue(result.size() == 2);
    }

    @Test
    public void testFindSchoolDataByCityCodes_withoutIdSet() {
        RSMathSchoolHomeworkBehaviorStat stat1 = new RSMathSchoolHomeworkBehaviorStat();
        stat1.setId("2015_1_1");
        stat1.setCcode(1L);
        rsMathSchoolHomeworkBehaviorStatDao.insert(stat1);

        RSMathSchoolHomeworkBehaviorStat stat2 = new RSMathSchoolHomeworkBehaviorStat();
        stat2.setId("2015_1_2");
        stat2.setCcode(2L);
        rsMathSchoolHomeworkBehaviorStatDao.insert(stat2);

        List<RSMathSchoolHomeworkBehaviorStat> result = rsMathSchoolHomeworkBehaviorStatDao.findSchoolDataByCityCodes_withoutIdSet(Arrays.asList(1L, 2L), 2015, Term.上学期);
        assertTrue(result.size() == 2);
    }

    @Test
    public void testFindSchoolDataByAreaCodes_withoutIdSet() {
        RSMathSchoolHomeworkBehaviorStat stat1 = new RSMathSchoolHomeworkBehaviorStat();
        stat1.setId("2015_1_1");
        stat1.setAcode(1L);
        rsMathSchoolHomeworkBehaviorStatDao.insert(stat1);

        RSMathSchoolHomeworkBehaviorStat stat2 = new RSMathSchoolHomeworkBehaviorStat();
        stat2.setId("2015_1_2");
        stat2.setAcode(2L);
        rsMathSchoolHomeworkBehaviorStatDao.insert(stat2);

        List<RSMathSchoolHomeworkBehaviorStat> result = rsMathSchoolHomeworkBehaviorStatDao.findSchoolDataByAreaCodes_withoutIdSet(Arrays.asList(1L, 2L), 2015, Term.上学期);
        assertTrue(result.size() == 2);
    }

    @Test
    public void testUpdateStudentAndTeacherDataById() throws Exception {
        List<Long> stuIds = Arrays.asList(1L, 2L, 3L);
        List<Long> teacherIds = Arrays.asList(4L, 5L, 6L);

        RSMathSchoolHomeworkBehaviorStat rsMathSchoolHomeworkBehaviorStat = new RSMathSchoolHomeworkBehaviorStat();
        rsMathSchoolHomeworkBehaviorStat.setId("2015_1_1");
        rsMathSchoolHomeworkBehaviorStat.setSchoolId("1");
        rsMathSchoolHomeworkBehaviorStat.setSchoolName("test school");
        rsMathSchoolHomeworkBehaviorStat.setAcode(2L);
        rsMathSchoolHomeworkBehaviorStat.setAreaName("test area");
        rsMathSchoolHomeworkBehaviorStat.setCcode(3L);
        rsMathSchoolHomeworkBehaviorStat.setCityName("test city");
        rsMathSchoolHomeworkBehaviorStat.setPcode(4L);
        rsMathSchoolHomeworkBehaviorStat.setProvinceName("test province");
        rsMathSchoolHomeworkBehaviorStat.setStuIds(new HashSet<>(stuIds));
        rsMathSchoolHomeworkBehaviorStat.setTeacherIds(new HashSet<>(teacherIds));
        rsMathSchoolHomeworkBehaviorStat.setStuNum(3);
        rsMathSchoolHomeworkBehaviorStat.setStuTimes(3L);
        rsMathSchoolHomeworkBehaviorStat.setTeacherNum(3);
        rsMathSchoolHomeworkBehaviorStat.setTeacherTimes(3L);
        rsMathSchoolHomeworkBehaviorStat.setCreateAt(new Date());

        String id = rsMathSchoolHomeworkBehaviorStatDao.insert(rsMathSchoolHomeworkBehaviorStat);

        rsMathSchoolHomeworkBehaviorStatDao.updateStudentAndTeacherDataById("1",
                Arrays.asList(3L, 4L, 5L), Arrays.asList(6L, 5L, 7L), 3L, 3L, 2015, Term.上学期);

        RSMathSchoolHomeworkBehaviorStat ret = rsMathSchoolHomeworkBehaviorStatDao.load(id);
        assertTrue(ret != null);
        assertEquals(6, (long)ret.getStuTimes());
        assertEquals(6, (long)ret.getTeacherTimes());
        assertEquals(5, ret.getStuIds().size());
        assertEquals(4, ret.getTeacherIds().size());
        assertEquals(5, (long)ret.getStuNum());
        assertEquals(4, (long)ret.getTeacherNum());
    }
}
