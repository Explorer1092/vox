package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xuesong.zhang
 * @since 2017/1/18
 */
@DropMongoDatabase
public class TestSubHomeworkResultDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testInitSubHomeworkResult() throws Exception {
        DayRange day = DayRange.current();
        SubHomeworkResult result;
        SubHomework.Location location = new SubHomework.Location();
        location.setCreateTime(day.getStartTime());
        location.setId(RandomUtils.nextObjectId());
        location.setSubject(Subject.MATH);
        location.setActionId("A");
        location.setClazzGroupId(1);
        long userId = 30009;
        String id = new SubHomeworkResult.ID(day.toString(), location.getSubject(), location.getId(), Long.toString(userId)).toString();
        subHomeworkResultDao.initSubHomeworkResult(location, userId);
        result = subHomeworkResultDao.load(id);
        Assert.assertNotNull(result.getUserStartAt());

        FlightRecorder.dot("##############################");
        subHomeworkResultDao.initSubHomeworkResult(location, userId);
    }

    @Test
    public void testFinishHomework() throws Exception {
        Date date = new Date();

        SubHomework.Location location = new SubHomework.Location();
        location.setId("123");
        location.setCreateTime(date.getTime());
        location.setSubject(Subject.ENGLISH);

        subHomeworkResultDao.initSubHomeworkResult(location, 10L);

        SubHomeworkResult ret = subHomeworkResultDao.finishHomework(location, 10L, ObjectiveConfigType.EXAM, 100D, 10L, true, true, true, true, SchoolLevel.JUNIOR, null, null, null, null, null, null);
        assertTrue(ret != null);

        SubHomeworkResult.ID id = new SubHomeworkResult.ID(DayRange.current().toString(), Subject.ENGLISH, "123", "10");
        SubHomeworkResult result = subHomeworkResultDao.load(id.toString());
        assertTrue(result != null);
    }

    @Test
    @MockBinder(
            type = SubHomeworkResult.class,
            jsons = {
                    "{'id':'20160101-ENGLISH-123-1'}"
            },
            persistence = SubHomeworkResultAsyncDao.class
    )
    public void testFinishCorrectToApp() {

        SubHomeworkResult subHomeworkResult = subHomeworkResultDao.load("20160101-ENGLISH-123-1");

        subHomeworkResult.setHomeworkId("20160101");

        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> practices = new LinkedHashMap<>();

        subHomeworkResult.setPractices(practices);

        BaseHomeworkResultAnswer baseHomeworkResultAnswer = new BaseHomeworkResultAnswer();

        practices.put(ObjectiveConfigType.NEW_READ_RECITE, baseHomeworkResultAnswer);

        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswers = new LinkedHashMap<>();

        BaseHomeworkResultAppAnswer baseHomeworkResultAppAnswer = new BaseHomeworkResultAppAnswer();

        appAnswers.put("12", baseHomeworkResultAppAnswer);

        subHomeworkResultDao.upsert(subHomeworkResult);

        subHomeworkResultDao.finishCorrectToApp("20160101", "20160101-ENGLISH-123-1", ObjectiveConfigType.NEW_READ_RECITE, "12", true, CorrectType.CORRECT, Correction.GOOD, "", false);

        SubHomeworkResult subHomeworkResult1 = subHomeworkResultDao.load("20160101-ENGLISH-123-1");

        int a = 14;

    }


    @Test
    @MockBinder(
            type = SubHomeworkResult.class,
            jsons = {
                    "{'id':'20160101-ENGLISH-123-1'}",
                    "{'id':'20160101-ENGLISH-123-2'}",
                    "{'id':'20160101-ENGLISH-123-3'}",
                    "{'id':'20160101-ENGLISH-123-4'}",
                    "{'id':'20160101-ENGLISH-123-5'}"
            },
            persistence = SubHomeworkResultShardDao.class
    )
    public void testFindByHomework() throws Exception {
        Set<SubHomeworkResult.Location> locations = subHomeworkResultDao.findByHomework("20160101", Subject.ENGLISH, "123", Collections.emptyList());
        assertEquals(5, locations.size());
        locations = subHomeworkResultDao.findByHomework("20160101", Subject.ENGLISH, "123", Collections.emptyList());
        assertEquals(5, locations.size());
    }

    @Test
    public void testSaveSubHomeworkComment() throws Exception {
        DayRange day = DayRange.current();
        SubHomework.Location location = new SubHomework.Location();
        location.setCreateTime(day.getStartTime());
        location.setId(RandomUtils.nextObjectId());
        location.setSubject(Subject.MATH);
        location.setActionId("A");
        location.setClazzGroupId(1);

        long userId = 30009;
        String comment = "我是评语";
        String audioComment = "https://oss-data.17zuoye.com/2017/06/09/20170609160344531143.mp3";

        String id = new SubHomeworkResult.ID(day.toString(), location.getSubject(), location.getId(), Long.toString(userId)).toString();

        SubHomeworkResult homeworkResult = subHomeworkResultDao.saveSubHomeworkComment(location, userId, comment, audioComment);
        assertTrue(homeworkResult != null);
        SubHomeworkResult result = subHomeworkResultDao.load(id);
        assertEquals(result.getComment(), comment);
        Assert.assertNull(result.getUserStartAt());

        subHomeworkResultDao.initSubHomeworkResult(location, userId);
        result = subHomeworkResultDao.load(id);
        Assert.assertNotNull(result.getUserStartAt());
    }
}
