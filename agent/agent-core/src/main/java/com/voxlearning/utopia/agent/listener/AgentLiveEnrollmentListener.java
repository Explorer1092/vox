package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.service.activity.ActivityOrderCourseService;
import com.voxlearning.utopia.agent.service.activity.ActivityOrderService;
import com.voxlearning.utopia.agent.service.activity.LiveEnrollmentService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * AgentWorkFlowQueueListener
 *
 * @author song.wang
 * @date 2016/12/27
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination( system = QueueSystem.KFK,config = "primary",queue = "live-cast-enrollment-activity-order"),
                @QueueDestination(system = QueueSystem.KFK,config = "main-backup", queue = "live-cast-enrollment-activity-order")
        }
)
public class AgentLiveEnrollmentListener extends SpringContainerSupport implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private LiveEnrollmentService liveEnrollmentService;
    @Inject
    private ActivityOrderService activityOrderService;
    @Inject
    private ActivityOrderCourseService activityOrderCourseService;
    @Inject
    private AgentActivityDao agentActivityDao;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }


        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "live-cast-enrollment-activity-order");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);

        String activityId = SafeConverter.toString(dataMap.get("activityId"), "");

        String orderId = SafeConverter.toString(dataMap.get("orderId"), "");
        Long orderUserId = SafeConverter.toLong(dataMap.get("platformPid"));
        Long studentId = SafeConverter.toLong(dataMap.get("platformSid"));

        BigDecimal orderPayAmount = new BigDecimal(SafeConverter.toLong(dataMap.get("payPrice")));

        long payTime = SafeConverter.toLong(dataMap.get("payTime"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(payTime);

        Long userId = SafeConverter.toLong(dataMap.get("clerkId"));

        if(StringUtils.isNotBlank(activityId)) {
            AgentActivity activity = agentActivityDao.load(activityId);
            if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
                return;
            }

            activityOrderService.handleListenerData(activityId, orderId, calendar.getTime(), orderPayAmount, Objects.equals(orderUserId, 0L) ? studentId : orderUserId, userId);

            List<Map<String, Object>> courseList = (List<Map<String, Object>>) dataMap.get("courseList");
            List<Map<String, Object>> courseInfoList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(courseList)) {
                courseList.forEach(p -> {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("courseId", SafeConverter.toString(p.get("courseSegmentId")));
                    courseInfo.put("courseName", SafeConverter.toString(p.get("courseName")));
                    courseInfo.put("isFirstActive", true);
                    courseInfoList.add(courseInfo);
                });
            }
            activityOrderCourseService.handleListenerData(orderId, studentId, courseInfoList);
        }

//        String deliveryId = SafeConverter.toString(map.get("deliveryId"));
//        String orderId = SafeConverter.toString(map.get("orderId"));
//        Long platformPid = SafeConverter.toLong(map.get("platformPid"));
//        Long studentId = SafeConverter.toLong(map.get("platformSid"));
//        Date payTime = SafeConverter.toDate(map.get("payTime"));
//        Long payPrice = SafeConverter.toLong(map.get("payPrice"));
//        Integer courseType = SafeConverter.toInt(map.get("courseType"),1);
//        String courseGrade = SafeConverter.toString(map.get("courseGrade"));
//        List<Integer> gradeList = StringUtils.toIntegerList(courseGrade);
//        String courseSubject = SafeConverter.toString(map.get("courseSubject"));
//        String courseName = SafeConverter.toString(map.get("courseName"));
//        String courseStage = SafeConverter.toString(map.get("courseStage"));
//
//
//        liveEnrollmentService.saveLiveEnrollmentOrder(deliveryId, orderId, payTime, platformPid, studentId,payPrice,courseType, gradeList, courseSubject, courseName, courseStage);

    }
}
