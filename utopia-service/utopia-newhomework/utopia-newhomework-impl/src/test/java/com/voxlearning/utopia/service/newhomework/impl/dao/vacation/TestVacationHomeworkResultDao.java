package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.annotation.MockBinders;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xuesong.zhang
 * @since 2016/11/28
 */
@DropMongoDatabase
public class TestVacationHomeworkResultDao extends NewHomeworkUnitTestSupport {

    @Test
    @MockBinders({
            @MockBinder(
                    type = VacationHomeworkPackage.class,
                    jsons = {
                            "{'id':'p1','teacherId':30009,'clazzGroupId':1,'subject':'MATH','bookId':'bk_1','actionId':'a1'}",
                            "{'id':'p2','teacherId':30009,'clazzGroupId':2,'subject':'MATH','bookId':'bk_1','actionId':'a1'}",
                    },
                    persistence = VacationHomeworkPackageDao.class
            ),
            @MockBinder(
                    type = VacationHomework.class,
                    jsons = {
                            "{'packageId':'p1','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
                            "{'packageId':'p1','weekRank':1,'dayRank':2,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
                            "{'packageId':'p1','weekRank':1,'dayRank':3,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
                            "{'packageId':'p1','weekRank':1,'dayRank':4,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",

                            "{'packageId':'p2','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10002}",
                    },
                    persistence = VacationHomeworkDao.class
            )
    })
    public void testInitVacationHomeworkResult() {
        String packageId = "p1";
        Long studentId = 10001L;

        FlightRecorder.dot("########## BEGIN ##########");
        String homeworkId = new VacationHomework.ID(packageId, 1, 1, studentId).toString();
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        FlightRecorder.dot("########## 1111 ##########");
        vacationHomeworkResultDao.initVacationHomeworkResult(vacationHomework.toLocation(), studentId);
        String resultId = new VacationHomeworkResult.ID(packageId, vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();

        FlightRecorder.dot("########## 2222 ##########");
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(resultId);
        Assert.assertNotNull(vacationHomeworkResult.getUserStartAt());
        FlightRecorder.dot("########## 3333 ##########");
        vacationHomeworkResultDao.initVacationHomeworkResult(vacationHomework.toLocation(), studentId);
        FlightRecorder.dot("########## END ##########");
    }

    @Test
    @MockBinder(
            type = VacationHomework.class,
            jsons = {
                    "{'packageId':'p1','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
            },
            persistence = VacationHomeworkDao.class
    )
    public void testStudentDoHomework() throws Exception {
        String packageId = "p1";
        Long studentId = 10001L;

        String homeworkId = new VacationHomework.ID(packageId, 1, 1, studentId).toString();
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        VacationHomework.Location location = vacationHomework.toLocation();
        vacationHomeworkResultDao.initVacationHomeworkResult(location, studentId);
        String resultId = new VacationHomeworkResult.ID(packageId, vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();
        assertEquals(homeworkId, resultId);

        vacationHomeworkResultDao.doHomework(location, ObjectiveConfigType.EXAM, "Q_1-1", "pro_1");
        vacationHomeworkResultDao.doHomework(location, ObjectiveConfigType.EXAM, "Q_2-1", "pro_2");
        vacationHomeworkResultDao.doHomework(location, ObjectiveConfigType.EXAM, "Q_3-1", "pro_3");
        vacationHomeworkResultDao.doHomework(location, ObjectiveConfigType.EXAM, "Q_3-1", "pro_4");

        FlightRecorder.dot("########## load ##########");
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(resultId);
        assertEquals(vacationHomeworkResult.getPractices().get(ObjectiveConfigType.EXAM).getAnswers().size(), 3);
        assertEquals(vacationHomeworkResult.getPractices().get(ObjectiveConfigType.EXAM).getAnswers().get("Q_3-1"), "pro_3");
    }

    @Test
    @MockBinder(
            type = VacationHomework.class,
            jsons = {
                    "{'packageId':'p1','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
            },
            persistence = VacationHomeworkDao.class
    )
    public void testFinishHomework() throws Exception {
        String packageId = "p1";
        Long studentId = 10001L;

        String homeworkId = new VacationHomework.ID(packageId, 1, 1, studentId).toString();
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        VacationHomework.Location location = vacationHomework.toLocation();
        vacationHomeworkResultDao.initVacationHomeworkResult(location, studentId);
        String resultId = new VacationHomeworkResult.ID(packageId, vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();

        VacationHomeworkResult ret = vacationHomeworkResultDao.finishHomework(location, ObjectiveConfigType.EXAM, 100D, 10L, true, true);
        assertTrue(ret != null);

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(resultId);
        assertTrue(vacationHomeworkResult != null);
    }

    @Test
    @MockBinder(
            type = VacationHomework.class,
            jsons = {
                    "{'packageId':'p1','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
            },
            persistence = VacationHomeworkDao.class
    )
    public void testSaveVacationHomeworkRewardIntegral() throws Exception {
        String packageId = "p1";
        Long studentId = 10001L;
        Integer Integral = 10;

        String homeworkId = new VacationHomework.ID(packageId, 1, 1, studentId).toString();
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        VacationHomework.Location location = vacationHomework.toLocation();
        vacationHomeworkResultDao.initVacationHomeworkResult(location, studentId);
        String resultId = new VacationHomeworkResult.ID(packageId, vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();

        FlightRecorder.dot("########## BEGIN ##########");
        boolean b = vacationHomeworkResultDao.saveVacationHomeworkRewardIntegral(resultId, Integral);
        assertTrue(b);
        FlightRecorder.dot("########## 1111 ##########");
        VacationHomeworkResult result = vacationHomeworkResultDao.load(resultId);
        assertEquals(result.getRewardIntegral(), Integral);

        FlightRecorder.dot("########## 2222 ##########");
        b = vacationHomeworkResultDao.saveVacationHomeworkRewardIntegral(resultId, Integral);
        assertTrue(b);
        FlightRecorder.dot("########## 3333 ##########");
        result = vacationHomeworkResultDao.load(resultId);
        assertEquals(result.getRewardIntegral(), new Integer(Integral + Integral));

        FlightRecorder.dot("########## END ##########");
    }

    @Test
    @MockBinder(
            type = VacationHomework.class,
            jsons = {
                    "{'packageId':'p1','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
            },
            persistence = VacationHomeworkDao.class
    )
    public void testSaveNewHomeworkComment() throws Exception {
        String packageId = "p1";
        Long studentId = 10001L;
        String comment = "我是评语";

        String homeworkId = new VacationHomework.ID(packageId, 1, 1, studentId).toString();
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        VacationHomework.Location location = vacationHomework.toLocation();
        vacationHomeworkResultDao.initVacationHomeworkResult(location, studentId);
        String resultId = new VacationHomeworkResult.ID(packageId, vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();


        FlightRecorder.dot("########## BEGIN ##########");
        boolean b = vacationHomeworkResultDao.saveVacationHomeworkComment(resultId, comment, null);
        assertTrue(b);
        FlightRecorder.dot("########## 1111 ##########");
        VacationHomeworkResult result = vacationHomeworkResultDao.load(resultId);
        assertEquals(result.getComment(), comment);
        FlightRecorder.dot("########## END ##########");
    }
}
