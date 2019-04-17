package com.voxlearning.utopia.service.reward.consumer.newversion;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.*;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.*;
import com.voxlearning.utopia.service.reward.api.newversion.RewardCenterService;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Named
public class RewardCenterClient implements RewardCenterService {

    @ImportService(interfaceClass = RewardCenterService.class)
    private RewardCenterService remoteReference;

    @Override
    public Long getIntegral(User user) {
        return remoteReference.getIntegral(user);
    }

    @Override
    public RewardHomePageTobySowMapper getHomePageTobyShow(User user) {
        return remoteReference.getHomePageTobyShow(user);
    }

    @Override
    public boolean isHasUnreadLeaveWord(long userId) {
        return remoteReference.isHasUnreadLeaveWord(userId);
    }

    @Override
    public List<HpPublicGoodMapper> getHomePagePublicGoodList() {
        return remoteReference.getHomePagePublicGoodList();
    }
    @Override
    public AlpsFuture<Boolean> updatePowerPillarNum(long userId, int num) {
        return remoteReference.updatePowerPillarNum(userId, num);
    }

    @Override
    public List<LeaveWordMapper> loadLeaveWordList(long userId) {
        return remoteReference.loadLeaveWordList(userId);
    }

    @Override
    public List<LeaveWordMapper> loadUnreadLeaveWordList(long userId) {
        return remoteReference.loadUnreadLeaveWordList(userId);
    }

    @Override
    public List<ZoneDynamicsMapper> loadZoneDynamics(long userId) {
        return remoteReference.loadZoneDynamics(userId);
    }

    @Override
    public LeaveWordGoodsListMapper loadLeaveWordGoodsList(User user) {
        return remoteReference.loadLeaveWordGoodsList(user);
    }

    @Override
    public RewardCenterToby loadTobyByUserId(Long userId) {
        return remoteReference.loadTobyByUserId(userId);
    }

    @Override
    public MyTobyShowMapper loadMyTobyShow(User user) {
        return remoteReference.loadMyTobyShow(user);
    }

    @Override
    public RewardCenterToby upsertToby(RewardCenterToby toby) {
        return remoteReference.upsertToby(toby);
    }

    @Override
    public List<RewardCenterToby> loadAccessoryErrExpiryTime(Long timeStamp) {
        return remoteReference.loadAccessoryErrExpiryTime(timeStamp);
    }

    @Override
    public List<RewardCenterToby> loadCountenanceErrExpiryTime(Long timeStamp) {
        return remoteReference.loadCountenanceErrExpiryTime(timeStamp);
    }

    @Override
    public List<RewardCenterToby> loadImageErrExpiryTime(Long timeStamp) {
        return remoteReference.loadImageErrExpiryTime(timeStamp);
    }

    @Override
    public List<RewardCenterToby> loadPropsErrExpiryTime(Long timeStamp) {
        return remoteReference.loadPropsErrExpiryTime(timeStamp);
    }

    @Override
    public MapMessage cancelTobyDress(Long id, Long userId) {
        return remoteReference.cancelTobyDress(id, userId);
    }

    @Override
    public TobyAccessoryCVRecord loadUserNewestTobyAccessory(Long id, Long userId) {
        return remoteReference.loadUserNewestTobyAccessory(id, userId);
    }

    @Override
    public TobyCountenanceCVRecord loadUserNewestTobyCountenance(Long id, Long userId) {
        return remoteReference.loadUserNewestTobyCountenance(id, userId);
    }

    @Override
    public TobyImageCVRecord loadUserNewestTobyImage(Long id, Long userId) {
        return remoteReference.loadUserNewestTobyImage(id, userId);
    }

    @Override
    public TobyPropsCVRecord loadUserNewestTobyProps(Long id, Long userId) {
        return remoteReference.loadUserNewestTobyProps(id, userId);
    }

    @Override
    public List<MyBackpackMapper> loadMyBackpack(User user) {
        return remoteReference.loadMyBackpack(user);
    }

    @Override
    public TryPowerPizeMapper tryPowerPize(User user) {
        return remoteReference.tryPowerPize(user);
    }

    @Override
    public PowerPizePoolMapper loadPowerPrizePool(User user) {
        return remoteReference.loadPowerPrizePool(user);
    }

    @Override
    public List<PowerPrize> loadAllPowerPrize() {
        return remoteReference.loadAllPowerPrize();
    }

    @Override
    public PowerPrize loadPowerById(long id) {
        return remoteReference.loadPowerById(id);
    }

    @Override
    public void updatePowerPrize(PowerPrize powerPrize) {
        remoteReference.updatePowerPrize(powerPrize);
    }

    @Override
    public void deletePowerPrize(long id) {
        remoteReference.deletePowerPrize(id);
    }

    @Override
    public Boolean isOwnTobyDress(Long userId, Long productId) {
        return remoteReference.isOwnTobyDress(userId, productId);
    }

    @Override
    public Map<Long, TobyCountenanceCVRecord> loadTobyCountenanceListByUserId(long userId) {
        return remoteReference.loadTobyCountenanceListByUserId(userId);
    }

    @Override
    public Map<Long, TobyImageCVRecord> loadTobyImageListByUserId(long userId) {
        return remoteReference.loadTobyImageListByUserId(userId);
    }

    @Override
    public Map<Long, TobyPropsCVRecord> loadTobyPropsListByUserId(long userId) {
        return remoteReference.loadTobyPropsListByUserId(userId);
    }

    @Override
    public Map<Long, TobyAccessoryCVRecord> loadTobyAccessoryListByUserId(long userId) {
        return remoteReference.loadTobyAccessoryListByUserId(userId);
    }

    @Override
    public Map<Long, TobyCountenanceCVRecord> loadTobyCountenanceListByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        return remoteReference.loadTobyCountenanceListByExpireTimeRegion(userId, startTime, endTime);
    }

    @Override
    public Map<Long, TobyImageCVRecord> loadTobyImageListByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        return remoteReference.loadTobyImageListByExpireTimeRegion(userId, startTime, endTime);
    }

    @Override
    public Map<Long, TobyPropsCVRecord> loadTobyPropsListByByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        return remoteReference.loadTobyPropsListByByExpireTimeRegion(userId, startTime, endTime);
    }

    @Override
    public Map<Long, TobyAccessoryCVRecord> loadTobyAccessoryListByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        return remoteReference.loadTobyAccessoryListByExpireTimeRegion(userId, startTime, endTime);
    }

    @Override
    public HpPublicGoodPlaqueEntity getPublicGoodPlaque(long userId) {
        return remoteReference.getPublicGoodPlaque(userId);
    }

    @Override
    public Long getDonationCount(Long userId) {
        return remoteReference.getDonationCount(userId);
    }

    @Override
    public TobyDressMapper loadTobyDress() {
        return remoteReference.loadTobyDress();
    }

    @Override
    public List<TobyDress> loadTobyDressByIds(Collection<Long> ids) {
        return remoteReference.loadTobyDressByIds(ids);
    }

    @Override
    public TobyDress loadTobyDressById(Long id) {
        return remoteReference.loadTobyDressById(id);
    }

    @Override
    public TobyDress upsertTobyDress(TobyDress tobyDress) {
        return remoteReference.upsertTobyDress(tobyDress);
    }

    @Override
    public List<RewardCenterToby> loadClassmateTobyList(Set<Long> userIdList) {
        return remoteReference.loadClassmateTobyList(userIdList);
    }

    @Override
    public RewardCenterToby loadClassmateToby(long userId) {
        return remoteReference.loadClassmateToby(userId);
    }

    @Override
    public MapMessage leaveWord(User user, long businessUserId, long leaveWordId) {
         return remoteReference.leaveWord(user, businessUserId, leaveWordId);
    }

    @Override
    public List<PrizeClawMapper> loadPrizeClawGame(int site) {
        return remoteReference.loadPrizeClawGame(site);
    }

    @Override
    public Boolean isShowNewcomersTip(long userId) {
        return remoteReference.isShowNewcomersTip(userId);
    }

    @Override
    public Boolean tryGameOneDayLimit(Long userId) {
        return remoteReference.tryGameOneDayLimit(userId);
    }

    @Override
    public PrizeClawWinningRecordMapper loadPrizeClawWinningRecord(long userId) {
        return remoteReference.loadPrizeClawWinningRecord(userId);
    }

    @Override
    public Boolean prizeClawJudge(User user, long id) {
        return remoteReference.prizeClawJudge(user, id);
    }

    @Override
    public MapMessage clawJudge(User user, long id) {
        return remoteReference.clawJudge(user, id);
    }

    @Override
    public AlpsFuture<Boolean> updateAvaterType(long userId, int type) {
        return remoteReference.updateAvaterType(userId, type);
    }

    @Override
    public Boolean isTobyAvatarType(long userId) {
        return remoteReference.isTobyAvatarType(userId);
    }

    @Override
    public void ctTobyDress(long userId, RewardProductDetail productDetail) {
        remoteReference.ctTobyDress(userId, productDetail);
    }

    @Override
    public boolean checkCost(User user, Map<Long, Integer> productIdSpandTypeMap) {
        return remoteReference.checkCost(user, productIdSpandTypeMap);
    }
}
