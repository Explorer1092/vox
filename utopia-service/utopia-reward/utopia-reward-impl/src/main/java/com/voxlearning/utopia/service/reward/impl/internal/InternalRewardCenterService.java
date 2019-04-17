package com.voxlearning.utopia.service.reward.impl.internal;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.HpPublicGoodMapper;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.HpPublicGoodPlaqueEntity;
import com.voxlearning.utopia.service.reward.entity.PublicGoodCollect;
import com.voxlearning.utopia.service.reward.entity.PublicGoodReward;
import com.voxlearning.utopia.service.reward.entity.RewardActivity;
import com.voxlearning.utopia.service.reward.entity.RewardActivityRecord;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodCollectDao;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodRewardDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardActivityRecordDao;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Named
public class InternalRewardCenterService extends SpringContainerSupport {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private RewardLoaderImpl rewardLoader;
    @Inject
    private RewardActivityRecordDao rewardActivityRecordDao;
    @Inject
    private PublicGoodCollectDao publicGoodCollectDao;
    @Inject
    private PublicGoodRewardDao publicGoodRewardDao;

    private IRedisCommands redisCommands;

    private static final String KEY_REMARD_RENAME_TIP = "REMARD_CENTRE_RENAME_TIP";

    @Override
    public void afterPropertiesSet() {
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    private String genKey(Object... keyParts){
        return KEY_REMARD_RENAME_TIP + ":" + StringUtils.join(keyParts,":");
    }

    /**
     * 前两次进入奖品中心提示
     * @param userId
     * @return
     */
    public boolean tryShowRenameTip(long userId) {
        int value = 0;
        try {
            RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
            RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
            String key = genKey(userId);

            Object obj = stringCommands.get(key);
            if (obj != null) {
                value = (Integer) obj;
            }
            if (value < 1) {
                stringCommands.set(key, 1);
                // 设置过期时间
                long ttl = keyCommands.ttl(key);
                if(ttl == -1){
                    keyCommands.expireat(key, com.voxlearning.alps.calendar.DateUtils.getTodayEnd());
                }
            }

        } catch (Exception e) {
            logger.warn(String.format("tryShowRenameTip warn userId:%s", userId), e);
        }

        return value < 1;
    }

    public long getIntegral(User user) {
        if (UserType.TEACHER.equals(user.fetchUserType())) {
            TeacherDetail teacherDetail;
            if (user instanceof TeacherDetail) {
                teacherDetail = (TeacherDetail) user;
            } else {
                teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            }
            if (teacherDetail.getUserIntegral() == null)
                return 0L;
            else
                return teacherDetail.getUserIntegral().getUsable();
        } else if (UserType.STUDENT.equals(user.fetchUserType())) {
            StudentDetail studentDetail;
            if (user instanceof StudentDetail) {
                studentDetail = (StudentDetail) user;
            } else
                studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            if (studentDetail.getUserIntegral() == null)
                return 0L;
            else
                return studentDetail.getUserIntegral().getUsable();
        } else
            return 0L;
    }

    public HpPublicGoodPlaqueEntity getPublicGoodPlaque(long userId) {
        HpPublicGoodPlaqueEntity entity = new HpPublicGoodPlaqueEntity();
        List<RewardActivityRecord> rewardActivityRecordList = rewardActivityRecordDao.loadUserRecords(userId);
        if (rewardActivityRecordList != null) {
            Set<Long> activityIdSet = rewardActivityRecordList
                    .stream()
                    .map(t -> t.getActivityId())
                    .collect(Collectors.toSet());
            if (activityIdSet != null) {
                entity.setJoinNum(activityIdSet.size());
            }
        }
        entity.setDonationIntegralNum(getDonationIntegralNum(userId));
        entity.setHonorNum(this.loadUserHonorNum(userId));
        return entity;
    }

    public long getDonationIntegralNum(long userId) {
        long donationIntegralNum = 0;
        List<RewardActivityRecord> rewardActivityRecordList = rewardActivityRecordDao.loadUserRecords(userId);
        if (rewardActivityRecordList != null && !rewardActivityRecordList.isEmpty()) {
            for (RewardActivityRecord record : rewardActivityRecordList) {
                if (record.getType() != 0) {
                    continue;
                }
                donationIntegralNum += record.getPrice();
            }
        }
        return donationIntegralNum;
    }

    private int loadUserHonorNum(long userId) {
        List<PublicGoodCollect>  publicGoodCollectList = publicGoodCollectDao.loadByUserId(userId);
        List<PublicGoodReward> publicGoodRewardList = publicGoodRewardDao.loadAll();
        if (publicGoodCollectList == null || publicGoodCollectList.isEmpty()
                || publicGoodRewardList == null || publicGoodRewardList.isEmpty() ) {
            return 0;
        }
        Map<Long, PublicGoodReward> publicGoodRewardMap = publicGoodRewardList
                .stream()

                .collect(toMap(PublicGoodReward::getId, Function.identity()));

        return (int) publicGoodCollectList
                .stream()
                .filter(t -> {
                    if (t.getRewardList() != null) {
                        t.getRewardList()
                                .stream()
                                .filter(reward -> publicGoodRewardMap.containsKey(reward.getId())
                                        && "CERT".equals(publicGoodRewardMap.get(reward.getId()).getType()));
                        return true;
                    }
                    return false;
                })
                .count();
    }

    public  List<HpPublicGoodMapper> getHomePagePublicGoodList() {
        // 筛选出来上线的
        List<HpPublicGoodMapper> mappers = rewardLoader.loadRewardActivities()
                .stream()
                .filter(RewardActivity::getOnline)
                // 按时间倒序，最新的在最前面
                // 这个改成按照排序值来排序
                // 未完成的排在前面，如果状态相同先看排序值，再按创建时间倒序
                .sorted((a1, a2) -> {
                    Integer a2Ow = SafeConverter.toInt(a2.getOrderWeights());
                    Integer a1Ow = SafeConverter.toInt(a1.getOrderWeights());

                    if(Objects.equals(a1.getStatus(),a2.getStatus())){
                        if(Objects.equals(a1Ow,a2Ow)){
                            return a2.getCreateDatetime().compareTo(a1.getCreateDatetime());
                        }else
                            return Integer.compare(a2Ow,a1Ow);
                    }else if(a1.isOnGoing()){
                        return -1;
                    }else{
                        return 1;
                    }
                })
                .map(activity -> {
                    HpPublicGoodMapper mapper = new HpPublicGoodMapper();
                    mapper.setStatus(activity.getStatus());
                    mapper.setPublicGoodId(activity.getId());
                    mapper.setPublicGoodPictuerUrl(activity.getImgUrl());
                    return mapper;
                })
                .collect(toList());
        return mappers;
    }

    public Long getDonationCount(Long userId) {
        List<RewardActivityRecord> rewardRecordList = rewardActivityRecordDao.loadUserRecords(userId);
        if (rewardRecordList == null) {
            return 0L;
        }
        long count = rewardRecordList.stream().filter(i -> !Objects.equals(i.getType(), 1)).count();
        return count;
    }
}
