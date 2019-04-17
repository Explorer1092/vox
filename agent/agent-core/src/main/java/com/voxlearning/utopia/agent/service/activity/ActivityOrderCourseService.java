package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderCourseDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderCourse;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class ActivityOrderCourseService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ActivityOrderCourseDao orderCourseDao;
    @Inject
    private ActivityOrderStudentService activityOrderStudentService;


    // courseActivationInfo :  保存订单激活的订单信息，  key: 课程ID  value:是否是首次激活
    public void handleListenerData(String orderId, Long studentId, List<Map<String, Object>> courseInfoList){
        if(StringUtils.isBlank(orderId) || studentId == null || CollectionUtils.isEmpty(courseInfoList)){
            return;
        }

        List<ActivityOrderCourse> orderCourseList = orderCourseDao.loadByOid(orderId);
        if(CollectionUtils.isNotEmpty(orderCourseList)){
            return;
        }

        List<ActivityOrderCourse> dataList = new ArrayList<>();
        courseInfoList.forEach(p -> {
            String courseId = SafeConverter.toString(p.get("courseId"));
            if(StringUtils.isBlank(courseId)){
                return;
            }
            ActivityOrderCourse item = new ActivityOrderCourse();
            item.setOrderId(orderId);
            item.setStudentId(studentId);
            item.setCourseId(courseId);
            item.setCourseName(SafeConverter.toString(p.get("courseName")));
            item.setFirstActivation(SafeConverter.toBoolean(p.get("isFirstActive"), true));
            dataList.add(item);
        });

        if(CollectionUtils.isNotEmpty(dataList)){
            orderCourseDao.inserts(dataList);
        }

        // 保存订单和学生的对应关系
        AlpsThreadPool.getInstance().submit(() -> activityOrderStudentService.saveOrderStudentByOid(orderId, studentId));
    }

    public Map<String, List<ActivityOrderCourse>> getOrderCourseByOids(Collection<String> orderIds){
        if(CollectionUtils.isEmpty(orderIds)){
            return Collections.emptyMap();
        }
        return orderCourseDao.loadByOids(orderIds);
    }


}
