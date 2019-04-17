/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.nekketsu.base.cache.NekketsuCacheSystem;
import com.voxlearning.utopia.service.nekketsu.impl.queue.ParkourQueueSender;
import com.voxlearning.utopia.service.nekketsu.parkour.api.NekketsuParkourService;
import com.voxlearning.utopia.service.nekketsu.parkour.dao.*;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.*;
import com.voxlearning.utopia.service.nekketsu.parkour.misc.ParkouMiscUtil;
import com.voxlearning.utopia.service.nekketsu.parkour.misc.ParkourPlayLogHandler;
import com.voxlearning.utopia.service.nekketsu.parkour.net.messages.ExchangeLoginRewardRequest;
import com.voxlearning.utopia.service.nekketsu.parkour.net.messages.GetLoginRewardListRequest;
import com.voxlearning.utopia.service.nekketsu.parkour.net.messages.SaveGameResultRequest;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.*;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.*;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Sadi.Wan
 * @since 2014/8/19
 */
@Named
@Service(interfaceClass = NekketsuParkourService.class)
@ExposeService(interfaceClass = NekketsuParkourService.class)
public class NekketsuParkourServiceImpl extends SpringContainerSupport implements NekketsuParkourService {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private NekketsuCacheSystem nekketsuCacheSystem;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;

    @Inject
    private ParkourRoleDao roleDao;

    @Inject
    private ParkourStageDao stageDao;

    @Inject
    private PersonalStageRecordDao personalStageRecordDao;

    protected CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

    @Inject
    private LevelSpeedInfoDao levelSpeedInfoDao;

    @Inject
    private ParkourRegionRankDao rankDao;

    @Inject
    private ParkourLoginPrizeDao parkourLoginPrizeDao;

    @Inject
    private ParkourShopItemDao parkourShopItemDao;

    @Inject private ParkourQueueSender parkourQueueSender;

    @Override
    public List<LevelSpeedInfo> loadLevelSpeed() {
        return levelSpeedInfoDao.findAll().stream()
                .sorted((o1, o2) -> Integer.compare(o1.getLevel(), o2.getLevel()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, LoginPrizeMonthConf> loadLoginPrizeConf() {
        return loginPrizeMonthConfDao.findThisAndNextMonth();
    }

    @Override
    public List<ParkourStage> replaceAll(List<ParkourStage> toSave) {
        return stageDao.replaceAll(toSave);
    }

    @Override
    public List<ParkourStage> listAllStage() {
        return stageDao.findAll().stream()
                .sorted((o1, o2) -> Integer.compare(o1.getStageId(), o2.getStageId()))
                .collect(Collectors.toList());
    }

    @Inject
    private LoginPrizeMonthConfDao loginPrizeMonthConfDao;

    @Inject private ParkourPlayLogHandler parkourPlayLogHandler;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    public MapMessage initInfo(StudentDetail studentDetail, Gender gender, boolean isPaidUser) {
        String roleCacheKey = ParkourRole.ck_id(studentDetail.getId());
        Map<String, CacheObject<Object>> fromCache = nekketsuCacheSystem.CBS.flushable.gets(Arrays.asList(ParkourStage.ck_all_count(), LevelSpeedInfo.ck_all(), roleCacheKey));
        if (fromCache == null) {
            fromCache = Collections.emptyMap();
        }
        MapMessage rtn = MapMessage.successMessage();
        rtn.add("stageCount", fromCache.containsKey(ParkourStage.ck_all_count()) ? fromCache.get(ParkourStage.ck_all_count()).getValue() : stageDao.count());
        ParkourRoleInfo role = new ParkourRoleInfo();
        ParkourRole roleMe = fromCache.containsKey(roleCacheKey) ? (ParkourRole) fromCache.get(roleCacheKey).getValue() : roleDao.load(studentDetail.getId());
        if (isPaidUser) {//付费用户，则把所有未兑换的学豆和金币都兑现
            roleMe = exchangeIntegralAndCoinForNewlyPaidUser(roleMe, studentDetail);
        }
        role.fillFrom(roleMe);
        role.img = getUserImageUrl(studentDetail.fetchImageUrl());
        role.roleName = studentDetail.fetchRealname();
        rtn.add("roleInfo", role);
        List<LevelSpeedInfo> speedConf = fromCache.containsKey(LevelSpeedInfo.ck_all()) ? (List<LevelSpeedInfo>) fromCache.get(LevelSpeedInfo.ck_all()).getValue() : levelSpeedInfoDao.findAll();
        List<LevelSpeed> levelSpeedNet = new ArrayList<>(speedConf.size());
        for (LevelSpeedInfo info : speedConf) {
            LevelSpeed ls = new LevelSpeed();
            ls.fillFrom(info);
            levelSpeedNet.add(ls);
        }
        rtn.add("levelSpeedConf", levelSpeedNet);

        //累计登录奖励
        ParkourLoginPrize thisMonthPrize = parkourLoginPrizeDao.findAndInsertThisMonthPrizeOnNonExist(studentDetail.getId());
        if (null != thisMonthPrize) {
            if (CollectionUtils.isNotEmpty(thisMonthPrize.getPrizeDetailList())) {
                for (LoginPrizeDetail lpd : thisMonthPrize.getPrizeDetailList()) {
                    if (!lpd.isExchanged()) {//如果本月还有没领的奖，弹
                        rtn.put("showPrizeList", true);
                        break;
                    }
                }
            }
            Calendar cal = Calendar.getInstance();
            //这个月第一次登录。或者今天还没登录过。发奖
            if (thisMonthPrize.getPrizeDetailList().isEmpty()
                    || SafeConverter.toInt(thisMonthPrize.getLatestLoginDay()) < cal.get(Calendar.DAY_OF_MONTH)) {
                LoginPrizeMonthConf thisMonthConf = loginPrizeMonthConfDao.findConfForNow();
                //这个月还有的领，累进奖励还没领完，发奖
                if (null != thisMonthConf && thisMonthPrize.getPrizeDetailList().size() < thisMonthConf.getPrizeItemId().get(gender).size()) {
                    parkourLoginPrizeDao.pushPrize(thisMonthPrize.getId(), new LoginPrizeDetail(DateUtils.dateToString(cal.getTime()), false));
                    rtn.put("showPrizeList", true);
                } else {//没的领了，只更新登陆日期
                    parkourLoginPrizeDao.updateLatestLoginDay(thisMonthPrize.getId());
                }
            }
        }

        List<ParkourShopItem> shopItemList = parkourShopItemDao.loadWithCache();
        List<CoinShopItem> coinShopItemList = new ArrayList<>();
        for (ParkourShopItem item : shopItemList) {
            if (Gender.NOT_SURE == item.getGender()) {
                CoinShopItem shopItem = new CoinShopItem();
                shopItem.fillFrom(item);
                coinShopItemList.add(shopItem);
            }
        }
        rtn.add("shopItemList", coinShopItemList);
        if (!rtn.containsKey("showPrizeList")) {
            rtn.add("showPrizeList", false);
        }
        rtn.add("showSpButton", null != roleMe.getSpDate() && roleMe.getSpDate().after(new Date()));
        return rtn;
    }

    @Override
    public GetAllStagePuzzleListResponse getAllStagePuzzleList(long userId) {
        //读取全部已玩过的关卡的单词拼图
        ParkourRole roleMe = roleDao.load(userId);
        List<Integer> playedStageList = new ArrayList<>();
        if (roleMe.getPassedStage() > 0) {
            for (int i = 1; i <= roleMe.getPassedStage(); i++) {
                playedStageList.add(i);
            }
        }
        long stageCount = stageDao.count();
        List<Integer> stageIdLst = new ArrayList<>();
        for (int i = 1; i <= stageCount; i++) {
            stageIdLst.add(i);
        }
        Map<Integer, PersonalStageRecord> myAllRecord = personalStageRecordDao.getsUserMultiStageRecord(userId, playedStageList);
        List<ParkourStage> allMyStage = stageIdLst.isEmpty() ?
                Collections.<ParkourStage>emptyList() :
                stageDao.loads(stageIdLst).values().stream()
                        .sorted((o1, o2) -> Integer.compare(o1.getStageId(), o2.getStageId()))
                        .collect(Collectors.toList());
        List<StagePuzzleList> allStagePuzzle = new ArrayList<>();
        for (ParkourStage stage : allMyStage) {
            StagePuzzleList plist = new StagePuzzleList();
            plist.topic = stage.getTopic();
            plist.fillFieldValue("wordList", stage.getWordList());
            PersonalStageRecord record = myAllRecord.get(stage.getStageId());
            if (null != record) {
                plist.fillFieldValue("achievedPuzzle", record.getAchievedPuzzle());
            }
            allStagePuzzle.add(plist);
        }
        AllStagePuzzleList pstruct = new AllStagePuzzleList();
        pstruct.stagePuzzleList = allStagePuzzle;
        GetAllStagePuzzleListResponse resp = new GetAllStagePuzzleListResponse();
        resp.success = true;
        resp.allStagePuzzle = pstruct;
        return resp;
    }

    public ParkourRole loadRole(long userId) {
        return roleDao.load(userId);
    }

    private ParkourRole exchangeIntegralAndCoinForNewlyPaidUser(ParkourRole role, StudentDetail studentDetail) {
        Map<String, KeyValuePair<String, ? extends Serializable>> exchangeMap = new HashMap<>();
        int coinExchanged = 0;
        if (role.getCoinToExchange() > 0) {
            coinExchanged = role.getCoinToExchange();
            exchangeMap.put("coinCount", new KeyValuePair<String, Serializable>("inc", role.getCoinToExchange()));
            exchangeMap.put("coinToExchange", new KeyValuePair<String, Serializable>("set", 0));
        }
        int integralInc = 0;
        if (CollectionUtils.isNotEmpty(role.getWordToExchange())) {
            for (ParkourWord wd : role.getWordToExchange()) {
                integralInc += wd.getCollectIntegeral();
            }
            exchangeMap.put("wordToExchange", new KeyValuePair<String, Serializable>("set", new ArrayList<>()));
        }
        if (exchangeMap.size() > 0) {
            if (integralInc > 0) {
                IntegralHistory integralHistory = new IntegralHistory();
                integralHistory.setComment(IntegralType.学生热血跑酷集齐拼图得学豆.getDescription());
                integralHistory.setIntegralType(IntegralType.学生热血跑酷集齐拼图得学豆.getType());
                integralHistory.setUserId(studentDetail.getId());
                integralHistory.setIntegral(integralInc);
                MapMessage integralChangeRs = userIntegralService.changeIntegral(studentDetail, integralHistory);
                if (integralChangeRs.isSuccess()) {
                    role = roleDao.updateFields(role.getRoleId(), exchangeMap);
                } else {
                    logger.error("USER {} failed Adding integral from Nekketsu Parkour to DB!!", role.getRoleId());
                }
            } else {
                role = roleDao.updateFields(role.getRoleId(), exchangeMap);
            }
        }
        if (null != role && coinExchanged > 0) {
            Map<String, String> coinAddMap = new HashMap<>();
            coinAddMap.put("source", "userPaidExchange");

            ParkourCoinHistory history = new ParkourCoinHistory();
            history.setUserId(role.getRoleId());
            history.setIncrement(coinExchanged);
            history.setCreateTime(new Date());
            history.setPaid(true);
            history.setAdditionalInfo(coinAddMap);

            parkourQueueSender.saveParkourCoinHistory(history);
        }
        return role;
    }

    @Override
    public List<LevelSpeedInfo> saveLevelSpeed(List<LevelSpeedInfo> toSave) {
        return levelSpeedInfoDao.replaceAll(toSave);
    }

    @Override
    public LoginPrizeMonthConf saveThisMonth(LoginPrizeMonthConf conf) {
        return loginPrizeMonthConfDao.saveThisMonth(conf);
    }

    @Override
    public LoginPrizeMonthConf saveNextMonth(LoginPrizeMonthConf conf) {
        return loginPrizeMonthConfDao.saveNextMonth(conf);
    }

    @Override
    public ParkourStage saveStage(ParkourStage stage) {
        return null;
    }

    @Override
    public GetStageInfoResponse getStageInfo(StudentDetail studentDetail, int stageId) {
        ParkourStage stage = stageDao.load(stageId);
        GetStageInfoResponse resp = new GetStageInfoResponse();
        if (null == stage) {
            logger.warn("Student {} requested for a Non-existing stage {}.", studentDetail.getId(), stageId);
            return resp;
        }
        resp.fillFrom(stage);
        LinkedHashMap<Integer, KeyValuePair<String, String>> rankRegionMap = ParkouMiscUtil.getStudentRankRegion(studentDetail);
        List<Integer> regionCodeLst = new ArrayList<>(rankRegionMap.keySet());
        //取全市 全省 全国排行
        Map<Integer, ParkourRegionRank> rankGet = rankDao.getsRegionRankBatch(regionCodeLst, stageId);
        Set<Long> studentDetailToLoad = new HashSet<>();
        for (ParkourRegionRank rg : rankGet.values()) {
            for (ParkourRankDetail rd : rg.getRankList()) {
                studentDetailToLoad.add(rd.getRoleId());
            }
        }

        Set<Long> clazzStudents = new HashSet<>(asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(studentDetail.getClazzId()));

        Set<Long> recentPlayedUserId = personalStageRecordDao.getRecentPlayedStageRoleId(stageId);
        if (recentPlayedUserId.size() > PersonalStageRecordDao.randomPickCount) {
            Long[] randomP = new Long[PersonalStageRecordDao.randomPickCount];
            RandomUtils.randomPickFew(new ArrayList<>(recentPlayedUserId), PersonalStageRecordDao.randomPickCount, randomP);
            recentPlayedUserId.retainAll(Arrays.asList(randomP));
        }

        studentDetailToLoad.addAll(clazzStudents);
        studentDetailToLoad.addAll(recentPlayedUserId);
        //取全班（包括我的），随机用户的本关最佳成绩
        Map<Long, PersonalStageRecord> allRecord = personalStageRecordDao.getMultiUserStageRecord(studentDetailToLoad, stageId);
        Set<Long> wrapSet = new HashSet<>(allRecord.keySet());
        Map<Long, ParkourRole> allRoleToUse = roleDao.loads(new HashSet<>(wrapSet));
        //取对应的studentDetail
        Map<Long, User> allUser = userLoaderClient.loadUsers(wrapSet);
        Map<Long, Clazz> userClazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazzs(new HashSet<>(allUser.keySet()));
        Map<Clazz, School> fetchedClazz = new HashMap<>();
        Map<Long, School> userSchoolNameMap = new HashMap<>();
        for (Map.Entry<Long, Clazz> entry : userClazzMap.entrySet()) {
            if (!fetchedClazz.containsKey(entry.getValue())) {
                School school = null;
                if (entry.getValue() != null) {
                    school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(entry.getValue().getSchoolId())
                            .getUninterruptibly();
                }
                fetchedClazz.put(entry.getValue(), school);
                if (null != school) {
                    userSchoolNameMap.put(entry.getKey(), school);
                }
            } else {
                School school = fetchedClazz.get(entry.getValue());
                userSchoolNameMap.put(entry.getKey(), school);
            }
        }
        List<PersonalStageRecord> allRecordList = new ArrayList<>(allRecord.values());
        Collections.sort(allRecordList);
        //同班挑战列表
        List<ParkourAi> classmateAi = new ArrayList<>();
        //随机挑战列表
        List<ParkourAi> randomAi = new ArrayList<>();
        for (PersonalStageRecord rcd : allRecordList) {
            User recordStudent = allUser.get(rcd.getRoleId());
            if (null == recordStudent) {
                continue;
            }
            ParkourAi flashAi = new ParkourAi();
            flashAi.fillFrom(rcd);
            ParkourRole role = allRoleToUse.get(rcd.getRoleId());
            flashAi.fillFrom(role);
            flashAi.roleName = recordStudent.fetchRealname();
            flashAi.img = getUserImageUrl(recordStudent.fetchImageUrl());
            flashAi.schoolName = userSchoolNameMap.get(rcd.getRoleId()) != null ? userSchoolNameMap.get(rcd.getRoleId()).getShortName() : "";
            flashAi.schoolFullName = userSchoolNameMap.get(rcd.getRoleId()) != null ? userSchoolNameMap.get(rcd.getRoleId()).getCname() : "";
            if (rcd.getRoleId().equals(studentDetail.getId())) {//是自己
                resp.fillFrom(rcd);//personalBest,starBest,achievedPuzzle赋值
            }
            if (clazzStudents.contains(rcd.getRoleId()) && !(recordStudent.getId().equals(studentDetail.getId()))) {//是同班,并且不是自己
                classmateAi.add(flashAi);
            }
            if (recentPlayedUserId.contains(rcd.getRoleId()) && !(recordStudent.getId().equals(studentDetail.getId()))) {//是同班,并且不是自己
                randomAi.add(flashAi);
            }
        }
        resp.classmateCandidate = classmateAi;
        if (randomAi.isEmpty()) {
            if (!classmateAi.isEmpty()) {
                randomAi.add(classmateAi.get(0));
            } else {
                ParkourAi fakeAi = new ParkourAi();
                fakeAi.fillFrom(resp.stageAi);
                fakeAi.roleName = "无名高手";
                randomAi.add(fakeAi);
            }
        }
        resp.randomCandidate = randomAi;

        //装配排行榜
        for (Map.Entry<Integer, KeyValuePair<String, String>> entry : rankRegionMap.entrySet()) {
            StageRankStruct rankStruct = new StageRankStruct();
            rankStruct.rankName = entry.getValue().getValue();
            ParkourRegionRank regionRank = rankGet.get(entry.getKey());
            if (null != regionRank) {
                rankStruct.fillFrom(regionRank);
                for (StageRankInfo sri : rankStruct.rankList) {
                    Long roleId = Long.valueOf(sri.roleId);
                    User sd = allUser.get(roleId);
                    ParkourRole role = allRoleToUse.get(roleId);
                    if (null != sd) {
                        sri.img = getUserImageUrl(sd.fetchImageUrl());
                        sri.roleName = sd.fetchRealname();
                        sri.level = role.getLevel();
                        sri.schoolName = userSchoolNameMap.get(sd.getId()) != null ? userSchoolNameMap.get(sd.getId()).getShortName() : "";
                        sri.schoolFullName = userSchoolNameMap.get(sd.getId()) != null ? userSchoolNameMap.get(sd.getId()).getCname() : "";
                    }
                }
            }
            resp.fillFieldValue(entry.getValue().getKey(), rankStruct);
        }

        //本关尚未兑换的单词
        ParkourRole me = roleDao.load(studentDetail.getId());
        if (CollectionUtils.isNotEmpty(me.getWordToExchange())) {
            for (ParkourWord wd : me.getWordToExchange()) {
                if (wd.getStageId() == stageId) {
                    WordToExchange sw = new WordToExchange();
                    sw.stageId = wd.getStageId();
                    sw.wordId = wd.getWordId();
                    resp.wordToExchange.add(sw);
                }
            }
        }
        resp.success = true;
        return resp;
    }

    private void sendToQueue(ParkourPlayLog saveLog, StudentDetail studentDetail) {
        parkourPlayLogHandler.handle(saveLog, studentDetail);
    }

    @Override
    public SaveGameResultResponse saveNpcGameResult(StudentDetail studentDetail, SaveGameResultRequest req, boolean isPaidUser) {
        ParkourStage stage = stageDao.load(req.stageId);
        SaveGameResultResponse resp = SaveGameResultRequest.newResponse();
        if (null == stage) {
            logger.warn("Student {} requested for a Non-existing stage {}.request is: {} ", studentDetail.getId(), req.stageId, JsonUtils.toJson(req));
            return resp;
        }
        if (!req.win) {
            return saveLostOrNonNpcGameResult(studentDetail, req, isPaidUser);
        }
        Date nowDate = Calendar.getInstance().getTime();
        PersonalStageRecord myRec = personalStageRecordDao.findOne(studentDetail.getId(), req.stageId);
        PersonalStageRecord newRec;

        int starCount = 0;
        for (Integer starTime : stage.getTimeForStar()) {
            if (req.timeCost <= starTime) {
                starCount++;
            }
        }
        KeyValuePair<ParkourWord, WordPuzzle> pickPrizeWord = pickPrizePuzzle(stage, myRec);
        ParkourWord prizeWord = pickPrizeWord.getKey();
        WordPuzzle prizePuzzle = pickPrizeWord.getValue();
        Map<String, String> addition = new HashMap<>();
        addition.put("isPaidUser", String.valueOf(isPaidUser));

        if (myRec == null) {//以前没赢过这关
            String id = studentDetail.getId() + "_" + req.stageId;
            newRec = new PersonalStageRecord(id, studentDetail.getId(), req.stageId, req.timeCost, nowDate, starCount, nowDate, req.correctRate, nowDate, req.timePerQuestion, nowDate, new HashSet<>(Arrays.asList(prizePuzzle)));
            newRec.setStarBest(starCount);
            personalStageRecordDao.insert(newRec);
        } else {//更新最好成绩
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("latestPlayTime", nowDate);
            if (req.timeCost < SafeConverter.toInt(myRec.getPersonalBest())) {
                updateMap.put("personalBest", req.timeCost);
                updateMap.put("personalBestAchieveTime", nowDate);
                addition.put("newPersonalBest", "true");
            }
            if (req.correctRate > SafeConverter.toDouble(myRec.getCorrectRate())) {
                updateMap.put("correctRate", req.correctRate);
                updateMap.put("bestCorrectRateAchieveTime", nowDate);
                updateMap.put("timePerQuestion", req.timePerQuestion);
                addition.put("newCorrectRate", "true");
            }
            if (starCount > SafeConverter.toInt(myRec.getStarBest())) {
                updateMap.put("starBest", starCount);
                updateMap.put("startBestAchieveTime", nowDate);
                addition.put("newStarBest", "true");
            }
            newRec = personalStageRecordDao.updateFields(myRec.getId(), updateMap, prizePuzzle);
            if (null == newRec) {
                return resp;
            }

        }
        resp.fillFieldValue("achievedPuzzle", prizePuzzle);
        resp.newStarCount = SafeConverter.toInt(newRec.getStarBest());
        int oldStarCount = null == myRec ? 0 : SafeConverter.toInt(myRec.getStarBest());
        int incCoin = req.coinPickCount;
        addition.put("coinPick", String.valueOf(req.coinPickCount));
        if (SafeConverter.toInt(newRec.getStarBest()) == 3 && SafeConverter.toInt(newRec.getStarBest()) > oldStarCount) {//第一次获得3星。发钱
            incCoin += SafeConverter.toInt(stage.getStageCoinBonus());
            addition.put("coinBonus", String.valueOf(SafeConverter.toInt(stage.getStageCoinBonus())));
        }

        ParkourRole meRole = roleDao.load(studentDetail.getId());
        int expOld = meRole.getExp();
        Map<String, KeyValuePair<String, ? extends Serializable>> roleUpdateMap = new HashMap<>();
        if (req.stageId == meRole.getOpenStage()) {
            if (req.stageId - meRole.getPassedStage() == 1) {//过关
                roleUpdateMap.put("passedStage", new KeyValuePair<String, Serializable>("set", req.stageId));
                addition.put("firstTimePassStage", "true");
            }
            int totalCount = (int) stageDao.count();
            if (totalCount > meRole.getOpenStage()) {//开启下一关
                roleUpdateMap.put("openStage", new KeyValuePair<String, Serializable>("set", meRole.getOpenStage() + 1));
                addition.put("openNextStage", "true");
            }

        }
        int exp = starCount < 1 ? 0 : stage.getExp().get(starCount - 1);
        int roleExp = meRole.getExp() + exp;
        addition.put("expAdd", String.valueOf(exp));
        addition.put("expAfterAdd", String.valueOf(roleExp));
        //加经验
        roleUpdateMap.put("exp", new KeyValuePair<String, Serializable>("inc", exp));
        List<LevelSpeedInfo> lfConf = levelSpeedInfoDao.findAll();
        int levelNew = 0;
        for (LevelSpeedInfo lsi : lfConf) {
            if (roleExp >= lsi.getExp()) {
                levelNew++;
            }
        }
        if (levelNew > meRole.getLevel()) {//升级了
            roleUpdateMap.put("level", new KeyValuePair<String, Serializable>("set", levelNew));
            addition.put("levelUp", "true");
            addition.put("levelAfter", String.valueOf(levelNew));
        }
        addition.put("levelAfter", String.valueOf(levelNew));
        addition.put("levelBefore", String.valueOf(meRole.getLevel()));
        if (isPaidUser) {//加金币
            roleUpdateMap.put("coinCount", new KeyValuePair<String, Serializable>("inc", incCoin));//付费用户直接加
        } else {
            roleUpdateMap.put("coinToExchange", new KeyValuePair<String, Serializable>("inc", incCoin));//非付费用户加到未兑换字段
        }
        roleUpdateMap.put("updateTime", new KeyValuePair<String, Serializable>("set", nowDate));
        resp.coinBonus = incCoin;
        addition.put("getPuzzle", JsonUtils.toJson(prizePuzzle));
        if (null == myRec || !myRec.getAchievedPuzzle().contains(prizePuzzle)) {//获得新拼图了
            addition.put("newPuzzle", "true");
            int puzzleCount = 0;
            for (WordPuzzle wp : newRec.getAchievedPuzzle()) {
                if (wp.getWordId().equals(prizePuzzle.getWordId())) {
                    puzzleCount++;
                }
            }
            Map<String, ParkourWord> wordMap = stage.getWordList().stream()
                    .filter(e -> e != null && e.getWordId() != null)
                    .collect(Collectors.groupingBy(ParkourWord::getWordId))
                    .values().stream()
                    .map(e -> e.iterator().next())
                    .collect(Collectors.toMap(ParkourWord::getWordId, Function.identity()));

            ParkourWord collectWord = wordMap.get(prizePuzzle.getWordId());
            if (puzzleCount == 4) {//集齐了一块拼图
                addition.put("collect4Puzzle", "true");
                resp.integralBonus = collectWord.getCollectIntegeral();
                addition.put("integralBonus", String.valueOf(collectWord.getCollectIntegeral()));
                if (isPaidUser) {//付费用户直接奖学豆
                    IntegralHistory integralHistory = new IntegralHistory();
                    integralHistory.setComment(IntegralType.学生热血跑酷集齐拼图得学豆.getDescription());
                    integralHistory.setIntegralType(IntegralType.学生热血跑酷集齐拼图得学豆.getType());
                    integralHistory.setUserId(studentDetail.getId());
                    integralHistory.setIntegral(collectWord.getCollectIntegeral());
                    MapMessage integralChangeRs = userIntegralService.changeIntegral(studentDetail, integralHistory);
                    if (!integralChangeRs.isSuccess()) {
                        logger.error("USER {} FAILED TO ADD Integray {} TYPE {}", studentDetail.getId(), collectWord.getCollectIntegeral(), IntegralType.学生热血跑酷集齐拼图得学豆);
                    }
                } else {//非付费用户将单词存起来
                    roleUpdateMap.put("wordToExchange", new KeyValuePair<String, Serializable>("addToSet", prizeWord));
                }
            }
        }
        meRole = roleDao.updateFields(studentDetail.getId(), roleUpdateMap);
        resp.newLevel = meRole.getLevel();
        resp.newExp = meRole.getExp();
        resp.expBonus = meRole.getExp() - expOld;
        resp.newOpenStage = meRole.getOpenStage();
        resp.coinTotal = meRole.getCoinCount();
        if (null != meRole.getWordToExchange()) {
            resp.fillFieldValue("wordToExchange", meRole.getWordToExchange());
        }
        resp.success = true;

        ParkourPlayLog playLog = new ParkourPlayLog();
        playLog.setPlayerId(studentDetail.getId());
        playLog.setStageId(req.stageId);
        playLog.setPlayDateTime(nowDate);
        playLog.setCorrectRate(req.correctRate);
        playLog.setPlayType(req.fightType);
        playLog.setTimeCost(req.timeCost);
        playLog.setTimePerQuestion(req.timePerQuestion);
        playLog.setWin(req.win);
        playLog.setStar(starCount);
        playLog.setAdditionalInfo(addition);
        playLog.setAnswerCorrectList(buildAnswerMap(req.answerList));
        sendToQueue(playLog, studentDetail);

        Map<String, String> coinAddMap = new HashMap<>();
        coinAddMap.put("source", req.fightType);

        ParkourCoinHistory history = new ParkourCoinHistory();
        history.setUserId(studentDetail.getId());
        history.setIncrement(resp.coinBonus);
        history.setCreateTime(new Date());
        history.setPaid(isPaidUser);
        history.setAdditionalInfo(coinAddMap);

        parkourQueueSender.saveParkourCoinHistory(history);
        return resp;
    }

    private KeyValuePair<ParkourWord, WordPuzzle> pickPrizePuzzle(ParkourStage stage, PersonalStageRecord myRec) {
        ParkourWord prizeWord = RandomUtils.pickRandomElementFromList(stage.getWordList());
        int prizePlace = RandomUtils.nextInt(1, 4);
        WordPuzzle prizePuzzle = new WordPuzzle(prizeWord.getWordId(), prizePlace);
        if (RuntimeMode.gt(Mode.DEVELOPMENT)) {
            return new KeyValuePair<>(prizeWord, prizePuzzle);
        } else {//研发环境下，让玩家按顺序获得拼图
            if (null == myRec) {//还没玩过本关，获得第一块拼图
                prizeWord = stage.getWordList().get(0);
                return new KeyValuePair<>(prizeWord, new WordPuzzle(prizeWord.getWordId(), 1));
            } else if (myRec.getAchievedPuzzle().size() == stage.getWordList().size() * 4) {//本关都获得完毕了，随机获得一块
                return new KeyValuePair<>(prizeWord, prizePuzzle);
            } else {//哪块还没获得就给哪块
                List<WordPuzzle> allPuzzleGot = new ArrayList<>(stage.getWordList().size() * 4);
                for (int i = 0; i < stage.getWordList().size() * 4; i++) {
                    allPuzzleGot.add(null);
                }
                Map<String, List<WordPuzzle>> wpm = new HashMap<>();
                for (WordPuzzle pz : myRec.getAchievedPuzzle()) {
                    List<WordPuzzle> wpl = wpm.get(pz.getWordId());
                    if (null == wpl) {
                        wpl = new ArrayList<>(4);
                        wpm.put(pz.getWordId(), wpl);
                    }
                    wpl.add(pz);
                }
                int wordCounter = 0;
                for (ParkourWord wd : stage.getWordList()) {
                    List<WordPuzzle> wpl = wpm.get(wd.getWordId());
                    if (CollectionUtils.isNotEmpty(wpl)) {
                        for (WordPuzzle wpz : wpl) {
                            int index = (wpz.getPuzzlePlace() + wordCounter * 4) - 1;
                            allPuzzleGot.set(index, wpz);
                        }
                    }
                    wordCounter++;
                }
                for (int i = 0; i < allPuzzleGot.size(); i++) {
                    WordPuzzle wp = allPuzzleGot.get(i);
                    if (null == wp) {
                        return new KeyValuePair<>(stage.getWordList().get(i / 4), new WordPuzzle(stage.getWordList().get(i / 4).getWordId(), i % 4 + 1));
                    }
                }
            }
        }
        return new KeyValuePair<>(prizeWord, new WordPuzzle(prizeWord.getWordId(), 1));
    }

    @Override
    public SaveGameResultResponse saveLostOrNonNpcGameResult(StudentDetail studentDetail, SaveGameResultRequest req, boolean isPaidUser) {
        ParkourStage stage = stageDao.load(req.stageId);
        SaveGameResultResponse resp = SaveGameResultRequest.newResponse();
        if (null == stage) {
            logger.warn("Student {} requested for a Non-existing stage {}.request is: {} ", studentDetail.getId(), req.stageId, JsonUtils.toJson(req));
            return resp;
        }
        Map<String, KeyValuePair<String, ? extends Serializable>> roleUpdateMap = new HashMap<>();
        if (isPaidUser) {//加金币
            roleUpdateMap.put("coinCount", new KeyValuePair<>("inc", req.coinPickCount));//付费用户直接加
        } else {
            roleUpdateMap.put("coinToExchange", new KeyValuePair<>("inc", req.coinPickCount));//非付费用户加到未兑换字段
        }
        ParkourRole updatedRole = roleDao.updateFields(studentDetail.getId(), roleUpdateMap);
        if (null != updatedRole) {
            resp.success = true;
            resp.coinTotal = updatedRole.getCoinCount();
            Map<String, String> add = new HashMap<>();
            resp.coinBonus = req.coinPickCount;
            resp.newExp = updatedRole.getExp();
            resp.newLevel = updatedRole.getLevel();
            resp.newOpenStage = updatedRole.getOpenStage();
            if (null != updatedRole.getWordToExchange()) {
                resp.fillFieldValue("wordToExchange", updatedRole.getWordToExchange());
            }
            add.put("coinPick", String.valueOf(req.coinPickCount));
            add.put("isPaidUser", String.valueOf(isPaidUser));
            ParkourPlayLog playLog = new ParkourPlayLog(
                    null,
                    studentDetail.getId(),
                    NumberUtils.toLong(req.opponentId, 0l),
                    req.stageId,
                    req.win,
                    req.timeCost,
                    req.correctRate,
                    buildAnswerMap(req.answerList),
                    req.fightType,
                    req.timePerQuestion,
                    new Date(),
                    0,
                    add);
            sendToQueue(playLog, studentDetail);

            Map<String, String> coinAddMap = new HashMap<>();
            coinAddMap.put("source", req.fightType);

            ParkourCoinHistory history = new ParkourCoinHistory();
            history.setUserId(studentDetail.getId());
            history.setIncrement(resp.coinBonus);
            history.setCreateTime(new Date());
            history.setPaid(isPaidUser);
            history.setAdditionalInfo(coinAddMap);

            parkourQueueSender.saveParkourCoinHistory(history);
        }
        return resp;
    }

    private List<KeyValuePair<String, Boolean>> buildAnswerMap(Collection<AnswerResult> answerList) {
        if (CollectionUtils.isEmpty(answerList)) {
            return Collections.emptyList();
        }
        List<KeyValuePair<String, Boolean>> rtn = new ArrayList<>();
        for (AnswerResult ar : answerList) {
            rtn.add(new KeyValuePair<>(ar.wordId, ar.correct));
        }
        return rtn;
    }

    @Override
    public GetLoginRewardListResponse getLoginRewardList(long userId, Gender gender) {
        if (gender == Gender.NOT_SURE) {
            gender = Gender.MALE;
        }
        GetLoginRewardListResponse resp = GetLoginRewardListRequest.newResponse();
        ParkourRole userRole = roleDao.load(userId);
        if (null == userRole) {
            return resp;
        }
        Calendar createCalendar = Calendar.getInstance();
        createCalendar.setTime(userRole.getCreateTime());
        Calendar now = Calendar.getInstance();
        int monthDiff = 12 * (now.get(Calendar.YEAR) - createCalendar.get(Calendar.YEAR)) + (now.get(Calendar.MONTH) - createCalendar.get(Calendar.MONTH));
        int fetchSize = Math.min(monthDiff + 1, 6);
        List<ParkourLoginPrize> lastMonthsPrize = parkourLoginPrizeDao.findLastMonths(userId, fetchSize);
        List<LoginPrizeMonthConf> lastMonthsPrizeConf = loginPrizeMonthConfDao.findLatestMulti(fetchSize);
        int confIndex = 0;
        if (CollectionUtils.isNotEmpty(lastMonthsPrize)) {
            for (ParkourLoginPrize plp : lastMonthsPrize) {
                LoginPrizeMonthConf conf = lastMonthsPrizeConf.get(confIndex++);
                List<String> itemIdList = conf.getPrizeItemId().get(gender);
                LoginRewardMonth lrm = new LoginRewardMonth();
                Calendar cal = Calendar.getInstance();
                cal.setTime(plp.getCreateTime());
                cal.add(Calendar.MONTH, 6);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long countDownMilli = cal.getTimeInMillis() - System.currentTimeMillis();
                lrm.expireCountdown = (int) (countDownMilli / 1000);
                lrm.id = plp.getId();
                for (int ct = 0; ct < itemIdList.size(); ct++) {
                    PrizeDetailLogin pdl = new PrizeDetailLogin();
                    pdl.itemId = itemIdList.get(ct);
                    pdl.obtainDay = ct + 1;
                    if (ct < plp.getPrizeDetailList().size()) {
                        LoginPrizeDetail lpd = plp.getPrizeDetailList().get(ct);
                        pdl.obtainDate = lpd.getObtainDate();
                        pdl.exchanged = lpd.isExchanged();
                        pdl.achieved = true;
                    }
                    lrm.prizeDetailList.add(pdl);
                }
                resp.loginRewardList.add(lrm);
            }
        }
        resp.success = true;
        return resp;
    }

    @Override
    public ExchangeLoginRewardResponse exchangeLoginReward(long userId, String id, PrizeDetailLogin detail, Gender gender) {
        if (gender == Gender.NOT_SURE) {
            gender = Gender.MALE;
        }
        ExchangeLoginRewardResponse resp = ExchangeLoginRewardRequest.newResponse();
        if (null == detail) {
            resp.failReason = "ILLEGAL_PRIZE";
            return resp;
        }
        if (detail.exchanged) {
            resp.failReason = "ALREADY_EXCHANGED";
            return resp;
        }

        LoginPrizeDetail lpd = new LoginPrizeDetail(detail.obtainDate, false);
        ParkourLoginPrize prize = parkourLoginPrizeDao.setPrizeExchanged(id, lpd);
        if (null == prize) {
            resp.failReason = "ILLEGAL_PRIZE";
            return resp;
        }
        LoginRewardMonth lrm = new LoginRewardMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(prize.getCreateTime());
        cal.add(Calendar.MONTH, 6);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long countDownMilli = cal.getTimeInMillis() - System.currentTimeMillis();
        lrm.expireCountdown = (int) (countDownMilli / 1000);
        lrm.id = prize.getId();
        String[] spt = StringUtils.split(id, "_");
        int confId = Integer.valueOf(spt[1] + spt[2]);
        LoginPrizeMonthConf prizeConf = loginPrizeMonthConfDao.findById(confId);
        List<String> itemIdList = prizeConf.getPrizeItemId().get(gender);
        for (int ct = 0; ct < itemIdList.size(); ct++) {
            PrizeDetailLogin pdl = new PrizeDetailLogin();
            pdl.itemId = itemIdList.get(ct);
            pdl.obtainDay = ct + 1;
            if (ct < prize.getPrizeDetailList().size()) {
                LoginPrizeDetail lpdNew = prize.getPrizeDetailList().get(ct);
                pdl.obtainDate = lpdNew.getObtainDate();
                pdl.exchanged = lpdNew.isExchanged();
                pdl.achieved = true;
            }
            lrm.prizeDetailList.add(pdl);
        }
        resp.rewardMonth = lrm;
        resp.success = true;
        return resp;
    }

    @Override
    public List<ParkourShopItem> loadShopItemCrm() {
        return parkourShopItemDao.loadWithoutCache();
    }

    @Override
    public List<ParkourShopItem> loadShopItemWithCache() {
        return parkourShopItemDao.loadWithCache();
    }

    @Override
    public void saveShopItem(List<ParkourShopItem> shopItem) {
        parkourShopItemDao.replaceAll(shopItem);
    }

    @Override
    public ParkourRole modifyCoin(long userId, int add, String detail) {
        Map<String, String> coinAddMap = new HashMap<>();

        ParkourRole rtn = roleDao.modifyCoin(userId, add);
        if (null != rtn) {
            coinAddMap.put("source", "shopping");
            coinAddMap.put("detail", detail);

            ParkourCoinHistory history = new ParkourCoinHistory();
            history.setUserId(userId);
            history.setIncrement(add);
            history.setCreateTime(new Date());
            history.setPaid(true);
            history.setAdditionalInfo(coinAddMap);

            parkourQueueSender.saveParkourCoinHistory(history);
        }
        return rtn;
    }

    @Override
    public boolean setUserSpValid(long userId) {
        Date spDate = DateUtils.addDays(new Date(), 7);
        spDate = DateUtils.round(spDate, Calendar.DATE);
        ParkourRole role = roleDao.load(userId);
        if (null == role || null != role.getSpDate()) {
            logger.warn("USER{} tried to set sp_valid again.");
            return false;
        }
        return null != roleDao.setSpDate(userId, spDate);
    }

    @Override
    public Map<Integer, List<String>> getStageWordMap(List<Integer> stageIdList) {
        if (CollectionUtils.isEmpty(stageIdList)) {
            return Collections.emptyMap();
        }
        List<ParkourStage> allRequestStage = stageDao.loads(stageIdList)
                .values()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o1.getStageId(), o2.getStageId()))
                .collect(Collectors.toList());
        Map<Integer, List<String>> rtn = new HashMap<>();
        for (ParkourStage parkourStage : allRequestStage) {
            List<String> list = new ArrayList<>();
            rtn.put(parkourStage.getStageId(), list);
            for (ParkourWord parkourWord : parkourStage.getWordList()) {
                list.add(parkourWord.getWordId());
            }
        }
        return rtn;
    }

    private String getUserImageUrl(String userImageUrl) {
        if (StringUtils.isBlank(userImageUrl)) {
            return commonConfiguration.getDefaultImageUrl();
        } else {
            return CdnConfig.getAvatarDomain().getValue() + "/gridfs/" + userImageUrl;
        }
    }
}
