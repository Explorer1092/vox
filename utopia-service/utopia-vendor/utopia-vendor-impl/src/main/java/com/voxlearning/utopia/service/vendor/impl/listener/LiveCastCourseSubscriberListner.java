package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourse;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastCourseDao;
import com.voxlearning.utopia.service.vendor.impl.service.LiveCastCourseServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.version.LiveCastCourseBufferVersion;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:53
 **/
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "xue.queue.JztHPWeeklyOpenCourse "),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "xue.queue.JztHPWeeklyOpenCourse")
})
public class LiveCastCourseSubscriberListner implements MessageListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Inject
    private LiveCastCourseDao liveCastCourseDao;

    @Inject
    private LiveCastCourseBufferVersion liveCastCourseBufferVersion;

    @Inject
    private LiveCastCourseServiceImpl liveCastCourseService;

    @Override
    public void onMessage(Message message) {
        List<LiveCastCourse> courseList = new ArrayList<>();
        Object object = message.decodeBody();
        if (object instanceof String) {
            String json = String.class.cast(object);
            if (!RuntimeMode.isProduction()){
                logger.info("接收到直播课程同步消息：{}", json);
            }
            courseList = JsonUtils.fromJsonToList(json, LiveCastCourse.class);
            if (courseList == null){
                logger.error("直播课程消息转化失败！：{}", json);
                return;
            }
        }else {
            logger.error("直播课程同步消息格式错误！不是 String！");
            return;
        }
        List<LiveCastCourse> liveCastCourses = liveCastCourseDao.query();
        if (CollectionUtils.isNotEmpty(liveCastCourses)){
            liveCastCourses.forEach(t -> liveCastCourseDao.remove(t.getCourseId()));
        }
        courseList.forEach(t -> {
            liveCastCourseDao.upsert(t);
            liveCastCourseService.setCourseSubCount(t.getCourseId(), t.getSubscribeNum());
        });
        liveCastCourseBufferVersion.increment();
    }
    public static void main(String[] args) {
        String json = "[{\"courseId\":\"2229_1\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"一年级\",\"grade\":1,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_2\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"二年级\",\"grade\":2,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_3\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"三年级\",\"grade\":3,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_4\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"四年级\",\"grade\":4,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_5\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"五年级\",\"grade\":5,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_6\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"六年级\",\"grade\":6,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_7\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"初一\",\"grade\":7,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_8\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"初二\",\"grade\":8,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]},{\"courseId\":\"2229_9\",\"courseName\":\"家长通-语文全年级\",\"subjectName\":\"语文\",\"subject\":3,\"gradeName\":\"初三\",\"grade\":9,\"subscribeNum\":0,\"detailUrl\":\"https://www.test.17zuoye.net/redirector/goaoshu.vpage?returnURL=https%3A%2F%2F17xue-student.test.17zuoye.net%2Fm%2Fcourse%2Fcenter%2Findex.vpage%23%2Fdetail%2F2229\",\"casterName\":\"老师lee\",\"casterAvatarUrl\":\"http://static.17xueba.com//test/server/image/2018/03/20180326105709236909.png\",\"rank\":3,\"tag\":\"系列课\",\"lessonSegments\":[{\"startTime\":1537952444000,\"endTime\":1537953044000}]}]";
        List<LiveCastCourse> courseList = JsonUtils.fromJsonToList(json, LiveCastCourse.class);
    }
}
