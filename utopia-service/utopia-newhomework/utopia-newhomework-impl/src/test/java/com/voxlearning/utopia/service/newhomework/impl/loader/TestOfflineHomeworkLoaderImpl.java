package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.OfflineHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/9/12
 */
@DropMongoDatabase
public class TestOfflineHomeworkLoaderImpl extends NewHomeworkUnitTestSupport {
    @Test
    @MockBinder(
            type = OfflineHomework.class,
            jsons = {
                    "{'id':$objectId,'newHomeworkId':'1'}",
                    "{'id':$objectId,'newHomeworkId':'2'}",
                    "{'id':$objectId,'newHomeworkId':'3'}",
            },
            persistence = OfflineHomeworkDao.class
    )
    public void testLoadByNewHomeworkIds() {
        List<String> newHomeworkIds = Arrays.asList("1", "2", "3");
        FlightRecorder.dot("111111111111111111111");
        offlineHomeworkLoader.loadByNewHomeworkIds(newHomeworkIds);
        newHomeworkIds = Arrays.asList("1", "2", "3", "4", "5");
        FlightRecorder.dot("222222222222222222222");
        offlineHomeworkLoader.loadByNewHomeworkIds(newHomeworkIds);
        FlightRecorder.dot("333333333333333333333");
        offlineHomeworkLoader.loadByNewHomeworkIds(newHomeworkIds);
    }

    @Test
    @MockBinder(
            type = OfflineHomework.class,
            jsons = {
                    "{'id':$objectId,'newHomeworkId':'1','clazzGroupId':'1'}",
                    "{'id':$objectId,'newHomeworkId':'2','clazzGroupId':'2'}",
                    "{'id':$objectId,'newHomeworkId':'3','clazzGroupId':'3'}",
                    "{'id':$objectId,'newHomeworkId':'4','clazzGroupId':'3'}"
            },
            persistence = OfflineHomeworkDao.class
    )
    public void testLoadGroupOfflineHomeworks() {
        List<Long> groupIds = Arrays.asList(1L, 2L);
        FlightRecorder.dot("111111111111111111111");
        Map<Long, List<OfflineHomework>> offlineHomeworkMap = offlineHomeworkLoader.loadGroupOfflineHomeworks(groupIds);
        Assert.assertEquals(offlineHomeworkMap.get(1L).size(), 1);
        Assert.assertEquals(offlineHomeworkMap.get(2L).size(), 1);
        groupIds = Arrays.asList(1L, 2L, 3L);
        FlightRecorder.dot("222222222222222222222");
        offlineHomeworkMap = offlineHomeworkLoader.loadGroupOfflineHomeworks(groupIds);
        Assert.assertEquals(offlineHomeworkMap.get(3L).size(), 2);
        FlightRecorder.dot("333333333333333333333");
        offlineHomeworkLoader.loadGroupOfflineHomeworks(groupIds);
    }
}
