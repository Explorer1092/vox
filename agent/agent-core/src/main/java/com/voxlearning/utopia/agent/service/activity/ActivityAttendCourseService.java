package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.dao.mongo.activity.*;
import com.voxlearning.utopia.agent.persist.entity.activity.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
public class ActivityAttendCourseService {


    @Inject
    private ActivityAttendCourseDao activityAttendCourseDao;
    @Inject
    private ActivityAttendCourseStatisticsService statisticsService;
    @Inject
    private ActivityOrderCourseDao orderCourseDao;
    @Inject
    private ActivityCardCourseDao cardCourseDao;
    @Inject
    private ActivityOrderDao orderDao;
    @Inject
    private ActivityCardDao cardDao;

    public void handleListenerData(Long studentId, String courseId, Date attendTime){

        if(studentId == null || StringUtils.isBlank(courseId)){
            return;
        }
        if(attendTime == null){
            attendTime = new Date();
        }

        List<ActivityOrderCourse> orderCourseList = orderCourseDao.loadBySidAndCid(studentId, courseId);

        List<ActivityCardCourse> cardCourseList = cardCourseDao.loadBySidAndCid(studentId, courseId);

        ActivityOrderCourse orderCourse = null;
        if(CollectionUtils.isNotEmpty(orderCourseList)){
            orderCourseList.sort((o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? -1 : 1);
            orderCourse = orderCourseList.get(0);
        }

        ActivityCardCourse cardCourse = null;
        if(CollectionUtils.isNotEmpty(cardCourseList)){
            cardCourseList.sort((o1, o2) -> o1.getCreateTime().after(o2.getCreateTime()) ? -1 : 1);
            cardCourse = cardCourseList.get(0);
        }


        String activityId = "";
        String relatedId = "";
        if(orderCourse == null && cardCourse == null){
            return;
        }else if(cardCourse == null){
            ActivityOrder order = orderDao.loadByOid(orderCourse.getOrderId());
            if(order != null){
                activityId = order.getActivityId();
                relatedId = order.getOrderId();
            }
        }else if(orderCourse == null){
            ActivityCard card = cardDao.loadByCn(cardCourse.getCardNo());
            if(card != null){
                activityId = card.getActivityId();
                relatedId = card.getCardNo();
            }
        }else {
            if(orderCourse.getCreateTime().after(cardCourse.getCreateTime())){
                // order
                ActivityOrder order = orderDao.loadByOid(orderCourse.getOrderId());
                if(order != null){
                    activityId = order.getActivityId();
                    relatedId = order.getOrderId();
                }
            }else {
                // card
                ActivityCard card = cardDao.loadByCn(cardCourse.getCardNo());
                if(card != null){
                    activityId = card.getActivityId();
                    relatedId = card.getCardNo();
                }
            }
        }

        if(StringUtils.isBlank(activityId) || StringUtils.isBlank(relatedId)){
            return;
        }

        ActivityAttendCourse attendCourse = new ActivityAttendCourse();
        attendCourse.setActivityId(activityId);
        attendCourse.setRelatedId(relatedId);
        attendCourse.setStudentId(studentId);
        attendCourse.setCourseId(courseId);
        attendCourse.setAttendTime(attendTime);
        activityAttendCourseDao.insert(attendCourse);

        AlpsThreadPool.getInstance().submit(() -> statisticsService.attendCourseStatistics(attendCourse));
    }
}
