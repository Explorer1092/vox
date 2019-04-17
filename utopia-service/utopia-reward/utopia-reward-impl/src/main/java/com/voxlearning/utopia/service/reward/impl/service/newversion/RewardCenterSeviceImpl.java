package com.voxlearning.utopia.service.reward.impl.service.newversion;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.*;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.*;
import com.voxlearning.utopia.service.reward.api.newversion.RewardCenterService;
import com.voxlearning.utopia.service.reward.constant.TwoLevelCategoryType;
import com.voxlearning.utopia.service.reward.entity.Debris;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.impl.internal.*;
import com.voxlearning.utopia.service.reward.impl.service.DebrisServiceImpl;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.StudentLoader;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.dateToString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Named("com.voxlearning.utopia.service.reward.impl.service.newversion.RewardCenterSeviceImpl")
@ExposeService(interfaceClass = RewardCenterService.class)
public class RewardCenterSeviceImpl extends SpringContainerSupport implements RewardCenterService {

    @Inject
    private InternalRewardCenterService internalRewardCenterService;
    @Inject
    private InternalLeaveWordService internalLeaveWordService;
    @Inject
    private InternalTobyService internalTobyService;
    @Inject
    private InternalGameService internalGameService;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private InternalPowerPillarService internalPowerPillarService;
    @Inject
    private InternalAvatarService internalAvatarService;
    @Inject
    private StudentLoader studentLoader;
    @Inject
    private InternalBackPackService internalBackPackService;
    @Inject
    private NewRewardLoaderImpl newRewardLoader;
    @Inject
    private DebrisServiceImpl debrisService;

    @Override
    public Long getIntegral(User user) {
        return internalRewardCenterService.getIntegral(user);
    }

    @Override
    public RewardHomePageTobySowMapper getHomePageTobyShow(User user) {
        RewardHomePageTobySowMapper mapper = new RewardHomePageTobySowMapper();
        mapper.setIsShowRenameTip(internalRewardCenterService.tryShowRenameTip(user.getId()));
        mapper.setIsHasNewLeaveWord(internalLeaveWordService.isHasUnreadLeaveWord(user.getId()));
        mapper.setIsProductArea(!internalPowerPillarService.isRealGoodsOfflineCity((StudentDetail)user));
        PowerPillar powerPillar = internalPowerPillarService.getPowerPillar(user.getId());
        int powerPillarNum = powerPillar==null || powerPillar.getPowerPillar()==null ? 0:powerPillar.getPowerPillar();
        mapper.setIntegralNum(internalRewardCenterService.getIntegral(user));
        mapper.setPowerPillar(powerPillarNum);
        boolean isPowerFull = internalPowerPillarService.isPowerFull(powerPillar);
        mapper.setIsPoweFull(isPowerFull);
        mapper.setFullPowerNumber(internalPowerPillarService.getFullPowerNumber(powerPillar));
        RewardCenterToby toby = internalTobyService.loadTobyByUserId(user.getId());
        if (toby != null) {
            HpMyTobyEntity entity = new HpMyTobyEntity();
            HpMyTobyEntity.Accessory accessory = entity.new Accessory();
            HpMyTobyEntity.Countenance countenance = entity.new Countenance();
            HpMyTobyEntity.Image image = entity.new Image();
            HpMyTobyEntity.Props props = entity.new Props();

            accessory.setId(toby.getAccessoryId());
            accessory.setUrl(toby.getAccessoryUrl());
            countenance.setId(toby.getCountenanceId());
            countenance.setUrl(toby.getCountenanceUrl());
            image.setId(toby.getImageId());
            image.setUrl(toby.getImageUrl());
            props.setId(toby.getPropsId());
            props.setUrl(toby.getPropsUrl());

            entity.setAccessory(accessory);
            entity.setCountenance(countenance);
            entity.setImage(image);
            entity.setProps(props);

            mapper.setToby(entity);
        }
        mapper.setPublicGoodPlaque(internalRewardCenterService.getPublicGoodPlaque(user.getId()));
        return mapper;
    }

    @Override
    public TryPowerPizeMapper tryPowerPize(User user) {
        TryPowerPizeMapper mapper = new TryPowerPizeMapper();
        internalPowerPillarService.tryPowerPrize(user, mapper);
        return mapper;
    }

    @Override
    public PowerPizePoolMapper loadPowerPrizePool(User user) {
        return internalPowerPillarService.loadPowerPrizePool(user);
    }

    @Override
    public List<PowerPrize> loadAllPowerPrize() {
        return internalPowerPillarService.loadAllPowerPrize();
    }

    @Override
    public PowerPrize loadPowerById(long id) {
        return internalPowerPillarService.loadPowerById(id);
    }


    @Override
    public void updatePowerPrize(PowerPrize powerPrize) {
        internalPowerPillarService.updatePowerPrize(powerPrize);
    }

    @Override
    public void deletePowerPrize(long id) {
        internalPowerPillarService.deletePowerPrize(id);
    }

    @Override
    public Boolean isOwnTobyDress(Long userId, Long productId) {
        return internalTobyService.isOwn(userId, productId);
    }

    @Override
    public boolean isHasUnreadLeaveWord(long userId) {
        return internalLeaveWordService.isHasUnreadLeaveWord(userId);
    }

    @Override
    public List<HpPublicGoodMapper> getHomePagePublicGoodList() {
        return internalRewardCenterService.getHomePagePublicGoodList();
    }

    @Override
    public AlpsFuture<Boolean> updatePowerPillarNum(long userId, int num) {
        return new ValueWrapperFuture<>(internalPowerPillarService.addPowerPillarNum(userId, num));
    }

    private Map<Long, User> clazzStudentMap(Long clazzId) {
        if (clazzId == null) {
            return Collections.emptyMap();
        }
        Map<Long, User> result = Collections.emptyMap();
        Map<Long, List<User>> clazzStudentMap = studentLoaderClient.loadClazzStudents(Collections.singleton(clazzId));
        if (clazzStudentMap != null && !clazzStudentMap.isEmpty()) {
            result = clazzStudentMap.get(clazzId)
                    .stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
        }
        return result;
    }

    private LeaveWordMapper cvLeaveWordMapper(LeaveWord leaveWord, Map<Long, LeaveWordGoods> goodsMap, Map<Long, User> clazzStudentMap) {
        LeaveWordMapper mapper = new LeaveWordMapper();
        mapper.setCreateTime(leaveWord.getCreateTime());
        mapper.setLeaveWordId(leaveWord.getLeaveWordGoodsId());
        long visitorUserId = leaveWord.getVisitorUserId();
        if (clazzStudentMap != null && clazzStudentMap.containsKey(visitorUserId)) {
            mapper.setVisitorPortraitUrl(clazzStudentMap.get(visitorUserId).fetchImageUrl());
            mapper.setVisitorUserName(clazzStudentMap.get(visitorUserId).fetchRealname());
        }
        if (goodsMap != null && !goodsMap.isEmpty()) {
            mapper.setLeaveWordPortraitUrl(goodsMap.get(leaveWord.getLeaveWordGoodsId()).getPortraitUrl());
            mapper.setLeaveWordName(goodsMap.get(leaveWord.getLeaveWordGoodsId()).getName());
        }
        mapper.setVisitorUserId(leaveWord.getVisitorUserId());
        return mapper;
    }

    @Override
    public List<LeaveWordMapper> loadUnreadLeaveWordList(long userId) {
        List<LeaveWordMapper> result = new ArrayList<>();
        StudentDetail student = studentLoader.loadStudentDetail(userId);
        if (student == null) {
            logger.warn(String.format("StudentDetail is empty userId:%s", userId));
            return null;
        }
        List<LeaveWord> leaveWordList = internalLeaveWordService.loadUnreadLeaveWord(userId);
        if (leaveWordList != null && !leaveWordList.isEmpty()) {
            Map<Long, LeaveWordGoods> goodsMap = internalLeaveWordService.loadAllLeaveWordGoodsMap();
            Map<Long, User> clazzStudentMap = this.clazzStudentMap(student.getClazzId());
            result = leaveWordList
                    .stream()
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .filter(t -> t.getIsRead() == null || !t.getIsRead())
                    .map(t -> {
                        LeaveWordMapper mapper =  cvLeaveWordMapper(t, goodsMap, clazzStudentMap);
                        return mapper;
                    }).collect(Collectors.toList());
            if (result != null && result.size() > 10) {
                result = result.subList(0, 9);
            }
        }
        return result;
    }

    @Override
    public List<LeaveWordMapper> loadLeaveWordList(long userId) {
        List<LeaveWordMapper> result = new ArrayList<>();
        List<LeaveWord> leaveWordList = internalLeaveWordService.loadLeaveWord(userId);
        Map<Long, LeaveWordGoods> goodsMap = internalLeaveWordService.loadAllLeaveWordGoodsMap();
        StudentDetail student = studentLoader.loadStudentDetail(userId);
        if (student == null) {
            logger.warn(String.format("StudentDetail is empty userId:%s", userId));
            return result;
        }
        Map<Long, User> clazzStudentMap = this.clazzStudentMap(student.getClazzId());
        if (leaveWordList != null && !leaveWordList.isEmpty()) {
            result = leaveWordList
                    .stream()
                    .map(t -> {
                        LeaveWordMapper mapper =  cvLeaveWordMapper(t, goodsMap, clazzStudentMap);
                        return mapper;
                    }).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<ZoneDynamicsMapper> loadZoneDynamics(long userId) {
        List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> entityList = loadLeaveWordEntityList(userId);
        List<ZoneDynamicsMapper> result = convertGroupByDateYear(entityList);
        List<Long> idList = entityList
                .stream()
                .map(t -> t.getLeaveWordId())
                .collect(Collectors.toList());
        internalLeaveWordService.updateToReadAlready(userId, new HashSet<>(idList));
        return result;
    }

    public List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> loadLeaveWordEntityList(long userId) {
        List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> result = new ArrayList<>();
        List<LeaveWord> leaveWordList = internalLeaveWordService.loadLeaveWord(userId);
        Map<Long, LeaveWordGoods> goodsMap = internalLeaveWordService.loadAllLeaveWordGoodsMap();
        StudentDetail student = studentLoader.loadStudentDetail(userId);
        if (student == null) {
            logger.warn(String.format("StudentDetail is empty userId:%s", userId));
            return result;
        }
        Map<Long, User> clazzStudentMap = this.clazzStudentMap(student.getClazzId());
        if (leaveWordList != null && !leaveWordList.isEmpty()) {
            result = leaveWordList
                    .stream()
                    .map(t -> {
                        ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity mapper = new ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity();
                        mapper.setCreateTime(t.getCreateTime());
                        mapper.setHourTime(DateFormatUtils.format(t.getCreateTime(), "HH:mm"));
                        mapper.setLeaveWordId(t.getLeaveWordGoodsId());
                        long visitorUserId = t.getVisitorUserId();
                        if (clazzStudentMap != null && clazzStudentMap.containsKey(visitorUserId)) {
                            mapper.setVisitorPortraitUrl(clazzStudentMap.get(visitorUserId).fetchImageUrl());
                            mapper.setVisitorUserName(clazzStudentMap.get(visitorUserId).fetchRealname());
                        }
                        if (goodsMap != null && !goodsMap.isEmpty()) {
                            mapper.setLeaveWordPortraitUrl(goodsMap.get(t.getLeaveWordGoodsId()).getPortraitUrl());
                            mapper.setLeaveWordName(goodsMap.get(t.getLeaveWordGoodsId()).getName());
                        }
                        mapper.setVisitorUserId(t.getVisitorUserId());
                        return mapper;
                    }).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * 把 mongo 中的原始数据按照年和日期分组并转化为前台可用的格式
     */
    private List<ZoneDynamicsMapper> convertGroupByDateYear(List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }

        Function<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity, String> groupByYear = feed -> dateToString(feed.getCreateTime(), "yyyy");

        final String dateFormat = "MM-dd";
        Date now = new Date();
        String today = dateToString(now, dateFormat);
        String yesterday = dateToString(DateUtils.addDays(now, -1), dateFormat);

        Function<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity, String> groupByDay = feed -> {
            String timeString = dateToString(feed.getCreateTime(), "MM-dd");
            if (today.equals(timeString)) {
                return "今天";
            } else if (yesterday.equals(timeString)) {
                return "昨天";
            } else {
                return timeString;
            }
        };

        // 按年分组
        LinkedHashMap<String, List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity>> yearAllData =
                entityList.stream().sorted(feedCreateDesc).collect(groupingBy(groupByYear, LinkedHashMap::new, toList()));

        List<ZoneDynamicsMapper> yearMapperList = new ArrayList<>();

        //年 动态
        for (Map.Entry<String, List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity>> stringListEntry : yearAllData.entrySet()) {
            // 专心处理某一年的
            List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> yearData = stringListEntry.getValue();
            List<ZoneDynamicsMapper.LeaveWordDay> yearDataList = new ArrayList<>();
            String year = stringListEntry.getKey();
            ZoneDynamicsMapper mapper = new ZoneDynamicsMapper();
            mapper.setYear(year);
            mapper.setYearDataList(yearDataList);

            // 按天分组
            LinkedHashMap<String, List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity>> dayAllData =
                    yearData.stream().sorted(feedCreateDesc).collect(groupingBy(groupByDay, LinkedHashMap::new, toList()));

            for (Map.Entry<String, List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity>> entry : dayAllData.entrySet()) {
                String day = entry.getKey();
                List<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> dayData = entry.getValue();
                ZoneDynamicsMapper.LeaveWordDay dayDataMapper = new ZoneDynamicsMapper.LeaveWordDay();
                dayDataMapper.setDate(day);
                dayDataMapper.setDayDataList(dayData);
                yearDataList.add(dayDataMapper);
            }
            yearMapperList.add(mapper);
        }
        return yearMapperList;
    }

    private static Comparator<ZoneDynamicsMapper.LeaveWordDay.LeaveWordEntity> feedCreateDesc = (o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime());

    @Override
    public LeaveWordGoodsListMapper loadLeaveWordGoodsList(User user) {
        LeaveWordGoodsListMapper mapper = new LeaveWordGoodsListMapper();
        List<LeaveWordGoodsListEntity> entityList = internalLeaveWordService.loadAllLeaveWordGoods()
                .stream()
                .map(t -> {
                    LeaveWordGoodsListEntity entity = new LeaveWordGoodsListEntity();
                    entity.setId(t.getId());
                    entity.setName(t.getName());
                    entity.setPortraitUrl(t.getPortraitUrl());
                    entity.setPrice(t.getPrice());
                    entity.setSpendType(t.getSpendType());
                    return entity;
                }).collect(Collectors.toList());
        mapper.setLeaveWordGoodsList(entityList);
        long integralNum = internalRewardCenterService.getIntegral(user);
        Debris debris = debrisService.loadDebrisByUserId(user.getId());
        mapper.setFragmentNum(debris.getUsableDebris());
        mapper.setIntegralNum(integralNum);
        return mapper;
    }

    @Override
    public MapMessage leaveWord(User user, long businessUserId, long leaveWordId) {
        return internalLeaveWordService.doLeaveWord(user, businessUserId, leaveWordId);
    }

    @Override
    public RewardCenterToby loadTobyByUserId(Long userId) {
        return internalTobyService.loadTobyByUserId(userId);
    }

    @Override
    public MyTobyShowMapper loadMyTobyShow(User user) {
        MyTobyShowMapper mapper = new MyTobyShowMapper();
        boolean hasExpiryDress = false;
        boolean isChangeAvatar;
        List<String> expiryDressNameList = new ArrayList<>();
        RewardCenterToby toby = internalTobyService.loadTobyByUserId(user.getId());
        long integralNum = internalRewardCenterService.getIntegral(user);
        Debris debris = debrisService.loadDebrisByUserId(user.getId());

        if (toby.isExpiry(toby.getImageExpiryTimeStamp())) {
            if (toby.getImageId() != null) {
                RewardProduct image = internalTobyService.loadProductById(toby.getImageId());
                if (image != null) {
                    hasExpiryDress = true;
                    expiryDressNameList.add(image.getProductName());
                }
            }
            RewardCenterToby.defaultImage(toby);
        }
        if (toby.isExpiry(toby.getCountenanceExpiryTimeStamp())) {
            if (toby.getCountenanceId() != null) {
                RewardProduct countenance = internalTobyService.loadProductById(toby.getCountenanceId());
                if (countenance != null) {
                    hasExpiryDress = true;
                    expiryDressNameList.add(countenance.getProductName());
                }
            }
            RewardCenterToby.defaultCountenance(toby);
        }
        if (toby.isExpiry(toby.getPropsExpiryTimeStamp())) {
            if (toby.getPropsId() != null) {
                RewardProduct props = internalTobyService.loadProductById(toby.getPropsId());
                if (props != null) {
                    hasExpiryDress = true;
                    expiryDressNameList.add(props.getProductName());
                }
            }
            RewardCenterToby.defaultProps(toby);
        }
        if (toby.isExpiry(toby.getAccessoryExpiryTimeStamp())) {
            if (toby.getAccessoryId() != null) {
                RewardProduct accessory = internalTobyService.loadProductById(toby.getAccessoryId());
                if (accessory != null) {
                    hasExpiryDress = true;
                    expiryDressNameList.add(accessory.getProductName());
                }
            }
            RewardCenterToby.defaultAccessory(toby);
        }
        if (hasExpiryDress) {
            internalTobyService.upsertToby(toby);
        }

        if (expiryDressNameList != null && !expiryDressNameList.isEmpty()) {
            mapper.setExpiryDressNameList(expiryDressNameList);
        }
        if (internalAvatarService.isTobyAvatarType(user.getId()) && hasExpiryDress) {
            isChangeAvatar = true;
            mapper.setIsChangeAvatar(isChangeAvatar);
        }
        TobyEntity entity = new TobyEntity();
        TobyEntity.Image image = entity.new Image();
        TobyEntity.Countenance countenance = entity.new Countenance();
        TobyEntity.Props props = entity.new Props();
        TobyEntity.Accessory accessory = entity.new Accessory();
        entity.setAccessory(accessory);
        entity.setCountenance(countenance);
        entity.setImage(image);
        entity.setProps(props);
        image.setId(toby.getImageId());
        image.setUrl(toby.getImageUrl());
        countenance.setId(toby.getCountenanceId());
        countenance.setUrl(toby.getCountenanceUrl());
        props.setId(toby.getPropsId());
        props.setUrl(toby.getPropsUrl());
        accessory.setId(toby.getAccessoryId());
        accessory.setUrl(toby.getAccessoryUrl());

        mapper.setToby(entity);
        mapper.setFragmentNum(debris.getUsableDebris());
        mapper.setIntegralNum(integralNum);
        return mapper;
    }

    @Override
    public List<RewardCenterToby> loadAccessoryErrExpiryTime(Long timeStamp) {
        return internalTobyService.loadAccessoryErrExpiryTime(timeStamp);
    }

    @Override
    public List<RewardCenterToby> loadCountenanceErrExpiryTime(Long timeStamp) {
        return internalTobyService.loadCountenanceErrExpiryTime(timeStamp);
    }

    @Override
    public List<RewardCenterToby> loadImageErrExpiryTime(Long timeStamp) {
        return internalTobyService.loadImageErrExpiryTime(timeStamp);
    }

    @Override
    public List<RewardCenterToby> loadPropsErrExpiryTime(Long timeStamp) {
        return internalTobyService.loadPropsErrExpiryTime(timeStamp);
    }

    @Override
    public MapMessage cancelTobyDress(Long id, Long userId) {
        return internalTobyService.cancelTobyDress(id, userId);
    }

    @Override
    public TobyAccessoryCVRecord loadUserNewestTobyAccessory(Long id, Long userId) {
        return internalTobyService.loadUserNewestTobyAccessory(id, userId);
    }

    @Override
    public TobyCountenanceCVRecord loadUserNewestTobyCountenance(Long id, Long userId) {
        return internalTobyService.loadUserNewestTobyCountenance(id, userId);
    }

    @Override
    public TobyImageCVRecord loadUserNewestTobyImage(Long id, Long userId) {
        return internalTobyService.loadUserNewestTobyImage(id, userId);
    }

    @Override
    public TobyPropsCVRecord loadUserNewestTobyProps(Long id, Long userId) {
        return internalTobyService.loadUserNewestTobyProps(id, userId);
    }

    @Override
    public RewardCenterToby upsertToby(RewardCenterToby toby) {
        return internalTobyService.upsertToby(toby);
    }

    @Override
    public List<MyBackpackMapper> loadMyBackpack(User user) {
        List<MyBackpackMapper> mappers = new ArrayList<>();
        List<PowerPrizeRecord> recordList = internalBackPackService.loadRecordByUserId(user.getId());
        if (recordList != null && !recordList.isEmpty()) {
            mappers = recordList
                    .stream()
                    .map(t -> {
                        MyBackpackMapper mapper = new MyBackpackMapper();
                        mapper.setFragmentNum(t.getFragmentNum());
                        mapper.setPrizeId(t.getPrizeId());
                        mapper.setPrizeName(t.getPrizeName());
                        mapper.setPrizePictuerUrl(t.getPictuerUrl());
                        mapper.setPrizeType(t.getPrizeType());
                        return mapper;
                    }).collect(toList());
        }
        return mappers;
    }

    @Override
    public Map<Long, TobyCountenanceCVRecord> loadTobyCountenanceListByUserId(long userId) {
        Map<Long, TobyCountenanceCVRecord> result = null;
        List<TobyCountenanceCVRecord> countenanceCVRecordList = internalTobyService.loadTobyCountenanceListByUserId(userId);
        RewardCenterToby myToby = this.loadTobyByUserId(userId);
        if (countenanceCVRecordList != null && !countenanceCVRecordList.isEmpty()) {
            result = countenanceCVRecordList
                    .stream()
                    .filter(t -> t.getExpiryTime() > new Date().getTime())
                    .map(t -> {
                        if (myToby != null && Objects.equals(myToby.getCountenanceId(), t.getCountenanceId())) {
                            t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                        }
                        return t;
                    })
                    .collect(Collectors.toMap(t -> t.getCountenanceId(), Function.identity(), (v1, v2) -> v2));

        }
        return result;
    }

    @Override
    public Map<Long, TobyImageCVRecord> loadTobyImageListByUserId(long userId) {
        Map<Long, TobyImageCVRecord> result = null;
        List<TobyImageCVRecord> imageCVRecordList = internalTobyService.loadTobyImageListByUserId(userId);
        RewardCenterToby myToby = this.loadTobyByUserId(userId);
        if (imageCVRecordList != null && !imageCVRecordList.isEmpty()) {
            result = imageCVRecordList
                    .stream()
                    .filter(t -> t.getExpiryTime() > new Date().getTime())
                    .map(t -> {
                        if (myToby != null && Objects.equals(myToby.getImageId(), t.getImageId())) {
                            t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                        }
                        return t;
                    })
                    .collect(Collectors.toMap(t -> t.getImageId(), Function.identity(), (v1, v2) -> v2));

        }
        return result;
    }

    @Override
    public Map<Long, TobyPropsCVRecord> loadTobyPropsListByUserId(long userId) {
        Map<Long, TobyPropsCVRecord> result = null;
        List<TobyPropsCVRecord> propsCVRecordList = internalTobyService.loadTobyPropsListByUserId(userId);
        RewardCenterToby myToby = this.loadTobyByUserId(userId);
        if (propsCVRecordList != null && !propsCVRecordList.isEmpty()) {
            result = propsCVRecordList
                    .stream()
                    .filter(t -> t.getExpiryTime() > new Date().getTime())
                    .map(t -> {
                        if (myToby != null && myToby.getCountenanceId() == t.getPropsId()) {
                            t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                        }
                        return t;
                    })
                    .collect(Collectors.toMap(t -> t.getPropsId(), Function.identity(), (v1, v2) -> v2));

        }
        return result;
    }

    @Override
    public Map<Long, TobyAccessoryCVRecord> loadTobyAccessoryListByUserId(long userId) {
        Map<Long, TobyAccessoryCVRecord> result = null;
        List<TobyAccessoryCVRecord> accessoryCVRecordList = internalTobyService.loadTobyAccessoryListByUserId(userId);
        RewardCenterToby myToby = this.loadTobyByUserId(userId);
        if (accessoryCVRecordList != null && !accessoryCVRecordList.isEmpty()) {
            result = accessoryCVRecordList
                    .stream()
                    .filter(t -> t.getExpiryTime() > new Date().getTime())
                    .map(t -> {
                        if (myToby != null && Objects.equals(myToby.getCountenanceId(), t.getAccessoryId())) {
                            t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                        }
                        return t;
                    })
                    .collect(Collectors.toMap(t -> t.getAccessoryId(), Function.identity(), (v1, v2) -> v2));

        }
        return result;
    }

    @Override
    public Map<Long, TobyCountenanceCVRecord> loadTobyCountenanceListByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        List<TobyCountenanceCVRecord> countenanceCVRecordList = internalTobyService.loadTobyCountenanceListByUserId(userId);
        if (CollectionUtils.isEmpty(countenanceCVRecordList)) {
            return Collections.emptyMap();
        }
        return countenanceCVRecordList.stream()
                .filter(record -> Objects.nonNull(record.getExpiryTime()) && record.getExpiryTime() > startTime && record.getExpiryTime() < endTime)
                .collect(Collectors.toMap(t -> t.getCountenanceId(), Function.identity(), (v1, v2) -> v2));
    }

    @Override
    public Map<Long, TobyImageCVRecord> loadTobyImageListByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        List<TobyImageCVRecord> imageCVRecordList = internalTobyService.loadTobyImageListByUserId(userId);
        if (CollectionUtils.isEmpty(imageCVRecordList)) {
            return Collections.emptyMap();
        }
        return imageCVRecordList.stream()
                .filter(record -> Objects.nonNull(record.getExpiryTime()) && record.getExpiryTime() > startTime && record.getExpiryTime() < endTime)
                .collect(Collectors.toMap(t -> t.getImageId(), Function.identity(), (v1, v2) -> v2));
    }

    @Override
    public Map<Long, TobyPropsCVRecord> loadTobyPropsListByByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        List<TobyPropsCVRecord> propsCVRecordList = internalTobyService.loadTobyPropsListByUserId(userId);
        if (CollectionUtils.isEmpty(propsCVRecordList)) {
            return Collections.emptyMap();
        }
        return propsCVRecordList.stream()
                .filter(record -> Objects.nonNull(record.getExpiryTime()) && record.getExpiryTime() > startTime && record.getExpiryTime() < endTime)
                .collect(Collectors.toMap(t -> t.getPropsId(), Function.identity(), (v1, v2) -> v2));
    }

    @Override
    public Map<Long, TobyAccessoryCVRecord> loadTobyAccessoryListByExpireTimeRegion(Long userId, Long startTime, Long endTime) {
        List<TobyAccessoryCVRecord> accessoryCVRecordList = internalTobyService.loadTobyAccessoryListByUserId(userId);
        if (CollectionUtils.isEmpty(accessoryCVRecordList)) {
            return Collections.emptyMap();
        }
        return accessoryCVRecordList.stream()
                .filter(record -> Objects.nonNull(record.getExpiryTime()) && record.getExpiryTime() > startTime && record.getExpiryTime() < endTime)
                .collect(Collectors.toMap(t -> t.getAccessoryId(), Function.identity(), (v1, v2) -> v2));
    }

    @Override
    public HpPublicGoodPlaqueEntity getPublicGoodPlaque(long userId) {
        return internalRewardCenterService.getPublicGoodPlaque(userId);
    }

    @Override
    public Long getDonationCount(Long userId) {
        return internalRewardCenterService.getDonationCount(userId);
    }

    @Override
    public TobyDressMapper loadTobyDress() {
        TobyDressMapper mapper = new TobyDressMapper();
        List<TobyDress> tobyDresses = internalTobyService.loadTobyDresses();

        List<TobyDressMapper.TobyDress> tobyDressList = new ArrayList<>();

        tobyDressList.addAll(tobyDresses
                .stream()
                .map(t -> {
                    TobyDressMapper.TobyDress tobyDress = mapper.new TobyDress();
                    tobyDress.setName(t.getName());
                    tobyDress.setUrl(t.getUrl());
                    tobyDress.setType(t.getType());
                    tobyDress.setId(t.getId());
                    return tobyDress;
                }).collect(Collectors.toList()));
        mapper.setTobyDressList(tobyDressList);
        return mapper;
    }

    @Override
    public List<TobyDress> loadTobyDressByIds(Collection<Long> ids) {
        List<TobyDress> tobyDresses = internalTobyService.loadTobyDresses();
        return tobyDresses.stream().filter(tobyDress -> ids.contains(tobyDress.getId())).collect(toList());
    }

    @Override
    public TobyDress loadTobyDressById(Long id) {
        return internalTobyService.loadTobyDressById(id);
    }

    @Override
    public TobyDress upsertTobyDress(TobyDress tobyDress) {
        return internalTobyService.upsertTobyDress(tobyDress);
    }

    @Override
    public List<RewardCenterToby> loadClassmateTobyList(Set<Long> userIdList) {
        List<RewardCenterToby> result = internalTobyService.loadTobyByUserIdList(userIdList);
        if (result == null || result.isEmpty()) {
            logger.warn(String.format("loadTobyByUserIdList is empty userIdList:%s", userIdList.toString()));
            return result;
        }
        return result;
    }

    @Override
    public RewardCenterToby loadClassmateToby(long userId) {
        return internalTobyService.loadTobyByUserId(userId);
    }

    @Override
    public List<PrizeClawMapper> loadPrizeClawGame(int site) {
        List<PrizeClawMapper> mappers = new ArrayList<>();
        List<PrizeClaw> prizeClaws = internalGameService.loadGameBySite(site);
        if (prizeClaws !=null && !prizeClaws.isEmpty()) {
            mappers = prizeClaws.stream().map(t -> {
                PrizeClawMapper mapper = new PrizeClawMapper();
                mapper.setId(t.getId());
                mapper.setPrizeName(t.getPrizeName());
                mapper.setPrizeType(t.getPrizeType());
                mapper.setPrizePicterUrl(t.getPrizePicterUrl());
                return mapper;
            }).collect(Collectors.toList());
        }
        return mappers;
    }

    @Override
    public Boolean isShowNewcomersTip(long userId) {
        return internalGameService.tryShowTip(userId);
    }

    @Override
    public Boolean tryGameOneDayLimit(Long userId) {
        return internalGameService.tryGameOneDayLimit(userId);
    }

    @Override
    public PrizeClawWinningRecordMapper loadPrizeClawWinningRecord(long userId) {
        PrizeClawWinningRecordMapper mapper = new PrizeClawWinningRecordMapper();
        List<PrizeClawWinningRecordEntity> entityList = new ArrayList<>();
        List<PrizeClawWinningRecord>  recordList = internalGameService.loadPrizeClawWinningRecord(userId);
        if (recordList != null && !recordList.isEmpty()) {
            recordList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).forEach(t -> {
                PrizeClawWinningRecordEntity entity = new PrizeClawWinningRecordEntity();
                //非积分奖励不统计总获得积分且中奖一栏显示奖品名称
                mapper.setConsumeSum(SafeConverter.toInt(mapper.getConsumeSum()) + t.getConsumeNum());
                if (t.getIsPrize()) {
                    if (t.getPrizeType() == PrizeClaw.PrizeType.INTEGRAL.getType()) {
                        mapper.setWinningSum(SafeConverter.toInt(mapper.getWinningSum()) + t.getPrize().intValue());
                        entity.setPrize(t.getPrize().toString());
                    } else {
                        entity.setPrize(t.getPrizeName());
                    }
                    entity.setConsumeNum(t.getConsumeNum());
                    entity.setCreateTime(DateUtils.dateToString(t.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                    entityList.add(entity);
                }
            });
            mapper.setPrizeRecordList(entityList);
        }
        return mapper;
    }

    @Override
    public Boolean prizeClawJudge(User user, long id) {
        return internalGameService.prizeClawJudge(user, id);
    }

    @Override
    public MapMessage clawJudge(User user, long id) {
        MapMessage resultMsg = MapMessage.successMessage();
        Long integral = this.getIntegral(user);
        PrizeClaw prizeClaw = internalGameService.loadOne(id);
        if (prizeClaw == null) {
            resultMsg.errorMessage("场次错误！");
            return resultMsg;
        }
        if (prizeClaw.getConsumerNum() > integral) {
            resultMsg.errorMessage("学豆不足！");
            return resultMsg;
        }
        resultMsg.add("isPrize", internalGameService.prizeClawJudge(user, id));
        resultMsg.add("isOneDayLimit", internalGameService.tryGameOneDayLimitAndAdd(user.getId()));
        return resultMsg;
    }

    @Override
    public AlpsFuture<Boolean> updateAvaterType(long userId, int type) {
        return new ValueWrapperFuture<>(internalAvatarService.setAvatarType(userId, type));
    }

    @Override
    public Boolean isTobyAvatarType(long userId) {
        return internalAvatarService.isTobyAvatarType(userId);
    }

    @Override
    public void ctTobyDress(long userId, RewardProductDetail productDetail) {
        TobyDress tobyDress = internalTobyService.loadTobyDressById(NumberUtils.toLong(productDetail.getRelateVirtualItemId()));
        String url = tobyDress.getUrl();
        long activeTimeMs = new Date().getTime() + productDetail.getExpiryDate() * 24 * 60 * 60 * 1000;
        Set<Long> categoryIdSet = newRewardLoader.getProductCategoryRefBuffer().getProductCategory(productDetail.getId());
        Set<Integer> twoLevelCategoryTypeSet = categoryIdSet.stream().map(categoryId -> {
            ProductCategory category = newRewardLoader.getProductCategoryBuffer().load(categoryId);
            if (Objects.nonNull(category)) {
                return category.getTwoLevelCategoryType();
            }
            return 0;
        }).collect(Collectors.toSet());

        if (twoLevelCategoryTypeSet.contains(TwoLevelCategoryType.TOBY_ACCESSORY.intType())) {
            internalTobyService.cvTobyAccessory(productDetail.getId(), userId, activeTimeMs, url);
        } else if (twoLevelCategoryTypeSet.contains(TwoLevelCategoryType.TOBY_COUNTENANCE.intType())) {
            internalTobyService.cvTobyCountenance(productDetail.getId(), userId, activeTimeMs, url);
        } else if (twoLevelCategoryTypeSet.contains(TwoLevelCategoryType.TOBY_IMG.intType())) {
            internalTobyService.cvTobyImage(productDetail.getId(), userId, activeTimeMs, url, productDetail.getProductName());
        } else if (twoLevelCategoryTypeSet.contains(TwoLevelCategoryType.TOBY_PROPS.intType())) {
            internalTobyService.cvTobyProps(productDetail.getId(), userId, activeTimeMs, url);
        } else {
            logger.warn("tobyDressType error productDetail:{}", productDetail.toString());
        }
    }

    @Override
    public boolean checkCost(User user, Map<Long, Integer> productIdSpandNumMap) {
        if (productIdSpandNumMap == null || productIdSpandNumMap.isEmpty()) {
            return false;
        }

        int consumerIntegralNum = 0;
        int consumerFragmentNum = 0;

        for (Map.Entry<Long, Integer> entry : productIdSpandNumMap.entrySet()) {
            RewardProduct product = internalTobyService.loadProductById(entry.getKey());
            if (product == null) {
                continue;
            }
            int spendNum = entry.getValue();
            if (product.getSpendType() == RewardProduct.SpendType.FRAGMENT.intValue()) {
                if (spendNum == 1) {
                    consumerFragmentNum += product.getPriceOldS().intValue();
                } else {
                    BigDecimal price = new BigDecimal( product.getPriceOldS()).multiply(new BigDecimal(2)).multiply(new BigDecimal(0.9));
                    int totalPrice = price.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    consumerFragmentNum += totalPrice;
                }
            } else {
                if (spendNum == 1) {
                    consumerIntegralNum += product.getPriceOldS().intValue();
                } else {
                    BigDecimal price = new BigDecimal( product.getPriceOldS()).multiply(new BigDecimal(2)).multiply(new BigDecimal(0.9));
                    int totalPrice = price.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    consumerIntegralNum += totalPrice;
                }
            }
        }

        long integralNum = internalRewardCenterService.getIntegral(user);
        Debris debris = debrisService.loadDebrisByUserId(user.getId());
        if (consumerIntegralNum == 0 && consumerFragmentNum == 0) {//没有兑换需求
            return true;
        }
        if (consumerIntegralNum > 0 && consumerFragmentNum <= 0) {//只有兑换学豆需求
            if (integralNum >= consumerIntegralNum) {
                return true;
            }
        }
        if (consumerFragmentNum > 0 && consumerIntegralNum <= 0) {//只有碎片兑换需求
            if (debris.getUsableDebris() >= consumerFragmentNum) {
                return true;
            }
        }
        if (consumerFragmentNum > 0 && consumerIntegralNum > 0) {//碎片、学豆都需要兑换
            if (integralNum >= consumerIntegralNum
                    && debris.getUsableDebris() >= consumerFragmentNum) {
                return true;
            }
        }
        return false;
    }
}
