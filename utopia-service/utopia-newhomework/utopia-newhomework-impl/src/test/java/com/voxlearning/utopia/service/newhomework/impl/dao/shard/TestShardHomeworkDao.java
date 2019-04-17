package com.voxlearning.utopia.service.newhomework.impl.dao.shard;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DropMongoDatabase
public class TestShardHomeworkDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testLoadShardHomeworksByGroupIds() {
        Date createDate = new Date();
        Subject subject = Subject.ENGLISH;

        List<ShardHomework> shardHomeworks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ShardHomework shardHomework = new ShardHomework();
            String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
            String id = new ShardHomework.ID(month).toString();
            shardHomework.setId(id);
            shardHomework.setClazzGroupId(i + 10000L);
            shardHomework.setSubject(subject);
            shardHomework.setCreateAt(createDate);
            shardHomeworks.add(shardHomework);
        }
        shardHomeworkDao.inserts(shardHomeworks);

        Set<Long> groupIds = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            groupIds.add(i + 10000L);
        }
        Map<Long, List<ShardHomework.Location>> map = shardHomeworkDao.loadShardHomeworksByClazzGroupIds(groupIds);
        assertEquals(groupIds.size(), map.size());
        map = shardHomeworkDao.loadShardHomeworksByClazzGroupIds(groupIds);

        groupIds.stream()
                .map(map::get)
                .mapToInt(List::size)
                .forEach(s -> assertEquals(1, s));
    }

    @Test
    public void testUpdateShardHomeworkChecked() {
        Date createDate = new Date();
        Subject subject = Subject.MATH;
        ShardHomework shardHomework = new ShardHomework();
        String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
        String id = new ShardHomework.ID(month).toString();
        shardHomework.setId(id);
        shardHomework.setClazzGroupId(9999999L);
        shardHomework.setSubject(subject);
        shardHomework.setCreateAt(createDate);
        shardHomeworkDao.insert(shardHomework);

        shardHomeworkDao.updateShardHomeworkChecked(id, true, new Date(), HomeworkSourceType.App);
        shardHomework = shardHomeworkDao.load(id);
        assertTrue(shardHomework.isHomeworkChecked());
        assertNotNull(shardHomework.getChecked());
    }

    @Test
    public void testUpdateShardHomeworkTime() {
        Date createDate = new Date();
        Subject subject = Subject.MATH;
        ShardHomework shardHomework = new ShardHomework();
        String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
        String id = new ShardHomework.ID(month).toString();
        shardHomework.setId(id);
        shardHomework.setClazzGroupId(9999999L);
        shardHomework.setSubject(subject);
        shardHomework.setCreateAt(createDate);
        shardHomeworkDao.insert(shardHomework);

        assertTrue(shardHomeworkDao.updateShardHomeworkTime(id, new Date(), new Date()));
        shardHomework = shardHomeworkDao.load(id);
        assertNotNull(shardHomework.getStartTime());
        assertNotNull(shardHomework.getEndTime());
    }

    @Test
    public void updateDisableTrue() {
        Date createDate = new Date();
        Subject subject = Subject.ENGLISH;
        List<ShardHomework> shardHomeworkList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            ShardHomework shardHomework = new ShardHomework();
            String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
            String id = new ShardHomework.ID(month).toString();
            shardHomework.setId(id);
            shardHomework.setClazzGroupId(1L);
            shardHomework.setSubject(subject);
            shardHomework.setCreateAt(createDate);
            shardHomework.setChecked(i == 1);
            shardHomeworkList.add(shardHomework);
        }
        shardHomeworkDao.inserts(shardHomeworkList);

        String id = shardHomeworkList.get(0).getId();
        shardHomeworkDao.load(id);
        Map<Long, List<ShardHomework.Location>> locationMap = shardHomeworkDao.loadShardHomeworksByClazzGroupIds(Collections.singleton(1L));
        assertEquals(locationMap.get(1L).size(), 3);
        FlightRecorder.dot("+++++++++++++++++++++++++");
        assertTrue(shardHomeworkDao.updateDisabledTrue(id));
        FlightRecorder.dot("+++++++++++++++++++++++++");
        ShardHomework shardHomework = shardHomeworkDao.load(id);
        assertTrue(shardHomework.isDisabledTrue());
        shardHomeworkDao.loadShardHomeworksByClazzGroupIds(Collections.singleton(1L));
    }

    @Test
    public void testInsert() {
        Date createDate = new Date();
        Subject subject = Subject.MATH;

        ShardHomework shardHomework = new ShardHomework();
        String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
        String id = new ShardHomework.ID(month).toString();
        shardHomework.setId(id);
        shardHomework.setClazzGroupId(9999999L);
        shardHomework.setSubject(subject);
        shardHomework.setCreateAt(createDate);

        FlightRecorder.dot(">>>>>>>>>> begin insert");
        shardHomeworkDao.insert(shardHomework);
        FlightRecorder.dot(">>>>>>>>>> end insert");
        id = shardHomework.getId();
        shardHomework = shardHomeworkDao.load(id);
        FlightRecorder.dot(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        assertEquals(subject, shardHomework.getSubject());
        shardHomeworkDao.load(id);
    }

    @Test
    public void testInserts() {
        Date createDate = new Date();
        Subject subject = Subject.ENGLISH;

        List<ShardHomework> shardHomeworks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ShardHomework shardHomework = new ShardHomework();
            String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
            String id = new ShardHomework.ID(month).toString();
            shardHomework.setId(id);
            shardHomework.setClazzGroupId(i + 10000L);
            shardHomework.setSubject(subject);
            shardHomework.setCreateAt(createDate);
            shardHomeworks.add(shardHomework);
        }
        shardHomeworkDao.inserts(shardHomeworks);

        for (ShardHomework shardHomework : shardHomeworks) {
            ShardHomework temp = shardHomeworkDao.load(shardHomework.getId());
            assertNotNull(temp);
        }
    }
}
