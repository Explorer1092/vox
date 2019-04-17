package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourseStudentSub;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastCourseStudentSubDao;
import com.voxlearning.utopia.service.vendor.impl.service.LiveCastCourseServiceImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2018-09-25 下午12:52
 **/
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "xue.queue.JztHPWeeklyOpenCourseStudent"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "xue.queue.JztHPWeeklyOpenCourseStudent")
})
public class LiveCastCourseStudentSubSubscriberListener implements MessageListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private LiveCastCourseStudentSubDao liveCastCourseStudentSubDao;

    @Inject
    private LiveCastCourseServiceImpl liveCastCourseService;

    @Override
    public void onMessage(Message message) {
        LiveCastCourseStudentSub liveCastCourseStudentSub = null;
        Object object = message.decodeBody();
        if (object instanceof String) {
            String json = String.class.cast(object);
            if (!RuntimeMode.isProduction()){
                logger.info("接收到直播 学生订阅课程 同步消息：{}", json);
            }
            liveCastCourseStudentSub = JsonUtils.fromJson(json, LiveCastCourseStudentSub.class);
            if (liveCastCourseStudentSub == null){
                logger.error("直播学生订阅课程消息转化失败！：{}", json);
                return;
            }
        }else {
            logger.error("直播学生订阅课程 消息格式错误！不是 String！");
            return;
        }
        liveCastCourseStudentSub.generateId();
        liveCastCourseStudentSubDao.upsert(liveCastCourseStudentSub);
        liveCastCourseService.setCourseSubCount(liveCastCourseStudentSub.getCourseId(), liveCastCourseStudentSub.getSubscribeNum());
    }
}
