package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkCrmService;
import com.voxlearning.utopia.service.newhomework.api.mapper.RepairHomeworkDataParam;

import java.util.Collection;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/11/8
 */
public class NewHomeworkCrmServiceClient implements NewHomeworkCrmService {

    @ImportService(interfaceClass = NewHomeworkCrmService.class)
    private NewHomeworkCrmService remoteReference;

    @Override
    public MapMessage changeHomeworkEndTime(Date searchStartDate, Date searchEndDate, Date endTime) {
        return remoteReference.changeHomeworkEndTime(searchStartDate, searchEndDate, endTime);
    }

    @Override
    public MapMessage changeHomeworkEndTime(String homeworkId, Date endTime) {
        return remoteReference.changeHomeworkEndTime(homeworkId, endTime);
    }

    @Override
    public MapMessage addHomeworkRewardInParentApp(Long userId, String homeworkId, Long groupId, Integer integralCount, Date expire) {
        return remoteReference.addHomeworkRewardInParentApp(userId, homeworkId, groupId, integralCount, expire);
    }

    @Override
    public MapMessage repairSelfStudyCorrectHomework(String homeworkId, Long studentId) {
        return remoteReference.repairSelfStudyCorrectHomework(homeworkId, studentId);
    }


    @Override
    public boolean repairOcrMentalPractiseImage(String homeworkId, Long userId,String processId) {
        return remoteReference.repairOcrMentalPractiseImage(homeworkId,userId,processId);
    }

    @Override
    public boolean repairOcrDictationPracticeImage(String homeworkId, Long userId, String processId) {
        return remoteReference.repairOcrDictationPracticeImage(homeworkId, userId, processId);
    }

    @Override
    public MapMessage resumeNewHomework(String homeworkId) {
        return remoteReference.resumeNewHomework(homeworkId);
    }

    @Override
    public MapMessage crmResendDubbingSynthetic(Collection<String> ids){
        return remoteReference.crmResendDubbingSynthetic(ids);
    }

    @Override
    public MapMessage repairHomeworkData(RepairHomeworkDataParam param) {
        return remoteReference.repairHomeworkData(param);
    }

    @Override
    public MapMessage resumeBasicReviewHomework(String packageId) {
        return remoteReference.resumeBasicReviewHomework(packageId);
    }

    @Override
    public boolean addNewHomeworkBlackWhiteList(String businessType, String idType, String blackWhiteId) {
        return remoteReference.addNewHomeworkBlackWhiteList(businessType, idType, blackWhiteId);
    }

    @Override
    public boolean deleteNewHomeworkBlackWhiteList(String id) {
        return remoteReference.deleteNewHomeworkBlackWhiteList(id);
    }
}
