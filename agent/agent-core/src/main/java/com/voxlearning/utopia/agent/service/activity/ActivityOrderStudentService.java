package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderStudentDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderStudent;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class ActivityOrderStudentService {

    @Inject
    private ActivityOrderStudentDao activityOrderStudentDao;

    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    public void saveOrderStudentByOid(String orderId, Long studentId){
        if(StringUtils.isBlank(orderId)){
            return;
        }

        if(studentId == null){
            return;
        }

        List<ActivityOrderStudent> orderStudentList = activityOrderStudentDao.loadByOid(orderId);
        if(CollectionUtils.isNotEmpty(orderStudentList)){
            if(orderStudentList.stream().anyMatch(p -> Objects.equals(p.getStudentId(), studentId))) {
                return;
            }
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if(studentDetail == null){
            return;
        }

        ActivityOrderStudent orderStudent = new ActivityOrderStudent();
        orderStudent.setOrderId(orderId);

        orderStudent.setStudentId(studentDetail.getId());
        if(studentDetail.getProfile() != null){
            orderStudent.setStudentName(studentDetail.getProfile().getRealname());
        }
        if(studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null){
            Long schoolId = studentDetail.getClazz().getSchoolId();
            School school =schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            if(school != null){
                orderStudent.setSchoolId(schoolId);
                orderStudent.setSchoolName(school.getCname());
            }
        }
        activityOrderStudentDao.insert(orderStudent);
    }

    public Map<String, List<ActivityOrderStudent>> getOrderStudentByOids(Collection<String> orderIds){
        if(CollectionUtils.isEmpty(orderIds)){
            return Collections.emptyMap();
        }
        return activityOrderStudentDao.loadByOids(orderIds);
    }
}
