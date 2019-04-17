package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderCourseDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderStudentDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderCourse;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderStudent;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "live-cast-enrollment-activity-order-delete"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "live-cast-enrollment-activity-order-delete"),
        }
)
public class LiveCastOrderDeleteListener extends SpringContainerSupport implements MessageListener {


    @Inject
    private ActivityOrderDao orderDao;
    @Inject
    private ActivityOrderCourseDao orderCourseDao;
    @Inject
    private ActivityOrderStudentDao orderStudentDao;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        LoggerUtils.info("live-cast-enrollment-activity-order-delete", dataMap);

        // staging环境仅打日志
        if(RuntimeMode.isStaging()){
            return;
        }

        String orderId = SafeConverter.toString(dataMap.get("orderId"), "");
        if(StringUtils.isNotBlank(orderId)){
            List<ActivityOrderCourse> courseList = orderCourseDao.loadByOid(orderId);
            if(CollectionUtils.isNotEmpty(courseList)){
                Set<String> ids = courseList.stream().map(ActivityOrderCourse::getId).collect(Collectors.toSet());
                orderCourseDao.removes(ids);
            }

            List<ActivityOrderStudent> studentList = orderStudentDao.loadByOid(orderId);
            if(CollectionUtils.isNotEmpty(studentList)){
                Set<String> ids = studentList.stream().map(ActivityOrderStudent::getId).collect(Collectors.toSet());
                orderStudentDao.removes(ids);
            }

            ActivityOrder order = orderDao.loadByOid(orderId);
            if(order != null){
                orderDao.remove(order.getId());
            }
        }


    }
}
