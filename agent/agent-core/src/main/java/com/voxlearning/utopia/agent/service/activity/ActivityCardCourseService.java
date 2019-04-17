package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCardCourseDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCardDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCard;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardCourse;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
public class ActivityCardCourseService {

    @Inject
    private ActivityCardCourseDao cardCourseDao;
    @Inject
    private ActivityCardDao cardDao;

    public void handleListenerData(String cardNo, Long studentId, List<String> courseIds){
        if(StringUtils.isBlank(cardNo) || studentId == null || CollectionUtils.isEmpty(courseIds)){
            return;
        }

        ActivityCard card = cardDao.loadByCn(cardNo);
        if(card == null){
            return;
        }

        List<ActivityCardCourse> cardCourseList = cardCourseDao.loadByOid(cardNo);
        if(CollectionUtils.isNotEmpty(cardCourseList)){
            return;
        }

        List<ActivityCardCourse> dataList = new ArrayList<>();
        courseIds.forEach(p -> {
            ActivityCardCourse cardCourse = new ActivityCardCourse();
            cardCourse.setCardNo(cardNo);
            cardCourse.setStudentId(studentId);
            cardCourse.setCourseId(p);

            dataList.add(cardCourse);
        });

        if(CollectionUtils.isNotEmpty(dataList)){
            cardCourseDao.inserts(dataList);
        }
    }
}
