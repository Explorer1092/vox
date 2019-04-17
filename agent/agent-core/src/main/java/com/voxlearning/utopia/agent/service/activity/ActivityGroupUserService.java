package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupUserDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroup;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupUser;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ActivityGroupUserService {

    @Inject
    private ActivityGroupDao groupDao;

    @Inject
    private ActivityGroupUserDao groupUserDao;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ActivityGroupUserStatisticsService groupUserStatisticsService;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    public void userJoinGroup(String groupId, Long joinUserId, Date joinTime, Boolean isLeader){

        ActivityGroup group = groupDao.loadByGid(groupId);
        if(group == null){
            return;
        }

        List<ActivityGroupUser> groupUserList = groupUserDao.loadByGid(groupId);
        if(CollectionUtils.isNotEmpty(groupUserList) && groupUserList.stream().anyMatch(p -> Objects.equals(p.getJoinUserId(), joinUserId))){
            return;
        }

        User platformUser = userLoaderClient.loadUser(joinUserId);
        if(platformUser == null){
            return;
        }

        ActivityGroupUser groupUser = new ActivityGroupUser();
        groupUser.setGroupId(groupId);
        groupUser.setJoinUserId(joinUserId);
        groupUser.setIsLeader(SafeConverter.toBoolean(isLeader));
        groupUser.setJoinTime(joinTime == null ? new Date() : joinTime);
        groupUser.setJoinUserRegTime(platformUser.getCreateTime());

        List<User> studentList = studentLoaderClient.loadParentStudents(joinUserId);
        if(CollectionUtils.isNotEmpty(studentList)){
            for(User student : studentList){
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
                if(studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null){
                    Long schoolId = studentDetail.getClazz().getSchoolId();
                    School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                    if(school != null){
                        groupUser.setSchoolId(schoolId);
                        groupUser.setSchoolName(school.getCname());
                        break;
                    }
                }
            }
        }


        groupUserDao.insert(groupUser);

        AlpsThreadPool.getInstance().submit(() -> groupUserStatisticsService.userCountStatistics(groupUser));
    }

    public List<ActivityGroupUser> loadByGidsAndTime(Collection<String> groupIds, Date startDate, Date endDate){
        Map<String, List<ActivityGroupUser>> groupUserMap = groupUserDao.loadByGids(groupIds);
        if(MapUtils.isEmpty(groupUserMap)){
            return new ArrayList<>();
        }
        return groupUserMap.values().stream().flatMap(List::stream).filter(p -> {
            if(p.getJoinTime() == null){
                return false;
            }
            if(startDate != null){
                if(endDate != null){
                    return p.getJoinTime().after(startDate) && p.getJoinTime().before(endDate);
                }else {
                    return p.getJoinTime().after(startDate);
                }
            }else {
                if(endDate != null){
                    return p.getJoinTime().before(endDate);
                }else {
                    return true;
                }
            }
        }).collect(Collectors.toList());
    }
}
