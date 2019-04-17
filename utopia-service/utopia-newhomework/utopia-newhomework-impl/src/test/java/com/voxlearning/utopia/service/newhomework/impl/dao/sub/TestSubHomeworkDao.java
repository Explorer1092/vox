package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/18
 */
@DropMongoDatabase
public class TestSubHomeworkDao extends NewHomeworkUnitTestSupport {

    @Test
    @MockBinder(
            type = SubHomework.class,
            jsons = {
                    "{'subject':'CHINESE','clazzGroupId':1,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':1,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':1,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':2,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':2,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':2,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':3,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':3,'disabled':false}",
                    "{'subject':'CHINESE','clazzGroupId':3,'disabled':false}",
            },
            persistence = SubHomeworkDao.class
    )
    public void testLoadSubHomeworksByGroupIds() throws Exception {
        Set<Long> groupIds = MDP.groupingBy(SubHomework.class, SubHomework::getClazzGroupId).keySet();
        Map<Long, List<SubHomework.Location>> map = subHomeworkDao.loadSubHomeworksByClazzGroupIds(groupIds);
        assertEquals(groupIds.size(), map.size());

        groupIds.stream()
                .map(map::get)
                .mapToInt(List::size)
                .forEach(s -> assertEquals(3, s));
    }

    @Test
    @MockBinder(
            type = SubHomework.class,
            jsons = "{'_id':$objectId}",
            persistence = SubHomeworkDao.class
    )
    public void testUpdateSubHomeworkChecked() throws Exception {
        String id = MDP.findOne(SubHomework.class).getId();
        assertTrue(subHomeworkDao.updateSubHomeworkChecked(id, true, new Date(), HomeworkSourceType.Web));
        SubHomework homework = subHomeworkDao.load(id);
        assertTrue(homework.isHomeworkChecked());
        assertNotNull(homework.getCheckedAt());
    }

    @Test
    @MockBinder(
            type = SubHomework.class,
            jsons = "{'_id':$objectId,'clazzGroupId':1, 'checked':false}",
            persistence = SubHomeworkDao.class
    )
    public void testUpdateSubHomeworkTime() throws Exception {
        String id = MDP.findOne(SubHomework.class).getId();
        assertTrue(subHomeworkDao.updateSubHomeworkTime(id, new Date(), new Date()));
        SubHomework homework = subHomeworkDao.load(id);
        assertNotNull(homework.getStartTime());
        assertNotNull(homework.getEndTime());
    }

    @Test
    @MockBinder(
            type = SubHomework.class,
            jsons = {
                    "{'_id':$objectId, subject:'ENGLISH', 'clazzGroupId':1, 'checked':false}",
                    "{'_id':$objectId, subject:'ENGLISH', 'clazzGroupId':1, 'checked':true}",
                    "{'_id':$objectId, subject:'ENGLISH', 'clazzGroupId':1, 'checked':true}",
            },
            persistence = SubHomeworkDao.class
    )
    public void updateDisabledTrue() throws Exception {
        String id = MDP.findOne(SubHomework.class).getId();
        subHomeworkDao.load(id);
        Map<Long, List<SubHomework.Location>> locationMap = subHomeworkDao.loadSubHomeworksByClazzGroupIds(Collections.singleton(1L));
        assertEquals(locationMap.get(1L).size(), 3);
        FlightRecorder.dot("+++++++++++++++++++++++++");
        assertTrue(subHomeworkDao.updateDisabledTrue(id));
        FlightRecorder.dot("+++++++++++++++++++++++++");
        SubHomework homework = subHomeworkDao.load(id);
        assertTrue(homework.isDisabledTrue());

        subHomeworkDao.loadSubHomeworksByClazzGroupIds(Collections.singleton(1L));
    }

    @Test
    public void testInsert() throws Exception {
        Date createDate = new Date();
        Subject subject = Subject.MATH;

        SubHomework homework = new SubHomework();
        homework.setClazzGroupId(999999999L);
        homework.setSubject(subject);
        homework.setCreateAt(createDate);

        FlightRecorder.dot(">>>>>>>>>> begin insert");
        subHomeworkDao.insert(homework);
        FlightRecorder.dot(">>>>>>>>>> end insert");
        String id = homework.getId();
        homework = subHomeworkDao.load(id);
        FlightRecorder.dot(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        assertEquals(subject, homework.getSubject());
        subHomeworkDao.load(id);
    }
}
