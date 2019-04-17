package com.voxlearning.utopia.service.reward.api.newversion;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.*;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.HpPublicGoodPlaqueEntity;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181217")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface RewardCenterService {

    Long getIntegral(User user);

    RewardHomePageTobySowMapper getHomePageTobyShow(User user);

    boolean isHasUnreadLeaveWord(long userId);

    List<HpPublicGoodMapper> getHomePagePublicGoodList();

    @Async
    AlpsFuture<Boolean> updatePowerPillarNum(long userId, int num);

    List<ZoneDynamicsMapper> loadZoneDynamics(long userId);

    List<LeaveWordMapper> loadLeaveWordList(long userId);

    List<LeaveWordMapper> loadUnreadLeaveWordList(long userId);

    LeaveWordGoodsListMapper loadLeaveWordGoodsList(User user);

    RewardCenterToby loadTobyByUserId(Long userId);

    MyTobyShowMapper loadMyTobyShow(User user);

    List<RewardCenterToby> loadAccessoryErrExpiryTime(Long timeStamp);
    List<RewardCenterToby> loadCountenanceErrExpiryTime(Long timeStamp);
    List<RewardCenterToby> loadImageErrExpiryTime(Long timeStamp);
    List<RewardCenterToby> loadPropsErrExpiryTime(Long timeStamp);

    MapMessage cancelTobyDress(Long id, Long userId);

    TobyAccessoryCVRecord loadUserNewestTobyAccessory(Long id, Long userId);
    TobyCountenanceCVRecord loadUserNewestTobyCountenance(Long id, Long userId);
    TobyImageCVRecord loadUserNewestTobyImage(Long id, Long userId);
    TobyPropsCVRecord loadUserNewestTobyProps(Long id, Long userId);

    RewardCenterToby upsertToby(RewardCenterToby toby);

    List<MyBackpackMapper> loadMyBackpack(User user);

    TryPowerPizeMapper tryPowerPize(User user);

    PowerPizePoolMapper loadPowerPrizePool(User user);

    List<PowerPrize> loadAllPowerPrize();

    PowerPrize loadPowerById(long id);

    void updatePowerPrize(PowerPrize powerPrize);

    void deletePowerPrize(long id);

    Boolean isOwnTobyDress(Long userId, Long productId);

    Map<Long, TobyCountenanceCVRecord> loadTobyCountenanceListByUserId(long userId);
    Map<Long, TobyImageCVRecord> loadTobyImageListByUserId(long userId);
    Map<Long, TobyPropsCVRecord> loadTobyPropsListByUserId(long userId);
    Map<Long, TobyAccessoryCVRecord> loadTobyAccessoryListByUserId(long userId);

    Map<Long, TobyCountenanceCVRecord> loadTobyCountenanceListByExpireTimeRegion(Long userId, Long startTime, Long endTime);
    Map<Long, TobyImageCVRecord> loadTobyImageListByExpireTimeRegion(Long userId, Long startTime, Long endTime);
    Map<Long, TobyPropsCVRecord> loadTobyPropsListByByExpireTimeRegion(Long userId, Long startTime, Long endTime);
    Map<Long, TobyAccessoryCVRecord> loadTobyAccessoryListByExpireTimeRegion(Long userId, Long startTime, Long endTime);

    HpPublicGoodPlaqueEntity getPublicGoodPlaque(long userId);

    Long getDonationCount(Long userId);

    TobyDressMapper loadTobyDress();

    List<TobyDress>  loadTobyDressByIds(Collection<Long> ids);

    TobyDress loadTobyDressById(Long id);

    TobyDress upsertTobyDress(TobyDress tobyDress);

    List<RewardCenterToby> loadClassmateTobyList(Set<Long> userIdList);

    RewardCenterToby loadClassmateToby(long userId);

    MapMessage leaveWord(User user, long businessUserId, long leaveWordId);

    List<PrizeClawMapper> loadPrizeClawGame(int site);

    Boolean isShowNewcomersTip(long userId);

    Boolean tryGameOneDayLimit(Long userId);

    PrizeClawWinningRecordMapper loadPrizeClawWinningRecord(long userId);

    Boolean prizeClawJudge(User user, long id);

    MapMessage clawJudge(User user, long id);
    @Async
    AlpsFuture<Boolean> updateAvaterType(long userId, int type);

    Boolean isTobyAvatarType(long userId);

    void ctTobyDress(long userId, RewardProductDetail productDetail);

    boolean checkCost(User user, Map<Long, Integer> productIdSpandTypeMap);
}
