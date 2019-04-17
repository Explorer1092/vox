package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2016/11/29
 */
@DropMongoDatabase
public class TestVacationHomeworkDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testInsertVacationHomework() throws Exception {
        Date createDate = new Date();
        Subject subject = Subject.MATH;
        String packageId = "p1";
        Integer weekRank = 1;
        Integer dayRank = 1;
        Long studentId = 10001L;

        VacationHomework vacationHomework = new VacationHomework();
        vacationHomework.setPackageId(packageId);
        vacationHomework.setWeekRank(weekRank);
        vacationHomework.setDayRank(dayRank);
        vacationHomework.setStudentId(studentId);
        vacationHomework.setSubject(subject);
        vacationHomework.setCreateAt(createDate);
        vacationHomeworkDao.insert(vacationHomework);

        String dbId = vacationHomework.getId();
        String expectId = new VacationHomework.ID(packageId, weekRank, dayRank, studentId).toString();
        assertEquals(dbId, expectId);

        vacationHomework = vacationHomeworkDao.load(expectId);

        assertEquals(subject, vacationHomework.getSubject());
        vacationHomeworkDao.load(expectId);
    }

    @Test
    @MockBinder(
            type = VacationHomework.class,
            jsons = {
                    "{'subject':'CHINESE','packageId':'p1','weekRank':1,'dayRank':1,'studentId':1,'clazzGroupId':1,'disabled':false}",
                    "{'subject':'CHINESE','packageId':'p1','weekRank':1,'dayRank':2,'studentId':1,'clazzGroupId':1,'disabled':false}",
                    "{'subject':'CHINESE','packageId':'p1','weekRank':2,'dayRank':1,'studentId':1,'clazzGroupId':1,'disabled':false}",
                    "{'subject':'MATH','packageId':'p2','weekRank':1,'dayRank':1,'studentId':1,'clazzGroupId':2,'disabled':false}",
                    "{'subject':'MATH','packageId':'p2','weekRank':1,'dayRank':2,'studentId':1,'clazzGroupId':2,'disabled':false}",
                    "{'subject':'MATH','packageId':'p2','weekRank':1,'dayRank':3,'studentId':1,'clazzGroupId':2,'disabled':false}",
                    "{'subject':'ENGLISH','packageId':'p3','weekRank':1,'dayRank':1,'studentId':2,'clazzGroupId':3,'disabled':false}",
                    "{'subject':'ENGLISH','packageId':'p3','weekRank':2,'dayRank':1,'studentId':2,'clazzGroupId':3,'disabled':false}",
                    "{'subject':'ENGLISH','packageId':'p3','weekRank':2,'dayRank':2,'studentId':2,'clazzGroupId':3,'disabled':false}",
            },
            persistence = VacationHomeworkDao.class
    )
    public void testLoadVacationHomeworkByClazzGroupIds() throws Exception {
//        Set<Long> groupIds = MDP.groupingBy(VacationHomework.class, VacationHomework::getClazzGroupId).keySet();
//        Map<Long, List<VacationHomework.Location>> map = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(groupIds);
//        assertEquals(groupIds.size(), map.size());
//
//        groupIds.stream()
//                .map(map::get)
//                .mapToInt(List::size)
//                .forEach(s -> assertEquals(3, s));
        Set<Long> groupIds = new LinkedHashSet<>();
        groupIds.add(4L);
        Map<Long, List<VacationHomework.Location>> map = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(groupIds);
        logger.info(map.toString());
        map = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(groupIds);
        logger.info(map.toString());
        groupIds.add(1L);
        map = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(groupIds);
        logger.info(map.toString());
        map = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(groupIds);
        logger.info(map.toString());
    }

    @Test
    public void testLoad() {
        Map<Long, List<VacationHomework.Location>> locations1 = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(Collections.singletonList(5L));
        Map<Long, List<VacationHomework.Location>> locations2 = vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(Collections.singletonList(5L));
        System.out.println(locations1);
        System.out.println(locations2);
        locations2.get(5L);
    }
}
