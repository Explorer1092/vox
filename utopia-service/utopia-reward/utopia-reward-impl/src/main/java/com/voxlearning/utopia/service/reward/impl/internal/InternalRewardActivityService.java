package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.utopia.service.reward.entity.RewardActivity;
import com.voxlearning.utopia.service.reward.entity.RewardActivityImage;
import com.voxlearning.utopia.service.reward.entity.RewardActivityRecord;
import com.voxlearning.utopia.service.reward.impl.dao.RewardActivityDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardActivityImageDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardActivityRecordDao;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * Created by haitian.gan on 2017/2/4.
 */
@Named
public class InternalRewardActivityService {

    @Inject private RewardActivityDao rewardActivityDao;
    @Inject private RewardActivityRecordDao rewardActivityRecordDao;
    @Inject private RewardActivityImageDao rewardActivityImageDao;

    public List<RewardActivity> loadRewardActivities(){
        return rewardActivityDao.findAll();
    }

    public RewardActivity loadRewardActivity(Long id){
        return rewardActivityDao.load(id);
    }

/*    public List<RewardActivityRecord> loadRewardActivityRecords(Long activityId,Long userId){
        return rewardActivityRecordDao.loadActivityRecordsUnderUser(activityId,userId);
    }*/

/*    public List<RewardActivityRecord> loadRecentActivityRecords(Long activityId,int limit){
        return rewardActivityRecordDao.loadActivityRecords(activityId,limit);
    }*/

    public List<RewardActivityImage> loadActivityImages(Long activityId){
        return rewardActivityImageDao.loadActivityImages(activityId);
    }

    public List<RewardActivityRecord> loadUserRecords(Long userId){
        return rewardActivityRecordDao.loadUserRecords(userId);
    }

/*    public List<RewardActivityRecord> loadUserRecordsInDay(Long userId,Date date){
        return rewardActivityRecordDao.loadUserRecordsInDay(userId,date);
    }*/

    public RewardActivityImage upsertActivityImage(RewardActivityImage image){
        if(image == null)
            return null;

        RewardActivityImage upserted = rewardActivityImageDao.upsert(image);
        return upserted;
    }

    public RewardActivityImage loadActivityImage(Long id) {
        return rewardActivityImageDao.load(id);
    }

    public boolean deleteActivityImage(Long id) {
        return rewardActivityImageDao.remove(id);
    }

/*    public List<RewardActivityRecord> loadUserCollectRecords(Long userId) {
        return rewardActivityRecordDao.loadUserCollectRecords(userId);
    }*/
}
