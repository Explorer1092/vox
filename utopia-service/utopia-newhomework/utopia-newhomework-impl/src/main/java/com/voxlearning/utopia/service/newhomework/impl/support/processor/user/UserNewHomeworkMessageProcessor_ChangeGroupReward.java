package com.voxlearning.utopia.service.newhomework.impl.support.processor.user;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkFinishRewardInParentAppDao;
import com.voxlearning.utopia.service.newhomework.impl.support.processor.UserNewHomeworkMessageAbstractProcessor;
import com.voxlearning.utopia.service.user.api.constants.UserNewHomeworkMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2016-10-13
 */
@Named
public class UserNewHomeworkMessageProcessor_ChangeGroupReward extends UserNewHomeworkMessageAbstractProcessor {

    @Inject
    private NewHomeworkFinishRewardInParentAppDao newHomeworkFinishRewardInParentAppDao;

    public UserNewHomeworkMessageProcessor_ChangeGroupReward() {
        this.messageType = UserNewHomeworkMessageType.CHANGE_GROUP_REWARD;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        userNewHomeworkMessageProcessorManager.register(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doProcess(Map<String, Object> map) {
        if (UserNewHomeworkMessageType.ofWithUnKnow(SafeConverter.toInt(map.get("messageType"))) != messageType) {
            return;
        }
        Map<String, Object> extInfo = (Map) (map.get("extInfo"));
        Set<Long> groupIdSet = ((List<Integer>) (extInfo.get("groupIdSet"))).stream().map(SafeConverter::toLong).collect(Collectors.toSet());
        Long studentId = SafeConverter.toLong(extInfo.get("studentId"));
        if (CollectionUtils.isEmpty(groupIdSet) || studentId < 1) {
            return;
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = newHomeworkFinishRewardInParentAppDao.load(studentId);
        //没有奖励
        if (rewardInParentApp == null) {
            return;
        }
        //没有未领取奖励
        if (MapUtils.isEmpty(rewardInParentApp.getNotReceivedRewardMap())) {
            return;
        }
        //所有未领取的学豆奖励
        //这里也把已经过期但是还没有被处理到timeOut里的数据处理一遍
        Map<String, NewHomeworkFinishRewardInParentApp.RewardDetail> notReceivedRewardMap = rewardInParentApp.getNotReceivedRewardMap();
        Set<String> timeOutRewardHomeworkIds = new HashSet<>();
        Set<String> changeGroupRewardHomeworkIds = new HashSet<>();
        for (String homeworkId : notReceivedRewardMap.keySet()) {
            NewHomeworkFinishRewardInParentApp.RewardDetail detail = notReceivedRewardMap.get(homeworkId);
            if (detail.getExpire().before(new Date())) {
                timeOutRewardHomeworkIds.add(homeworkId);
            } else if (groupIdSet.contains(detail.getGroupId())) {
                //未过期且是这个group的作业产生的奖励
                changeGroupRewardHomeworkIds.add(homeworkId);
            }
        }
        if (CollectionUtils.isNotEmpty(timeOutRewardHomeworkIds) || CollectionUtils.isNotEmpty(changeGroupRewardHomeworkIds)) {
            newHomeworkFinishRewardInParentAppDao.updateChangeGroupRewardInteger(studentId, timeOutRewardHomeworkIds, changeGroupRewardHomeworkIds);
        }
    }
}
