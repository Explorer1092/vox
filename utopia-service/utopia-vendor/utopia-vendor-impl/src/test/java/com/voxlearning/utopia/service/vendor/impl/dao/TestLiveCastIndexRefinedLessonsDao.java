package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastTargetAndExtra;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午7:41
 **/
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestLiveCastIndexRefinedLessonsDao {

    @Inject
    private LiveCastIndexRefinedLessonsDao liveCastIndexRefinedLessonsDao;

    @Test
    public void test(){
        LiveCastIndexRefinedLessons lessons = new LiveCastIndexRefinedLessons();
        lessons.setId("sid_123");
        LiveCastIndexRefinedLessons.Target target = new LiveCastTargetAndExtra.Target();
        target.setType(LiveCastTargetAndExtra.Target.Type.sid);
        target.setValue(123L);

        LiveCastIndexRefinedLessons.Extra extra = new LiveCastTargetAndExtra.Extra();
        extra.setPriority(100);
        extra.setEndTime(DateUtils.calculateDateDay(new Date(), 1));

        LiveCastIndexRefinedLessons.LessonInfo lessonInfo = new LiveCastIndexRefinedLessons.LessonInfo();
        lessonInfo.setWatchCount("100");
        lessonInfo.setCoverUrl("http://1231231213");
        lessonInfo.setDesc("descdesc");
        lessonInfo.setLabel("lable");
        lessonInfo.setNeedLogin(true);
        lessonInfo.setTitle("titletitle");
        lessonInfo.setUrl("http://wwwwwdlldld.dld.dl.dld");

        lessons.setExtra(extra);
        lessons.setTarget(target);
        lessons.setLessonInfoList(Collections.singletonList(lessonInfo));
        LiveCastIndexRefinedLessons upsert1 = liveCastIndexRefinedLessonsDao.upsert(lessons);


        lessons.getExtra().setPriority(999999);
        LiveCastIndexRefinedLessons upsert2 = liveCastIndexRefinedLessonsDao.upsert(lessons);



        LiveCastIndexRefinedLessons load = liveCastIndexRefinedLessonsDao.load("sid_123");

        assert load.getExtra().getPriority().equals(999999);
    }
}
