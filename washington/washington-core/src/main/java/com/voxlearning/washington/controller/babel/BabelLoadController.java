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

package com.voxlearning.washington.controller.babel;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Babel load controller implementation.
 * Created by Sadi.Wan on 2014/10/8.
 */
@Controller
@RequestMapping("/student/babel/api/load")
@NoArgsConstructor
public class BabelLoadController extends AbstractBabelController {

//    @Inject private AsyncBabelCacheServiceClient asyncBabelCacheServiceClient;
//    @Inject private UserPopupServiceClient userPopupServiceClient;
//    @Inject private IntegralLoaderClient integralLoaderClient;
//
//    @Inject
//    private LoadHomeworkHelper loadHomeworkHelper;
//
//    /**
//     * 初始信息。包含pk角色信息，通天塔角色信息，宠物列表，物品列表，pk物品列表
//     */
//    @RequestMapping(value = "initInfo.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String initInfo() {
//        InitInfoResponse response = InitInfoRequest.newResponse();
//        return response.toResponse();
//
////        StudentDetail student = currentStudentDetail();
////        Integer clazzLevel = currentStudentClazzLevel(student);
////        if (clazzLevel == null) {
////            return response.toResponse();
////        }
////
////        RoleInfo roleInfo = loadPkRole(student.getId());
////        if (roleInfo == null) {
////            response.failReason = "NO_PK_ROLE";
////            response.success = false;
////            return response.toResponse();
////        }
////        logger.debug("User {} PK role loaded", student.getId());
////
////        if (loadHomeworkHelper.hasUndoneHomework(student)) {
////            //作业还没做完呢，不许玩
////            response.failReason = "HOMEWORK_NOT_FINISHED";
////            return response.toResponse();
////        }
////
////        BabelRole babelRole = babelLoaderClient.loadRole(student.getId());
////        if (babelRole == null) {
////            logger.error("User {} no babel role found", student.getId());
////            return response.toResponse();
////        }
////
////        BabelBag babelBag = babelLoaderClient.loadBag(student.getId());
////        if (babelBag == null) {
////            logger.error("User {} no babel bag found", student.getId());
////            return response.toResponse();
////        }
////
////        BabelRolePet babelRolePet = babelLoaderClient.loadRolePet(student.getId());
////        if (babelRolePet == null) {
////            logger.error("User {} no babel role pet found", student.getId());
////            return response.toResponse();
////        }
////
////        BabelGiftPoolManager babelGiftPoolManager = new BabelGiftPoolManager();
////        BabelGiftPool babelGiftPool = babelGiftPoolManager.getBabelGiftPool(student.getId());
////        if (babelGiftPool == null) {
////            logger.error("Failed to load babel role {} gift pool", student.getId());
////            return response.toResponse();
////        }
////
////        GetBabelRoleInfo getBabelRoleInfo = new GetBabelRoleInfo();
////        getBabelRoleInfo.fillFrom(babelRole);
////
////        //检查我当前的书，没书的话走选书逻辑，书过时了提示需要选书
////        validateBabelBooks(response, student, babelRole, clazzLevel);
////
////        getBabelRoleInfo.avatar = getUserAvatarImgUrl(currentUser().fetchImageUrl());
////        getBabelRoleInfo.gender = roleInfo.getGender().name();
////        getBabelRoleInfo.roleCareer = roleInfo.getCareer().name();
////        getBabelRoleInfo.roleName = currentUser().fetchRealname();
////        getBabelRoleInfo.roleId = currentUserId().toString();
////        if (CollectionUtils.isNotEmpty(babelBag.getItemList())) {
////            for (RoleItem roleItem : babelBag.getItemList()) {
////                getBabelRoleInfo.itemList.add(roleItem.toBagItem());
////            }
////        }
////        getBabelRoleInfo.fillFieldValue("giftList", babelBag.getGiftList());
////        getBabelRoleInfo.fillFieldValue("bossPrizeList", babelBag.getBossPrizeList());
////        getBabelRoleInfo.fillFieldValue("petList", babelRolePet.getPetList());
////        getBabelRoleInfo.grade = student.getClazzLevel().getLevel();
////
////        getBabelRoleInfo.sendableGiftList.addAll(BabelGiftPoolUtils.toSendableGiftList(babelGiftPool));
////        getBabelRoleInfo.vitality = babelVitalityServiceClient.getCurrentBalance(babelRole.getRoleId()).getBalance();
////        getBabelRoleInfo.integral = (int) integralLoaderClient.getIntegralLoader().loadStudentIntegral(student.getId()).getUsable();
////        response.roleInfo = getBabelRoleInfo;
////        response.fillFieldValue("attackBuff", getAttackBuff());
////        response.fillFieldValue("itemList", Arrays.asList(BabelItem.values()));
////        response.fillFieldValue("petList", babelLoaderClient.loadAvailablePets().values());
////        response.fillFieldValue("dailyMission", Arrays.asList(DailyMission.values()));
////        response.babelStarExchangeRate.addAll(BabelStarExchangeRateConfig.getRates());
////
////        List<EquipmentConfig> pkItemList = pkConfigLoaderClient.equipmentConfigCache().loadAll()
////                .stream()
////                .filter(t -> t.getCategory() == SourceCategory.BABEL)
////                .collect(Collectors.toList());
////        response.fillFieldValue("pkItemList", pkItemList);
////        response.afentiOpen = true;
////        response.afentiUser = afentiServiceClient.hasValidAfentiOrder(student.getId(), Subject.ENGLISH);
////        if (response.afentiUser) {
////            getBabelRoleInfo.afentiStarCount = 0;
////        }
////
////        UserAuthentication ua = userLoaderClient.loadUserAuthentication(student.getId());
////        response.hasPassword = StringUtils.isNotBlank(ua.getPaymentPassword());
////        response.secureKey = genSecureKeyForCurrentUser(SecureKeyType.PK通天塔通用);
////
////        response.maxFloor = babelLoaderClient.loadMaxFloorId();
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 获取全部楼层信息 用于图鉴
//     */
//    @RequestMapping(value = "getAllNpcIllustrated.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getAllNpcIllustrated() {
//        GetAllNpcIllustrateResponse response = GetAllNpcIllustrateRequest.newResponse();
//        return response.toResponse();
//
////        Map<Integer, BabelNpc> availableNpcs = babelLoaderClient.loadAvailableNpcs();
////        if (MapUtils.isEmpty(availableNpcs)) {
////            logger.error("No avaliable BABEL npc loaded");
////            return response.toResponse();
////        }
////        List<BabelNpc> availableNpcList = new ArrayList<>(availableNpcs.values());
////        Map<Integer, List<NpcIllustrate>> illustrations = new LinkedHashMap<>();
////        for (int i = 0; i < availableNpcList.size(); i++) {
////            int mapNo = i / 15 + 1;
////            if (!illustrations.containsKey(mapNo)) {
////                illustrations.put(mapNo, new ArrayList<NpcIllustrate>());
////            }
////            NpcIllustrate illustration = availableNpcList.get(i).toNpcIllustrate();
////            illustration.mapNo = mapNo;
////            illustrations.get(mapNo).add(illustration);
////        }
////        for (Integer mapNo : illustrations.keySet()) {
////            MapNpcIllustrate mi = new MapNpcIllustrate();
////            mi.mapNo = mapNo;
////            mi.npcIllustrateList.addAll(illustrations.get(mapNo));
////            response.mapNpcIllustrate.add(mi);
////        }
////
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 增加已播放引导动画ID
//     */
//    @RequestMapping(value = "addIntroAnimation.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String addIntroAnimation() {
//        AddIntroAnimationResponse response = new AddIntroAnimationResponse();
//        return response.toResponse();
//
////        AddIntroAnimationRequest request = parseRequestFromParameter("data", AddIntroAnimationRequest.class);
////        if (request == null) {
////            logger.error("Illegal request parameter: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("BABEL role {} not loaded", currentUserId());
////            return response.toResponse();
////        }
////        MapMessage message = babelServiceClient.playAnimation(role, request.animationId);
////        if (!message.isSuccess()) {
////            return response.toResponse();
////        }
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 获取用户可达楼层与关卡号
//     */
//    @RequestMapping(value = "getMyFloorStage.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getMyFloorStage() {
//        GetMyFloorStageResponse response = GetMyFloorStageRequest.newResponse();
//        return response.toResponse();
//
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("Failed to load BABEL role {}", currentUserId());
////            return response.toResponse();
////        }
////        response.floor = role.getFloor();
////        response.stageIndex = role.getStageIndex();
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 刷新我的BOSS挑战奖品列表
//     */
//    @RequestMapping(value = "getBossPrizeList.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getBossPrizeList() {
//        GetBossPrizeListResponse response = GetBossPrizeListRequest.newResponse();
//        return response.toResponse();
//
////        BabelBag bag = babelLoaderClient.loadBag(currentUserId());
////        if (bag == null) {
////            logger.error("Failed to load BABEL bag {}", currentUserId());
////            return response.toResponse();
////        }
////        if (CollectionUtils.isNotEmpty(bag.getBossPrizeList())) {
////            for (BabelStoredPrize prize : bag.getBossPrizeList()) {
////                if (null != prize) {
////                    response.bossPrizeList.add(prize.toBossPrizeInfo());
////                }
////            }
////        }
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 进入挑战BOSS
//     */
//    @RequestMapping(value = "enterBossBattle.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String enterBossBattle() {
//        EnterBossBattleResponse resp = EnterBossBattleRequest.newResponse();
//        return resp.toResponse();
//
////        List<BossPrizeBrief> bossPrizeBriefList = this.buildBossPrizeBriefList();
////        if (!BabelBossBattleConf.isNowLegalToBattle()) {//BOSS战尚未开启
////            resp.nextBattleOpenTime = BabelBattleTimeCalculator.getNextBattleCountDown(BabelBossBattleConf.OPEN_TIME);
////            resp.prizeBrief = bossPrizeBriefList;
////            return resp.toResponse();
////        }
////        resp.prizeBrief = bossPrizeBriefList;
////        resp.battleEndTime = BabelBattleTimeCalculator.getNowBattleOverCountDown(BabelBossBattleConf.OPEN_TIME);
////        resp.success = true;
////        return resp.toResponse();
//    }
//
//    private List<BossPrizeBrief> buildBossPrizeBriefList() {
//        BabelBossPrizeConf prizeConf = babelLoaderClient.loadBabelBossPrizeConf();
//        if (null == prizeConf) {
//            return Collections.emptyList();
//        }
//        List<BossPrizeBrief> rtn = new ArrayList<>(15);//一般不会多余15个
//        if (CollectionUtils.isNotEmpty(prizeConf.getTopPrize())) {
//            for (BabelStoredPrize prize : prizeConf.getTopPrize()) {
//                if (null != prize) {
//                    BossPrizeBrief brief = new BossPrizeBrief();
//                    brief.order = rtn.size() + 1;
//                    brief.range = new StringBuilder("第").append(brief.order).append("名").toString();
//                    RewardInfo reward = new RewardInfo();
//                    reward.rewardType = prize.getRewardType().name();
//                    reward.count = prize.getCount();
//                    reward.itemId = prize.getItemId();
//                    brief.reward = reward;
//                    rtn.add(brief);
//                }
//            }
//        }
//
//        if (CollectionUtils.isNotEmpty(prizeConf.getAllPrize())) {
//            int rangeFrom = 0;
//            for (BossPrizeRangeConf rangeConf : prizeConf.getAllPrize()) {
//                if (null != rangeConf && null != rangeConf.getPrize()) {
//                    BossPrizeBrief brief = new BossPrizeBrief();
//                    brief.order = rtn.size() + 1;
//                    brief.range = new StringBuilder().append(rangeFrom).append("%～").append(rangeConf.getDivider()).append("%").toString();
//                    RewardInfo reward = new RewardInfo();
//                    BabelStoredPrize prize = rangeConf.getPrize();
//                    reward.rewardType = prize.getRewardType().name();
//                    reward.count = prize.getCount();
//                    reward.itemId = prize.getItemId();
//                    brief.reward = reward;
//                    rtn.add(brief);
//                    rangeFrom = rangeConf.getDivider() + 1;
//                }
//            }
//        }
//        return rtn;
//    }
//
//    /**
//     * 开始BOSS战
//     */
//    @RequestMapping(value = "startBossBattle.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String startBossBattle() {
//        StartBossBattleResponse resp = new StartBossBattleResponse();
//        if (currentStudentDetail().getClazzLevel().getLevel() < 3) {
//            return resp.toResponse();
//        }
//
//        if (!BabelBossBattleConf.isNowLegalToBattle()) {//BOSS战尚未开启
//            resp.nextBattleOpenTime = BabelBattleTimeCalculator.getNextBattleCountDown(BabelBossBattleConf.OPEN_TIME);
//            resp.vitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
//            return resp.toResponse();
//        }
//
//        resp.battleEndTime = BabelBattleTimeCalculator.getNowBattleOverCountDown(BabelBossBattleConf.OPEN_TIME);
//        resp.bossHp = 100 - BabelBattleTimeCalculator.getNowBattleOverRate(BabelBossBattleConf.OPEN_TIME);
//        if (resp.bossHp <= 0) {
//            resp.bossHp = 1;
//        }
//
//        resp.vitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
//        resp.fightStartTime = (int) (System.currentTimeMillis() / 1000);
//        resp.success = true;
//        return resp.toResponse();
//    }
//
//    /**
//     * 获取BOSS挑战排名
//     */
//    @RequestMapping(value = "getBossBattleRank.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getBossBattleRank() {
//        GetBossBattleRankResponse resp = GetBossBattleRankRequest.newResponse();
//        return resp.toResponse();
//
////        try {
////            resp.newSecureKey = getCurrentKey(currentUserId(), SecureKeyType.PK通天塔通用);
////            resp.keyCheckOk = true;
////            long userId = currentUserId();
////            if (!BabelBossBattleConf.isNowLegalToBattle()) {//战斗结束了
////                resp.success = true;
////                int battleFinishedTime = BabelBossBattleConf.getLatestFinishedTime();
////                int now = (int) (System.currentTimeMillis() / 1000);
////                int rankCoolDown = RuntimeMode.le(Mode.STAGING) ? 5 * 60 : 30 * 60;
////                if (now - battleFinishedTime < rankCoolDown) {//战斗结束半小时内，排行榜正在生成，此时排行榜不开放
////                    return resp.toResponse();
////                }
////
////                //战斗结束，排行榜已算出
////                fillLastTimeRankInfo(resp, true);
////                resp.isRankOpen = true;
////                return resp.toResponse();
////            }
////
////            //boss战中，实时排名。实际上是取出最近一次实时排名，并把自己的当前得分merge到排名中。所以每个人看到的排行榜中，自己的分数都是完全实时的，其他的人分数则不一定
////            resp.isRankOpen = true;
////            resp.isRealtimeRank = true;
////            BabelBossFightScore myScore = BabelCache.getBabelCache()
////                    .load(BabelBossFightScore.class.getName() + BabelBossBattleConf.getUsableSuffixForNow() + "_" + userId);
////
////            String playCountCacheKey = BabelCacheKey.BABEL_BOSS_FIGHT_BATTLE_PLAYER_COUNT + BabelBossBattleConf.getUsableSuffixForNow();
////            String totalPlayedCount = BabelCache.getBabelCache().load(playCountCacheKey);
////            if (null == totalPlayedCount) {
////                totalPlayedCount = String.valueOf(babelServiceClient.getNowPlayedCount());
////                BabelCache.getBabelCache().add(playCountCacheKey, BabelCacheKey.BABEL_BOSS_BATTLE_CACHE_EXPIRE, totalPlayedCount);
////            }
////
////            resp.totalPlayedCount = totalPlayedCount == null ? "0" : totalPlayedCount;
////            totalPlayedCount = totalPlayedCount == null ? "0" : totalPlayedCount;
////            List<BossBattleTopInfo> topList = babelServiceClient.getAllSortWithScoreDesc();
////            if (topList == null) {
////                topList = Collections.emptyList();
////            }
////
////            if (null == myScore) {
////                List<BabelBossFightScore> userScore = babelServiceClient.getUserBattleScoreNow(userId);
////                if (!CollectionUtils.isEmpty(userScore)) {
////                    BabelBossFightScore scoreToCache = new BabelBossFightScore();
////                    scoreToCache.setUserId(userId);
////                    scoreToCache.setFightStartTime(userScore.get(userScore.size() - 1).getFightStartTime());
////                    scoreToCache.setFightFinishTime(userScore.get(0).getFightFinishTime());
////                    for (BabelBossFightScore score : userScore) {
////                        scoreToCache.setScore(scoreToCache.getScore() + score.getScore());
////                    }
////
////                    BabelCache.getBabelCache()
////                            .add(BabelBossFightScore.class.getName() + BabelBossBattleConf.getUsableSuffixForNow() + "_" + userId, BabelCacheKey.BABEL_BOSS_BATTLE_CACHE_EXPIRE, scoreToCache);
////                    myScore = scoreToCache;
////                } else {//本次还没打过BOSS，直接返回
////                    resp.myRank = String.valueOf(BabelCountConstant.MAX_RANK_COUNT) + "+";
////                    resp.topList = topList;
////                    resp.success = true;
////                    fillLastTimeRankInfo(resp);
////                    return resp.toResponse();
////                }
////            }
////
////            resp.myScore = myScore.getScore();
////            Student me = currentStudentDetail();
////            BossBattleTopInfo meInfo = new BossBattleTopInfo();
////            meInfo.score = myScore.getScore();
////            meInfo.imgUrl = getUserAvatarImgUrl(me.getProfile().getImgUrl());
////            meInfo.userId = String.valueOf(userId);
////            meInfo.lastTimeFinishTime = myScore.getFightFinishTime();
////            meInfo.userName = me.fetchRealname();
////            final TreeSet<BabelBossFightScore> lastRank = babelServiceClient.getRealtimRankLatest();
////            if (null == lastRank || lastRank.isEmpty()) {//我来的很早，第一轮排名还没出来||还没有别人玩过，把我放在第一名直接返回
////                topList = new ArrayList<>();
////                topList.add(meInfo);
////                resp.myRank = "1";
////                resp.position = "1";
////                resp.success = true;
////                resp.topList = topList;
////                fillLastTimeRankInfo(resp);
////                return resp.toResponse();
////            }
////
////            BabelBossFightScore rankTail = lastRank.last();
////            if (rankTail.getScore() > myScore.getScore()) {//不在前10000||前10000名未满而本人尚未排进来名中
////                if (lastRank.size() >= BabelCountConstant.MAX_RANK_COUNT) {//前10000名满了，显示10000+
////                    resp.myRank = String.valueOf(BabelCountConstant.MAX_RANK_COUNT) + "+".toString();
////                    int approximatePosition = NumberUtils.toInt(calcMyPosition(NumberUtils.toInt(totalPlayedCount), lastRank.size())) + (100 - NumberUtils.toInt(calcMyPosition(lastRank.last().getScore(), meInfo.score)));
////                    approximatePosition = approximatePosition > 99 ? 99 : approximatePosition;
////                    resp.position = String.valueOf(approximatePosition);
////                } else {
////                    //排名不满10000人，正好将本人排在末尾
////                    resp.myRank = String.valueOf(lastRank.size() + 1);
////                    if (lastRank.size() == topList.size() && topList.size() < BabelConstants.MAX_BOSS_FIGHT_TOP_COUNT) {//如果top100还没满，将我放在尾部
////                        topList.add(meInfo);
////                    }
////                    resp.position = calcMyPosition(NumberUtils.toInt(totalPlayedCount), NumberUtils.toInt(resp.myRank));
////                }
////
////                resp.topList = topList;
////                resp.success = true;
////                fillLastTimeRankInfo(resp);
////                return resp.toResponse();
////            }
////
////            //用户位于前10000名，需要给出他的具体排名序号。如果进了前100，需要将其插入到top列表中
////            //如果能进前100，直接遍历前top就行了，不用遍历lastRank
////            BossBattleTopInfo topLast = topList.get(topList.size() - 1);
////            if (topLast.score < meInfo.score) {//能进top了
////                Iterator<BossBattleTopInfo> topIter = topList.iterator();
////                while (topIter.hasNext()) {//如果自己原先也在前100，先把原先的移除
////                    BossBattleTopInfo bti = topIter.next();
////                    if (bti.userId.equals(meInfo.userId)) {
////                        topIter.remove();
////                        break;
////                    }
////                }
////
////                topList.add(meInfo);
////                Collections.sort(topList, topInfoComparator);
////                resp.topList = topList;
////                resp.myRank = String.valueOf(topList.indexOf(meInfo) + 1);
////                if (topList.size() > BabelConstants.MAX_BOSS_FIGHT_TOP_COUNT) {
////                    topList.remove(topList.size() - 1);
////                }
////                resp.position = calcMyPosition(NumberUtils.toInt(totalPlayedCount), NumberUtils.toInt(resp.myRank));
////                resp.success = true;
////                fillLastTimeRankInfo(resp);
////                return resp.toResponse();
////            }
////
////
////            //没进前100，但在最大10000名的排行榜里
////            Iterator<BabelBossFightScore> iter = lastRank.iterator();
////            int myRankNo = 1;
////            while (iter.hasNext()) {
////                BabelBossFightScore bf = iter.next();
////                if (bf.getUserId() == userId) {//如果以前自己也在10000名里，删除老的
////                    iter.remove();
////                    continue;
////                }
////
////                int compare = battleScoreComparator.compare(bf, myScore);
////                if (compare < 0) {
////                    myRankNo++;
////                } else {//遍历到了我所属的排名
////                    resp.myRank = String.valueOf(myRankNo);
////                    resp.position = calcMyPosition(NumberUtils.toInt(totalPlayedCount), NumberUtils.toInt(resp.myRank));
////                    resp.topList = topList;
////                    resp.success = true;
////                    fillLastTimeRankInfo(resp);
////                    return resp.toResponse();
////                }
////
////            }
////
////
////            //不在前10000里
////            if (lastRank.size() < BabelCountConstant.MAX_RANK_COUNT) {//如果排行榜还不满，我的排名就是榜尾+1
////                resp.myRank = String.valueOf(lastRank.size() + 1);
////                resp.position = calcMyPosition(NumberUtils.toInt(totalPlayedCount), lastRank.size() + 1);//我是倒数第一名
////            } else {
////                resp.myRank = String.valueOf(BabelCountConstant.MAX_RANK_COUNT) + "+";//10000+
////                int approximatePosition = 100 - NumberUtils.toInt(calcMyPosition(lastRank.last().getScore(), meInfo.score));
////                approximatePosition += NumberUtils.toInt(calcMyPosition(NumberUtils.toInt(totalPlayedCount), lastRank.size()));
////                if (approximatePosition >= 100) {
////                    approximatePosition = 99;
////                }
////
////                resp.position = ((Integer) approximatePosition).toString();
////            }
////
////
////            resp.topList = topList;
////            resp.success = true;
////            fillLastTimeRankInfo(resp);
////        } catch (Exception e) {
////            logger.error("", e.getMessage(), e);
////        }
////        return resp.toResponse();
//    }
//
//    /**
//     * BOSS战推题
//     */
//    @RequestMapping(value = "getBossQuestion.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getBossQuestion() {
//        GetBossQuestionResponse resp = new GetBossQuestionResponse();
//
//        return resp.toResponse();
////        try {
////            BabelRole myRole = babelLoaderClient.loadRole(currentUserId());
////            if (null == myRole || currentStudentDetail().getClazzLevel().getLevel() < 3) {
////                return resp.toResponse();
////            }
////            long bookId = NumberUtils.toLong(myRole.getEnglishBook());
////            resp.questionIdList = buildExamQuestion(bookId, "BABEL", pushQuestionCount, true);
////            resp.success = true;
////        } catch (Exception e) {
////
////        }
////        return resp.toResponse();
//    }
//
//    /**
//     * 进入某个关卡 活力不够不可进入、PK背包无剩余空间会报提示
//     */
//    @RequestMapping(value = "enterStage.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String enterStage() {
//        EnterStageResponse response = new EnterStageResponse();
//        return response.toResponse();
//
////        EnterStageRequest request = parseRequestFromParameter("data", EnterStageRequest.class);
////        if (request == null) {
////            logger.error("Illegal request parameters: {}", getRequestParameter("data", ""));
////            response.errorCode = "10001";
////            return response.toResponse();
////        }
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("No BABEL role {} loaded", currentUserId());
////            response.errorCode = "10002";
////            return response.toResponse();
////        }
////        BabelFloor floor = babelLoaderClient.loadFloor(request.floor);
////        if (floor == null) {
////            logger.error("Invalid_BABEL_floor {} specified", request.floor);
////            response.errorCode = "10003";
////            return response.toResponse();
////        }
////        for (StageNpc npc : BabelFloor.getStageNpcList(floor, request.stageIndex)) {
////            response.npcList.add(npc.toStageNpcInfo());
////        }
////
////        MapMessage message = babelServiceClient.enterStage(role, floor, request.stageIndex);
////        if (!message.isSuccess()) {
////            logger.error("FAILED_ENTERING_STAGE {},{}", request.floor, request.stageIndex);
////            response.errorCode = "10003";
////            return response.toResponse();
////        }
////
////        int spaceLeft = pkLoaderClient.getBagEquipmentSpaceLeft(currentUserId());
////        if (spaceLeft < 1) {
////            logger.warn("entered stage (floor:{},stageIndex:{}) with NO PK bag space left",
////                    request.floor, request.stageIndex);
////        }
////
////        response.pkSpaceLeft = spaceLeft;
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 进入某楼层，获取该楼层关卡数量
//     */
//    @RequestMapping(value = "enterFloor.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String enterFloor() {
//        EnterFloorResponse response = new EnterFloorResponse();
//        return response.toResponse();
//
////        EnterFloorRequest request = parseRequestFromParameter("data", EnterFloorRequest.class);
////        if (request == null) {
////            logger.error("Illegal request parameters: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("BABEL role {} not loaded", currentUserId());
////            return response.toResponse();
////        }
////        BabelFloor floor = babelLoaderClient.loadFloor(request.floor);
////        if (floor == null) {
////            logger.error("BABEL floor {} unrecognized", request.floor);
////            return response.toResponse();
////        }
////        response.count = BabelFloor.getStageList(floor).size();
////
////        MapMessage message = babelServiceClient.enterFloor(role, floor);
////        if (!message.isSuccess()) {
////            return response.toResponse();
////        }
////
////        response.isNewFloor = message.containsKey("firstTimeEnterFloor");
////        if (response.isNewFloor) {
////            if (RuntimeMode.lt(Mode.PRODUCTION)) {
////                pkServiceClient.grantPkPrize(currentUserId(), "BABEL_1");
////            } else {
////                if (floor.getFloorNo() == 120) {//首次达到最大楼层
////                    pkServiceClient.grantPkPrize(currentUserId(), "BABEL_1");
////                }
////            }
////        }
////        claimExternalReward(role, (BabelReward) message.get("externalReward"));
////        response.fillFieldValue("reward", message.get("reward"));
////        response.success = true;
////        return response.toResponse();
//    }
//
//    @RequestMapping(value = "getNpcQuestionType.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getNpcQuestionType() {
//        GetNpcQuestionTypeResponse response = new GetNpcQuestionTypeResponse();
//        return response.toResponse();
//
////        GetNpcQuestionTypeRequest request = parseRequestFromParameter("data", GetNpcQuestionTypeRequest.class);
////        if (request == null) {
////            logger.error("ILLGEAL REQUEST PARAMETERS: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("BABEL ROLE {} NOT LOADED", currentUserId());
////            return response.toResponse();
////        }
////        BabelFloor floor = babelLoaderClient.loadFloor(request.floor);
////        if (floor == null) {
////            logger.error("BABEL FLOOR {} NOT LOADED", request.floor);
////            return response.toResponse();
////        }
////        StageNpc npc = BabelFloor.getStageNpc(floor, request.stageIndex, request.npcIndex);
////        if (null == npc) {
////            logger.error("NPC NOT FOUND (floor={},stageIndex={},npcIndex={})",
////                    floor.getFloorNo(), request.stageIndex, request.npcIndex);
////            return response.toResponse();
////        }
////
////        AttackType[] attackTypeThisTime = new AttackType[3];
////        RandomUtils.randomPickFew(Arrays.asList(AttackType.values()), 3, attackTypeThisTime);
////        List<QuestionTypeInfo> questionTypes = new ArrayList<>();
////        for (AttackType atp : attackTypeThisTime) {
////            QuestionTypeInfo qt = new QuestionTypeInfo();
////            qt.attackType = atp.name();
////            //推数学应用、英语应用
////            qt.questionType = RandomUtils.pickRandomElementFromList(BabelApplication.getApplications()).getQuestionType();
////            questionTypes.add(qt);
////        }
////        response.battleStartTime = (int) (System.currentTimeMillis() / 1000);
////        response.isClazzBattle = babelLoaderClient.isClazzBattle(request.floor);
////        response.questionTypeList.addAll(questionTypes);
////
////        MapMessage message = babelServiceClient.meetNpc(role, npc);
////        if (!message.isSuccess()) {
////            return response.toResponse();
////        }
////        response.success = true;
////        return response.toResponse();
//    }
//
//    @SuppressWarnings("unchecked")
//    @RequestMapping(value = "getQuestion.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getQuestion() {
//        GetQuestionResponse resp = new GetQuestionResponse();
//        return resp.toResponse();
//
//
////        GetQuestionRequest req;
////        try {
////            req = GetQuestionRequest.parseRequest(getRequestParameter("data", ""));
////            if (null == req) {
////                logger.warn("NULL_GetQuestionRequest,USER:{}", currentUserId());
////                throw new NullPointerException();
////            }
////        } catch (Exception e) {
////            return resp.toResponse();
////        }
////        try {
////            BabelRole myRole = babelLoaderClient.loadRole(currentUserId());
////            if (null == myRole) {
////                return resp.toResponse();
////            }
////            long bookId;
////            List<AppQuestion> appQuestionList = new ArrayList<>();
////            int cityCode = null == currentStudentDetail().getCityCode() ? -1 : currentStudentDetail().getCityCode();
////            switch (req.questionType) {
////                case 1://英语应试
////                    bookId = NumberUtils.toLong(myRole.getEnglishBook());
////                    if (bookId <= 0L) {
////                        break;
////                    }
////                    resp.questionList = buildExamQuestion(bookId, "BABEL", pushQuestionCount, true);
////                    if (resp.questionList.size() >= pushQuestionCount) {
////                        resp.success = true;
////                    }
////                    break;
////                case 2://英语应用
////                    bookId = NumberUtils.toLong(myRole.getEnglishBook());
////                    if (bookId <= 0L) {
////                        break;
////                    }
////                    Map<String, List<String>> wordAndMatch = new HashMap<>();//key:知识点，value:配错项
////                    Map<String, PsrPrimaryAppEnMatchItem> wordAndPsr = new HashMap<>();//key:知识点，value:psr推荐内容
////                    try {
////                        final int matchCount = 3;//配错数量
////                        PsrPrimaryAppEnMatchContent appPsrRs = utopiaPsrServiceClient.getRemoteReference().getPsrPrimaryAppEnMatch("BABEL", currentUserId(), cityCode, bookId, -1l, pushEnglishAppEkCount, "", matchCount);
////                        if (appPsrRs.getErrorContent().equals("success")) {
////                            for (PsrPrimaryAppEnMatchItem enItem : appPsrRs.getAppEnMatchList()) {
////                                List<String> match = enItem.getMatchEids();
////                                if (match.size() >= matchCount) {
////                                    List<String> matchWord = new ArrayList<>();
////                                    for (String mw : match) {
////                                        matchWord.add(StringUtils.substringAfter(mw, "#"));
////                                    }
////                                    wordAndMatch.put(enItem.getEid(), matchWord);
////                                }
////                                wordAndPsr.put(enItem.getEid(), enItem);
////                            }
////                        }
////                    } catch (Exception e) {
////                        logger.error("PSR getPsrPrimaryAppEnMatch FAILED with parameter:(cityCode:{},bookId:{},unitId:-1l).trace:{}", currentStudentDetail().getCityCode(), bookId, e.getMessage(), e);
////                        break;
////                    }
////                    if (wordAndMatch.isEmpty()) {
////                        logger.warn("PSR pushed NO POINT for BABEL english app with parameter:(userId:{},cityCode:{},bookId:{},unitId:-1l).HAVE TO user backup point from book.", currentUserId(), currentStudentDetail().getCityCode(), bookId);
////                    }
////                    //组织好推题知识点对应的句子
////                    Map<Sentence, Unit> psrSentence = this.loadSentenceFromWordListAndBook(new ArrayList(wordAndMatch.keySet()), bookId, pushEnglishAppEkCount);
////                    String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
////
////                    //随机3种题型
////                    int randomTypeLength = 3;
////                    BabelEnglishFlashGameConfig[] babelEnglishFlashGameConfigs = new BabelEnglishFlashGameConfig[randomTypeLength];
////                    RandomUtils.randomPickFew(
////                            new ArrayList<>(BabelEnglishFlashGameConfig.getConfigs()),
////                            randomTypeLength,
////                            babelEnglishFlashGameConfigs
////                    );
////
////                    for (BabelEnglishFlashGameConfig babelEnglishFlashGameConfig : babelEnglishFlashGameConfigs) {
////                        PracticeType englishPractice = practiceLoaderClient.loadNamedPractice(babelEnglishFlashGameConfig.getName());
////                        MapMessage mapMessage = flashGameServiceClient.loadDataFromSentenceList(currentUserId(), cdnUrl, new ArrayList<>(psrSentence.keySet()), englishPractice, Ktwelve.PRIMARY_SCHOOL, null, false);
////                        AppQuestion appQ = new AppQuestion();
////                        for (Map.Entry<Sentence, Unit> stcEntry : psrSentence.entrySet()) {
////                            UnitLessonId ul = new UnitLessonId();
////                            ul.unitId = String.valueOf(stcEntry.getValue().getId());
////                            ul.lessonId = String.valueOf(stcEntry.getKey().getLessonId());
////                            appQ.unitLessonIdList.add(ul);
////                        }
////                        Map<String, Object> gameData = (Map<String, Object>) mapMessage.get("gameData");
////                        List<Map<String, Object>> sentences = (List<Map<String, Object>>) gameData.get("sentence");
////                        for (Map<String, Object> sentence : sentences) {
////                            Map<String, Object> stcData = (Map<String, Object>) sentence.get("data");
////                            List<String> match = wordAndMatch.get("word#" + stcData.get("foreignText"));
////                            if (CollectionUtils.isNotEmpty(match)) {
////                                stcData.put("match", match);
////                            }
////                            PsrPrimaryAppEnMatchItem enItem = wordAndPsr.get("word#" + stcData.get("foreignText"));
////                            if (enItem != null) {
////                                stcData.put("weight", enItem.getWeight());
////                                stcData.put("algov", enItem.getAlgov());
////                            }
////                        }
////                        appQ.content = JsonUtils.toJson(mapMessage);
////                        appQ.appType = babelEnglishFlashGameConfig.getCategoryName();
////                        appQ.practiceId = babelEnglishFlashGameConfig.getId();
////                        appQuestionList.add(appQ);
////                    }
////                    resp.success = true;
////                    resp.appQuestionList = appQuestionList;
////                    break;
////                case 3:
////                    bookId = NumberUtils.toLong(myRole.getMathBook());
////                    if (bookId <= 0L) {
////                        break;
////                    }
////                    PsrPrimaryAppMathContent mathPsr = null;
////                    try {
////                        mathPsr = utopiaPsrServiceClient.getPsrPrimaryAppMath("BABEL", currentUserId(), cityCode, bookId, -1L, 1);//math目前每次推1个知识点
////                    } catch (Exception e) {
////                        logger.error("PSR getPsrPrimaryAppMath FAILED with parameter:(userId:{},cityCode:{},bookId:{},unitId:-1).trace:{}", currentUserId(), currentStudentDetail().getCityCode(), bookId, e.getMessage());
////                    }
////
////                    Map<Long, Map<Long, List<MathPoint>>> bulps = mathContentLoaderClient.getExtension().loadMathBookBulpsByMathBookId(bookId);
////                    Map<String, Object> targetPoint = null;
////                    String pointName = null;
////                    String baseDscp = null;
////                    int questionTime = 0;
////                    // psr有推荐知识点且该知识点能在本书中定位到才设置为true
////                    boolean hasPsrPoint = false;
////                    double weight = 0.0;
////                    String algov = "";
////                    if (null != mathPsr && CollectionUtils.isNotEmpty(mathPsr.getAppMathList())) {
////                        PsrPrimaryAppMathItem psrItem = mathPsr.getAppMathList().get(0);
////                        questionTime = null != psrItem.getTime() ? psrItem.getTime() : 0;
////                        pointName = psrItem.getEk().substring(psrItem.getEk().indexOf("#") + 1);
////                        baseDscp = psrItem.getEType().substring(psrItem.getEType().indexOf("#") + 1);
////                        weight = psrItem.getWeight();
////                        algov = psrItem.getAlgov();
////                        for (Map.Entry<Long, Map<Long, List<MathPoint>>> entry : bulps.entrySet()) {
////                            if (null != targetPoint)
////                                break;
////                            for (Map.Entry<Long, List<MathPoint>> entryLesson : entry.getValue().entrySet()) {
////                                if (null != targetPoint)
////                                    break;
////                                for (MathPoint mp : entryLesson.getValue()) {
////                                    if (mp.getPointName().equals(pointName)) {
////                                        //定位psr给出的知识点在本书的位置
////                                        targetPoint = new HashMap<>();
////                                        targetPoint.put("unitId", entry.getKey());
////                                        targetPoint.put("lessonId", entryLesson.getKey());
////                                        targetPoint.put("point", mp);
////                                        break;
////                                    }
////                                }
////                            }
////                        }
////                    }
////
////                    if (null != targetPoint) {
////                        hasPsrPoint = true;
////                    }
////                    if (null == targetPoint) {
////                        /*
////                        if(null != mathPsr && CollectionUtils.isNotEmpty(mathPsr.getAppMathList())){
////                            logger.warn("PSR pushed POINT_NAME {} for USERID:{},MATH_BOOK_ID:{}.BUT it's NOT FOUND IN THE BOOK.HAVE TO use backup point",mathPsr.getAppMathList().get(0),currentUserId(),bookId);
////                    } else {
////                            logger.warn("PSR pushed NO POINT for USERID:{},MATH_BOOK_ID:{}.HAVE TO use backup point",currentUserId(),bookId);
////                        }
////                        */
////
////                        for (Map.Entry<Long, Map<Long, List<MathPoint>>> entry : bulps.entrySet()) {
////                            if (null != targetPoint)
////                                break;
////                            for (Map.Entry<Long, List<MathPoint>> entryLesson : entry.getValue().entrySet()) {
////                                if (null != targetPoint)
////                                    break;
////                                for (MathPoint mp : entryLesson.getValue()) {
////                                    targetPoint = new HashMap<>();
////                                    targetPoint.put("unitId", entry.getKey());
////                                    targetPoint.put("lessonId", entryLesson.getKey());
////                                    targetPoint.put("point", mp);
////                                    break;
////                                }
////                            }
////                        }
////                    }
////
////                    int questionPerPoint = 20;
////                    final int defaultDuration = 30;
////                    // FIXME: currently, only one math flash game
////                    BabelMathFlashGameConfig babelMathFlashGameConfig = MiscUtils.firstElement(
////                            BabelMathFlashGameConfig.getConfigs()
////                    );
////
////
////                    PracticeType practiceType = practiceLoaderClient.loadNamedPractice(babelMathFlashGameConfig.getName());
////                    resp.mathPracticeType = practiceType.getId().toString();
////                    if (null != targetPoint) {
////                        MathPoint mathTargetPoint = (MathPoint) targetPoint.get("point");
////                        long pointId = mathTargetPoint.getId();
////                        pointName = mathTargetPoint.getPointName();
////                        resp.mathAppPointId = String.valueOf(pointId);
////                        resp.mathAppLessonId = targetPoint.get("lessonId").toString();
////                        resp.mathAppUnitId = targetPoint.get("unitId").toString();
////                        Map<String, Object> questionGet = flashGameServiceClient.loadMentalArithmeticDataWithDscp(pointId, questionPerPoint, practiceType, "1", baseDscp);
////                        if (questionGet.get("base") instanceof List) {
////                            Map<String, Integer> dscpTimeMap = new HashMap<>();
////                            List<Map> qContent = (List<Map>) questionGet.get("base");
////                            for (Map qMap : qContent) {
////                                // 应用增加字段存储 redmine http://192.168.100.222/redmine/issues/16508
////                                // 有psr推荐的知识点，在所有题目上增加weight和algov参数
////                                if (hasPsrPoint) {
////                                    qMap.put("weight", weight);
////                                    qMap.put("algov", algov);
////                                }
////                                if (questionTime > 0) {
////                                    qMap.put("duration", questionTime);
////                                    continue;
////                                }
////                                String contentBaseDscp = (String) qMap.get("baseDscp");
////                                if (StringUtils.isEmpty(contentBaseDscp)) {
////                                    qMap.put("duration", defaultDuration);
////                                    continue;
////                                }
////                                if (dscpTimeMap.containsKey(contentBaseDscp)) {
////                                    qMap.put("duration", dscpTimeMap.get(contentBaseDscp));
////                                    continue;
////                                }
////                                Integer durationFromPsr = utopiaPsrServiceClient.getPrimaryAppMathEtTimeByEkEt("BABEL", "point#" + pointName, "pattern#" + contentBaseDscp);
////                                if (null == durationFromPsr || durationFromPsr <= 0) {
////                                    qMap.put("duration", defaultDuration);
////                                    dscpTimeMap.put(contentBaseDscp, defaultDuration);
//////                                    logger.warn("Duration time missing form PSR.pointName:{},baseDscp:{}.set default duration {} seconds.",pointName,contentBaseDscp,defaultDuration);
////                                } else {
////                                    dscpTimeMap.put(contentBaseDscp, durationFromPsr);
////                                    qMap.put("duration", durationFromPsr);
////                                }
////                            }
////                        }
////                        LinkedHashMap<String, Serializable> map = new LinkedHashMap<>(13);
////                        map.put("bookId", myRole.getMathBook());
////                        map.put("cid", "");
////                        map.put("dataType", "1");
////                        map.put("gameType", practiceType.getId());
////                        map.put("hid", null);
////                        map.put("homeworkDetailId", null);
////                        map.put("lessonId", resp.mathAppLessonId);
////                        map.put("pointId", resp.mathAppLessonId);
////                        map.put("practiceType", practiceType.getId());
////                        map.put("questionNum", questionPerPoint);
////                        map.put("studyType", "babel");
////                        map.put("unitId", resp.mathAppUnitId);
////                        map.put("userId", myRole.getRoleId());
////                        LinkedHashMap<String, Serializable> param = map;
////                        questionGet.put("param", param);
////                        AppQuestion appQ = new AppQuestion();
////                        appQ.content = JsonUtils.toJson(questionGet);
////                        appQ.appType = babelMathFlashGameConfig.getCategoryName();
////                        appQ.practiceId = babelMathFlashGameConfig.getId();
////                        appQuestionList.add(appQ);
////                    } else {//这本书就没有知识点
////                        logger.error("There's NO POINT STORED IN DB FOR MATH_BOOK{},getQuestion FAILED.USERID:{}", bookId, currentUserId());
////                        break;
////                    }
////                    resp.success = true;
////                    resp.appQuestionList = appQuestionList;
////                    resp.mathAppBookId = String.valueOf(bookId);
////                    break;
////                default:
////                    break;
////            }
////        } catch (Exception e) {
////            logger.error("{}", e.getMessage(), e);
////        }
////
////        return resp.toResponse();
//    }
//
//    /**
//     * 刷新活力值。用于flash获取用户最新活力值
//     */
//    @RequestMapping(value = "refreshVatality.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String refreshVatality() {
//        GetVitalityResponse response = new GetVitalityResponse();
//        return response.toResponse();
//
////        response.vitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 战斗图中hp掉到0，使用复活卡
//     */
//    @RequestMapping(value = "revive.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String revive() {
//        ReviveResponse response = new ReviveResponse();
//        return response.toResponse();
//
////        User user = currentUser();
////        if (user == null) {
////            logger.error("RE-LOGIN IS REQUIRED, NO USER FOUND");
////            return response.toResponse();
////        }
////
////        BabelRole babelRole = babelLoaderClient.loadRole(user.getId());
////        if (babelRole == null) {
////            logger.error("BABEL ROLE {} NOT LOADED", user.getId());
////            return response.toResponse();
////        }
////
////        MapMessage message = babelServiceClient.useItem(babelRole, new BabelQuantitiedItem(BabelItem.REVIVE, 1));
////        if (!message.isSuccess()) {
////            BabelBag babelBag = babelLoaderClient.loadBag(babelRole.getRoleId());
////            if (babelBag != null) {
////                for (RoleItem roleItem : babelBag.getItemList()) {
////                    response.itemList.add(roleItem.toBagItem());
////                }
////            }
////            return response.toResponse();
////        }
////
////        BabelBag babelBag = (BabelBag) message.get("bag");
////        for (RoleItem roleItem : babelBag.removeZeroItem().getItemList()) {
////            response.itemList.add(roleItem.toBagItem());
////        }
////
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 消耗道具，将活力补满
//     */
//    @RequestMapping(value = "refillVitality.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String refillVitality() {
//        RefillVitalityResponse response = new RefillVitalityResponse();
//        return response.toResponse();
//
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("NO BABEL ROLE {} LOADED", currentUserId());
////            return response.toResponse();
////        }
////
////        BabelVitalityResponse bvr = babelVitalityServiceClient.getCurrentBalance(role.getRoleId());
////        if (bvr.getBalance() >= BabelVitality.MAX_VALUE) {
////            response.failReason = "VITALITY_ALREADY_FULL";
////            BabelBag bag = babelLoaderClient.loadBag(role.getRoleId());
////            if (bag != null) {
////                for (RoleItem roleItem : bag.removeZeroItem().getItemList()) {
////                    response.itemList.add(roleItem.toBagItem());
////                }
////            }
////            response.newVitality = bvr.getBalance();
////            return response.toResponse();
////        }
////
////        MapMessage message = babelServiceClient.useItem(role, new BabelQuantitiedItem(BabelItem.VITALITY_REFILL, 1));
////        if (!message.isSuccess()) {
////            response.failReason = "NO_ITEM";
////            BabelBag bag = babelLoaderClient.loadBag(role.getRoleId());
////            if (bag != null) {
////                for (RoleItem roleItem : bag.removeZeroItem().getItemList()) {
////                    response.itemList.add(roleItem.toBagItem());
////                }
////            }
////            response.newVitality = bvr.getBalance();
////            return response.toResponse();
////        }
////
////        response.success = true;
////        BabelBag babelBag = (BabelBag) message.get("bag");
////        for (RoleItem roleItem : babelBag.removeZeroItem().getItemList()) {
////            response.itemList.add(roleItem.toBagItem());
////        }
////
////        bvr = babelVitalityServiceClient.increaseVitality(role.getRoleId(), 1, "使用道具精力卡获得1点精力");
////        if (!bvr.isSuccess()) {
////            response.newVitality = babelVitalityServiceClient.getCurrentBalance(role.getRoleId()).getBalance();
////        } else {
////            response.newVitality = bvr.getBalance();
////        }
////        return response.toResponse();
//    }
//
//    /**
//     * 学豆换星星
//     */
//    @RequestMapping(value = "exchangeIntegralToStar.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String exchangeIntegralToStar() {
//        ExchangeIntegralToStarResponse response = ExchangeIntegralToStarRequest.newResponse();
//        return response.toResponse();
//
////        ExchangeIntegralToStarRequest request = parseRequestFromParameter("data", ExchangeIntegralToStarRequest.class);
////        if (request == null) {
////            logger.error("ILLEGAL REQUEST PARAMETERS: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////
////        User student = currentUser();
////        UserAuthentication ua = userLoaderClient.loadUserAuthentication(student.getId());
////        if (StringUtils.isNotEmpty(ua.getPaymentPassword())) {
////            Password password = Password.of(ua.getPaymentPassword());
////            String paymentPassword = Password.obscurePassword(request.password, password.getSalt());
////            if (!StringUtils.equals(paymentPassword, password.getPassword())) {
////                logger.error("INCORRECT PAYMENT PASSWORD OF {} WHEN EXCHANGE INTEGRAL -> STAR", student.getId());
////                return response.toResponse();
////            }
////        }
////        response.passwordOk = true;
////
////        BabelRole role = babelLoaderClient.loadRole(student.getId());
////        MapMessage message = babelServiceClient.exchangeStar(role, request.count);
////        if (!message.isSuccess()) {
////            if (message.containsKey("usableIntegral")) {
////                response.integralCount = (Integer) message.get("usableIntegral");
////            }
////            return response.toResponse();
////        }
////        role = (BabelRole) message.get("role");
////        response.starCount = role.getStarCount();
////        response.integralCount = (Integer) message.get("usableIntegral");
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 把阿分题星星兑换为通天塔星星
//     */
//    @RequestMapping(value = "exchangeAfentiStarToBabelStar.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String exchangeAfentiStarToBabelStar() {
//        ExchangeAfentiStarToBabelStarResponse response = new ExchangeAfentiStarToBabelStarResponse();
//        return response.toResponse();
//
////        ExchangeAfentiStarToBabelStarRequest request = parseRequestFromParameter("data", ExchangeAfentiStarToBabelStarRequest.class);
////        if (request == null) {
////            logger.error("ILLEGAL REQUEST PARAMETERS: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////
////        BabelStarExchangeRateConfig config = BabelStarExchangeRateConfig.getConfig(request.count);
////        if (config == null) {
////            logger.error("NO BABEL STAR EXCHANGE RATE CONFIG FOUND FOR {} STARS", request.count);
////            return response.toResponse();
////        }
////
////        User student = currentUser();
////        BabelRole role = babelLoaderClient.loadRole(student.getId());
////        if (role == null) {
////            logger.error("BABEL ROLE {} NOT LOADED", student.getId());
////            return response.toResponse();
////        }
////
////        try {
////            return atomicLockManager.wrapAtomic(this)
////                    .keyPrefix("BABEL:EXCHANGE_STAR_AFENTI")
////                    .keys(role.getRoleId())
////                    .proxy()
////                    .internalExchangeAfentiStarToBabelStar(student, request, response, config, role);
////        } catch (CannotAcquireLockException ex) {
////            logger.error("BABEL ROLE {} FAILED TO EXCHANGE AFENTI STAR -> BABEL STAR: DUPLICATED OPERATION", role.getRoleId());
////            return response.toResponse();
////        }
//    }
//
//    protected String internalExchangeAfentiStarToBabelStar(User student,
//                                                           ExchangeAfentiStarToBabelStarRequest request,
//                                                           ExchangeAfentiStarToBabelStarResponse response,
//                                                           BabelStarExchangeRateConfig config,
//                                                           BabelRole role) {
//        response.afentiStarCount = 0;
//        return response.toResponse();
//    }
//
//    /**
//     * 使用换题卡
//     */
//    @RequestMapping(value = "substitute.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String substitute() {
//        SubstituteResponse response = new SubstituteResponse();
//        return response.toResponse();
//
////        User user = currentUser();
////        if (user == null) {
////            logger.error("RE-LOGIN IS REQUIRED, NO USER FOUND");
////            return response.toResponse();
////        }
////
////        BabelRole babelRole = babelLoaderClient.loadRole(user.getId());
////        if (babelRole == null) {
////            logger.error("BABEL ROLE {} NOT LOADED", user.getId());
////            return response.toResponse();
////        }
////
////        MapMessage message = babelServiceClient.useItem(babelRole, new BabelQuantitiedItem(BabelItem.SUBSTITUTE, 1));
////        if (!message.isSuccess()) {
////            BabelBag babelBag = babelLoaderClient.loadBag(babelRole.getRoleId());
////            if (babelBag != null) {
////                for (RoleItem roleItem : babelBag.removeZeroItem().getItemList()) {
////                    response.itemList.add(roleItem.toBagItem());
////                }
////            }
////            return response.toResponse();
////        }
////
////        BabelBag babelBag = (BabelBag) message.get("bag");
////        for (RoleItem roleItem : babelBag.removeZeroItem().getItemList()) {
////            response.itemList.add(roleItem.toBagItem());
////        }
////
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 获取每日已完成任务
//     */
//    @RequestMapping(value = "getDailyMissionDone.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getDailyMissionDone() {
//        DailyMissionDoneResponse response = GetDailyMissionDoneRequest.newResponse();
//        return response.toResponse();
//
////        BabelDailyMissionResponse dm = asyncBabelCacheServiceClient.getAsyncBabelCacheService()
////                .BabelDailyMissionManager_load(currentUserId())
////                .take();
////        if (!dm.isSuccess()) {
////            logger.error("Failed to load BABEL role {} daily missions", currentUserId());
////            return response.toResponse();
////        }
////        response.doneMissionList.addAll(dm.toFinishedList());
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 领取每日任务奖励
//     */
//    @RequestMapping(value = "exchangeDailyMissionPrize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String exchangeDailyMissionPrize() {
//        ExchangeDailyMissionPrizeResponse response = new ExchangeDailyMissionPrizeResponse();
//        return response.toResponse();
//
////        ExchangeDailyMissionPrizeRequest request = parseRequestFromParameter("data", ExchangeDailyMissionPrizeRequest.class);
////        if (request == null) {
////            logger.error("ILLEGAL REQUEST PARAMETER: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////
////        DailyMission mission;
////        try {
////            mission = DailyMission.valueOf(request.missionName);
////        } catch (Exception ex) {
////            logger.error("ILLEGAL MISSION NAME SPECIFIED: {}", request.missionName);
////            return response.toResponse();
////        }
////
////        BabelRole role = currentBabelRole();
////        if (role == null) {
////            logger.error("BABEL ROLE {} NOT LOADED", currentUserId());
////            return response.toResponse();
////        }
////
////        MapMessage message = babelServiceClient.claimReward(role, mission);
////        if (!message.isSuccess()) {
////            return response.toResponse();
////        }
////
////        if (message.get("role") instanceof BabelRole) {
////            response.starCount = ((BabelRole) message.get("role")).getStarCount();
////        }
////        BabelDailyMissionResponse dm = asyncBabelCacheServiceClient.getAsyncBabelCacheService()
////                .BabelDailyMissionManager_load(role.getRoleId())
////                .take();
////        if (dm.isSuccess()) {
////            response.doneMissionList.addAll(dm.toFinishedList());
////        }
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 消获取用户背包信息
//     */
//    @RequestMapping(value = "getBag.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getBag() {
//        GetBagResponse response = new GetBagResponse();
//        return response.toResponse();
//
////        User user = currentUser();
////        if (user == null) {
////            logger.error("RE-LOGIN IS REQUIRED, NO USER FOUND");
////            return response.toResponse();
////        }
////
////        BabelBag bag = babelLoaderClient.loadBag(user.getId());
////        if (null == bag) {
////            response.success = false;
////            return response.toResponse();
////        }
////
////        response.success = true;
////        response.fillFrom(bag);
////        return response.toResponse();
//    }
//
//    /**
//     * 送礼品
//     */
//    @RequestMapping(value = "sendGift.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String sendGift() {
//        SendGiftResponse response = new SendGiftResponse();
//        return response.toResponse();
//
////        SendGiftRequest request = parseRequestFromParameter("data", SendGiftRequest.class);
////        if (request == null) {
////            logger.error("ILLEGAL REQUEST PARAMETER: {}", getRequestParameter("data", ""));
////            return response.toResponse();
////        }
////
////        final StudentDetail student = currentStudentDetail();
////        final long receiverId;
////        try {
////            receiverId = Long.parseLong(request.targetUserId);
////        } catch (Exception ex) {
////            logger.error("UNRECOGNIZED RECEIVER ID: {}", request.targetUserId);
////            return response.toResponse();
////        }
////        if (Objects.equals(student.getId(), receiverId)) {
////            logger.error("CANNOT SEND GIFT TO YOURSELF {}", student.getId());
////            return response.toResponse();
////        }
////
////        final BabelGiftConfig giftConfig = BabelGiftConfig.getConfig(request.giftId);
////        if (giftConfig == null) {
////            logger.error("ILLEGAL GIFT ID {} SPECIFIED", request.giftId);
////            return response.toResponse();
////        }
////
////        StudentDetail targetDetail = studentLoaderClient.loadStudentDetail(receiverId);
////        if (null == targetDetail) {
////            logger.error("NULL_RECEIVER: {}", request.targetUserId);
////            return response.toResponse();
////        }
////
////        if (null == student.getClazzId()) {
////            logger.error("NULL_SENDER_CLAZZ_ID: {}", student.getId());
////            return response.toResponse();
////        }
////
////        if (null == targetDetail.getClazzId()) {
////            logger.error("NULL_RECEIVER_CLAZZ_ID: {}", targetDetail.getId());
////            return response.toResponse();
////        }
////
////        if (!targetDetail.getClazzId().equals(student.getClazzId())) {
////            logger.error("SENDER_{}_RECEIVER_{}_CLAZZ_ID_DIFFERENT", student.getId(), targetDetail.getId());
////            return response.toResponse();
////        }
////        RoleGift roleGift = new RoleGift();
////        roleGift.setItemId(giftConfig.getItemId());
////        roleGift.setCount(1);
////        roleGift.setReceiveTime(System.currentTimeMillis());
////        roleGift.setRewardType(giftConfig.getRewardType());
////        roleGift.setSenderId(student.getId());
////        roleGift.setSenderName(student.fetchRealname());
////
////        BabelRole role = babelLoaderClient.loadRole(student.getId());
////        MapMessage message = babelServiceClient.sendGift(role, receiverId, giftConfig.getGiftId(), roleGift);
////        if (!message.isSuccess()) {
////            return response.toResponse();
////        }
////        BabelGiftPool giftPool = (BabelGiftPool) message.get("giftPool");
////        response.sendableGiftList.addAll(BabelGiftPoolUtils.toSendableGiftList(giftPool));
////        response.success = true;
////
////        //弹窗通知收礼人
////        String content = "{}同学送给你一个{}，快去回赠一个礼物吧！" +
////                "<a href=\"/student/babel/api/index.vpage\">去看看</a>";
////        content = StringUtils.formatMessage(content, student.fetchRealname(), giftConfig.giftName());
////        userPopupServiceClient.createPopup(receiverId)
////                .content(content)
////                .type(PopupType.BABEL_SEND_GIFT)
////                .category(PopupCategory.LOWER_RIGHT)
////                .create();
////
////        return response.toResponse();
//    }
//
//    /**
//     * 获取同班同学
//     */
//    @RequestMapping(value = "loadClassmate.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String loadClassmate() {
//        LoadClassmateResponse response = new LoadClassmateResponse();
//        return response.toResponse();
//
////        List<User> classmates = studentLoaderClient.loadStudentClassmates(currentUserId());
////        StudentDetail studentDetail = currentStudentDetail();
////        List<User> classmates = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(studentDetail.getClazzId(), studentDetail.getId());
////        for (User student : classmates) {
////            Classmate cm = new Classmate();
////            cm.userId = String.valueOf(student.getId());
////            cm.userName = student.fetchRealname();
////            cm.logo = getUserAvatarImgUrl(student.fetchImageUrl());
////            response.classmate.add(cm);
////        }
////        response.success = true;
////        return response.toResponse();
//    }
//
//    /**
//     * 星空图 缓存每日一更新
//     */
//    @RequestMapping(value = "getStarMap.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getStarMap() throws Exception {
//        return new GetStarMapResponse().toResponse();
//
////        User user = currentUser();
////        if (user == null) {
////            logger.error("RE-LOGIN IS REQUIRED, NO USER FOUND");
////            return new GetStarMapResponse().toResponse();
////        }
////
////        BabelEnglishStarMapManager.Loader loader = new BabelEnglishStarMapManager.Loader() {
////            @Override
////            public GetStarMapResponse load(Long roleId) {
////                PsrPrimaryAppEnUserEks myEk = null;
////                try {
////                    myEk = utopiaPsrServiceClient.getPsrPrimaryAppEnEksByUserId("BABEL", roleId);
////                } catch (Exception ex) {
////                    logger.warn("Failed to load babel role {} PSR eks", roleId, ex);
////                }
////
////                Map<String, String> ekMap = new HashMap<>();
////                if (null != myEk && myEk.getErrorContent().equals("success")) {
////                    for (PsrPrimaryAppEnUserEkItem ek : myEk.getEkList()) {
////                        ekMap.put(StringUtils.substring(ek.getEk(), StringUtils.indexOf(ek.getEk(), "#") + 1),
////                                ek.getStatus().toString());
////                    }
////                }
////
////                BabelRole babelRole = babelLoaderClient.loadRole(roleId);
////                if (babelRole == null) {
////                    logger.error("NO BABEL ROLE {} LOADED", roleId);
////                    return null;
////                }
////                Long englishBookId = babelRole.parseEnglishBookId();
////                Book myBook = englishContentLoaderClient.loadEnglishBook(englishBookId);
////                if (myBook == null) {
////                    logger.error("BABEL ROLE {} HAS NO ENGLISH BOOK", roleId);
////                    return null;
////                }
////                GetStarMapResponse resp = new GetStarMapResponse();
////                resp.bookId = String.valueOf(myBook.getId());
////                resp.bookName = myBook.getCname();
////                resp.imgUrl = myBook.getImgUrl();
////                resp.bookUnit = new ArrayList<>();
////                Map<Unit, List<Sentence>> myBookAllSentence = getAllSentenceOrdrByUnit(englishBookId);
////                for (Map.Entry<Unit, List<Sentence>> entry : myBookAllSentence.entrySet()) {
////                    StarUnit unit = new StarUnit();
////                    unit.fillFrom(entry.getKey());
////                    List<StarSentence> startSentenceList = new ArrayList<>(entry.getValue().size());
////                    for (Sentence st : entry.getValue()) {
////                        StarSentence starSentence = new StarSentence();
////                        starSentence.fillFrom(st);
////                        if (ekMap.containsKey(st.getEnText())) {
////                            starSentence.status = ekMap.get(st.getEnText());
////                        } else {
////                            if (st.getType() != 1) {//非词汇类的知识点，跳过
////                                continue;
////                            }
////
////                            starSentence.status = "E";
////                        }
////
////                        startSentenceList.add(starSentence);
////                    }
////
////                    unit.unitSentence = startSentenceList;
////                    resp.bookUnit.add(unit);
////                }
////
////                resp.success = true;
////                return resp;
////            }
////        };
////        BabelEnglishStarMapManager babelEnglishStarMapManager = new BabelEnglishStarMapManager();
////        return babelEnglishStarMapManager.loadBabelStarMap(user.getId(), loader);
//    }
//
//    /**
//     * 数学星空图 缓存每日一更新
//     */
//    @RequestMapping(value = "getMathStarMap.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getMathStarMap() throws Exception {
//        User user = currentUser();
//        if (user == null) {
//            logger.error("RE-LOGIN IS REQUIRED, NO USER FOUND");
//            return new GetMathStarMapResponse().toResponse();
//        }
//
//        BabelMathStarMapManager.Loader loader = new BabelMathStarMapManager.Loader() {
//            @Override
//            public GetMathStarMapResponse load(Long roleId) {
//                PsrPrimaryAppUserEks myEk = null;
//                try {
//                    myEk = utopiaPsrServiceClient.getPrimaryAppMathEksByUserId("BABEL", currentUserId());
//                } catch (Exception ignored) {
//                }
//
//                Map<String, String> ekMap = new HashMap<>();
//                if (null != myEk && myEk.getErrorContent().equals("success")) {
//                    for (PsrPrimaryAppUserEkItem ek : myEk.getEkList()) {
//                        ekMap.put(StringUtils.substring(ek.getEk(), StringUtils.indexOf(ek.getEk(), "#") + 1), ek.getStatus().toString());
//                    }
//
//                }
//
//                GetMathStarMapResponse resp = new GetMathStarMapResponse();
//                String bookIdStr = babelLoaderClient.loadRole(currentUserId()).getMathBook();
//                if (StringUtils.isEmpty(bookIdStr)) {
//                    return null;
//                }
//
//                long mathBookId = NumberUtils.toLong(bookIdStr);
//                MathBook myMathBook = mathContentLoaderClient.loadMathBook(mathBookId);
//                Map<MathBook, List<MathPoint>> allBookPoint = mathContentLoaderClient.getExtension().loadMathBookPointMapByPressName(myMathBook.getPress());
//                List<MathStarBook> rtnLst = new ArrayList<>();
//                mathPaintedSkin(new ArrayList<>(allBookPoint.keySet()));
//                for (Map.Entry<MathBook, List<MathPoint>> entry : allBookPoint.entrySet()) {
//                    List<BabelMathPoint> pointLst = new ArrayList<>();
//                    for (MathPoint pt : entry.getValue()) {
//                        BabelMathPoint bpt = new BabelMathPoint();
//                        bpt.pointName = pt.getPointName();
//                        if (ekMap.containsKey(pt.getPointName())) {
//                            bpt.status = ekMap.get(pt.getPointName());
//                        } else {
//                            bpt.status = "E";
//                        }
//
//                        pointLst.add(bpt);
//                    }
//
//                    MathStarBook starBook = new MathStarBook();
//                    starBook.fillFieldValue("bookContent", entry.getKey());
//                    starBook.pointList = pointLst;
//                    rtnLst.add(starBook);
//                }
//
//                resp.success = true;
//                resp.bookList = rtnLst;
//                return resp;
//            }
//        };
//
//        BabelMathStarMapManager babelMathStarMapManager = new BabelMathStarMapManager();
//        return babelMathStarMapManager.loadBabelStarMap(user.getId(), loader);
//    }
//
//    /**
//     * 获取班内排行
//     */
//    @RequestMapping(value = "loadClazzRank.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String loadClazzRank() {
//        LoadClazzRankResponse response = new LoadClazzRankResponse();
//
//        StudentDetail student = currentStudentDetail();
//        if (student.getClazzId() == null) {
//            logger.warn("Student {} has no clazz", student.getId());
//            return response.toResponse();
//        }
//
//        BabelClazzRankManager babelClazzRankManager = new BabelClazzRankManager();
//        BabelClazzRankResponse rank = babelClazzRankManager.load(
//                student.getClazzId(),
//                clazzId -> {
//                    List<Long> userIds = asyncGroupServiceClient.findStudentIdsByClazzIdWithCache(clazzId);
//                    return new ArrayList<>(userLoaderClient.loadUsers(userIds).values());
//                },
//                students -> {
//                    Set<StudentClazzRankInfo> result = new HashSet<>();
//                    Set<Long> roleIds = students.stream()
//                            .map(User::getId)
//                            .filter(t -> t != null)
//                            .collect(Collectors.toCollection(LinkedHashSet::new));
//                    Map<Long, BabelRole> roles = babelLoaderClient.loadRoles(roleIds);
//                    for (User classmate : students) {
//                        StudentClazzRankInfo rankInfo = new StudentClazzRankInfo();
//                        rankInfo.userId = String.valueOf(classmate.getId());
//                        rankInfo.imgUrl = getUserAvatarImgUrl(classmate.fetchImageUrl());
//                        rankInfo.userName = classmate.fetchRealname();
//                        BabelRole role = roles.get(classmate.getId());
//                        if (null != role) {
//                            rankInfo.floor = role.getFloor();
//                            rankInfo.stageIndex = role.getStageIndex();
//                            rankInfo.starCount = role.getStarCount();
//                        }
//                        result.add(rankInfo);
//                    }
//                    return result;
//                }
//        );
//        if (!rank.isSuccess()) {
//            return response.toResponse();
//        }
//        response.rankList.addAll(rank.getRanks());
//        response.success = true;
//        return response.toResponse();
//    }
//
//    /**
//     * 购物
//     */
//    @RequestMapping(value = "sendShoppingList.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String sendShoppingList() {
//        SendShoppingListResponse response = SendShoppingListRequest.newResponse();
//        SendShoppingListRequest request = parseRequestFromParameter("data", SendShoppingListRequest.class);
//        if (request == null) {
//            logger.error("ILLEGAL REQUEST PARAMETERS: {}", getRequestParameter("data", ""));
//            return response.toResponse();
//        }
//
//        List<BabelQuantitiedItem> shoppingList = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(request.shoppingList)) {
//            for (ShoppingItem shoppingItem : request.shoppingList) {
//                BabelItem item = BabelItem.getByItemId(shoppingItem.itemId);
//                if (item == null) {
//                    logger.error("UNRECOGNIZED ITEM ID: {}", shoppingItem.itemId);
//                    return response.toResponse();
//                }
//                shoppingList.add(new BabelQuantitiedItem(item, shoppingItem.count));
//            }
//        }
//        if (CollectionUtils.isEmpty(shoppingList)) {
//            logger.error("EMPTY SHOPPING LIST");
//            return response.toResponse();
//        }
//
//        User user = currentUser();
//
//        switch (request.payType) {
//            case BabelConstants.PAY_TYPE_BABEL_STAR: {
//                BabelRole role = babelLoaderClient.loadRole(user.getId());
//                MapMessage message = babelServiceClient.buyItems(role, Currency.BABEL_STAR, shoppingList);
//                if (!message.isSuccess()) {
//                    return response.toResponse();
//                }
//                role = (BabelRole) message.get("role");
//                response.starCount = role.getStarCount();
//                BabelBag bag = (BabelBag) message.get("bag");
//                for (RoleItem roleItem : bag.getItemList()) {
//                    response.itemList.add(roleItem.toBagItem());
//                }
//                response.passwordOk = true;
//                response.success = true;
//                return response.toResponse();
//            }
//            case BabelConstants.PAY_TYPE_INTEGRAL: {
//                UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
//                if (StringUtils.isNotEmpty(ua.getPaymentPassword())) {
//                    Password password = Password.of(ua.getPaymentPassword());
//                    String paymentPassword = Password.obscurePassword(request.password, password.getSalt());
//                    if (!StringUtils.equals(paymentPassword, password.getPassword())) {
//                        logger.error("USER {} PAYMENT PASSWORD AUTHENTICATION FAILED", user.getId());
//                        return response.toResponse();
//                    }
//                }
//                BabelRole role = babelLoaderClient.loadRole(user.getId());
//                MapMessage message = babelServiceClient.buyItems(role, Currency.INTEGRAL, shoppingList);
//                if (!message.isSuccess()) {
//                    return response.toResponse();
//                }
//                UserIntegral userIntegral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(user.getId());
//                response.integralCount = (int) userIntegral.getUsable();
//                BabelBag bag = (BabelBag) message.get("bag");
//                for (RoleItem roleItem : bag.getItemList()) {
//                    response.itemList.add(roleItem.toBagItem());
//                }
//                response.passwordOk = true;
//                response.success = true;
//                return response.toResponse();
//            }
//            default: {
//                logger.error("UNRECOGNIZED PAY TYPE: {}", request.payType);
//                return response.toResponse();
//            }
//        }
//    }
//
//    /**
//     * 打开班级争霸赛窗口
//     */
//    @RequestMapping(value = "openClazzBattleWindow.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String openClazzBattleWindow() {
//        OpenClazzBattleWindowResponse response = new OpenClazzBattleWindowResponse();
//        BabelRole role = currentBabelRole();
//        if (role == null) {
//            logger.error("Failed to load BABEL role {}", currentUserId());
//            return response.toResponse();
//        }
//        MapMessage message = babelServiceClient.enterClazzBattle(role, currentStudentDetail());
//        if (!message.isSuccess()) {
//            response.failReason = "服务器错误_后端服务失败";
//            return response.toResponse();
//        }
//        response.isBattleOpen = (Boolean) message.get("isBattleOpen");
//        response.battleFloor = (Integer) message.get("battleFloor");
//        if (response.isBattleOpen) {
//            response.battleOverCountdown = (Integer) message.get("battleOverCountdown");
//        } else {
//            response.battleStartCountdown = (Integer) message.get("battleStartCountdown");
//        }
//        response.playClazzCount = String.valueOf(message.get("playClazzCount"));
//        response.isMyClazzQualified = (Boolean) message.get("isMyClazzQualified");
//        response.myClazzScore = String.valueOf(message.get("myClazzScore"));
//        if (message.containsKey("topClazzName")) {
//            response.topClazzName = (String) message.get("topClazzName");
//        }
//        if (message.containsKey("topClazzScore")) {
//            response.topClazzScore = String.valueOf(message.get("topClazzScore"));
//        }
//        response.myClazzName = (String) message.get("myClazzName");
//        Collection topHistories = (Collection) message.get("topHistories");
//        for (Object topHistory : topHistories) {
//            response.clazzTopHistory.add((ClazzBattleTopHistory) topHistory);
//        }
//        Collection studentScores = (Collection) message.get("studentScores");
//        for (Object studentScore : studentScores) {
//            response.clazzBattleStudentScore.add((ClazzBattleStudentScore) studentScore);
//        }
//        Collection topPrize = (Collection) message.get("topPrize");
//        for (Object tp : topPrize) {
//            response.topPrize.add((RewardInfo) tp);
//        }
//        Collection clazzPrices = (Collection) message.get("clazzPrices");
//        for (Object clazzPrice : clazzPrices) {
//            response.clazzBattlePrizeInfo.add((ClazzBattlePrizeInfo) clazzPrice);
//        }
//
//        response.success = true;
//        return response.toResponse();
//    }
//
//    // ========================================================================
//    // PRIVATE METHODS
//    // ========================================================================
//
//    /**
//     * 检查我是否有书，及书是否符合我的年龄段。如书为空，走推荐书逻辑。如果书过期，则提示选书
//     */
//    @SuppressWarnings("ConstantConditions")
//    private void validateBabelBooks(InitInfoResponse response, StudentDetail student, BabelRole role, int myLevel) {
//        Map<String, String> updateBookCandidate = new HashMap<>();
//
//        Long englishBookId = role.parseEnglishBookId();
//        if (englishBookId != null) {
//            Book englishBook = englishContentLoaderClient.loadEnglishBook(englishBookId);
//            if (englishBook == null) {
//                logger.warn("BABEL role {} english book {} not found, set need change to TRUE",
//                        role.getRoleId(), englishBookId);
//                response.needChangeEnglishBook = true;
//            } else {
//                engPaintedSkin(Collections.singletonList(englishBook));
//                response.englishBook = toBabelBook(englishBook);
//            }
//        } else {
//            // no english found, fetch recommended books
//            NewBookProfile newBookProfile = newClazzBookLoaderClient.fetchUserBook(student, Subject.ENGLISH);
//            if (newBookProfile != null && newBookProfile.getOldId() != null) {
//                Book englishBook = englishContentLoaderClient.loadEnglishBook(newBookProfile.getOldId());
//                if (myLevel > 2) {
//                    response.needChangeEnglishBook = true;
//                } else {
//                    engPaintedSkin(Collections.singletonList(englishBook));
//                    response.englishBook = toBabelBook(englishBook);
//                    updateBookCandidate.put(BabelRole.ENGLISH_BOOK_FIELD, String.valueOf(englishBook.getId()));
//                }
//            } else {
//                // no recommended book found, set book manually
//                response.needChangeEnglishBook = true;
//            }
//        }
//
//        Long mathBookId = role.parseMathBookId();
//        if (mathBookId != null) {
//            MathBook mathBook = mathContentLoaderClient.loadMathBook(NumberUtils.toLong(role.getMathBook()));
//            if (mathBook == null) {
//                logger.warn("BABEL role {} math book {} not found, set need change to TRUE",
//                        role.getRoleId(), mathBookId);
//                response.needChangeMathBook = true;
//            } else {
//                mathPaintedSkin(Collections.singletonList(mathBook));
//                response.mathBook = toBabelBook(mathBook);
//            }
//        } else {
//            // no math found, fetch recommended books
//            NewBookProfile newBookProfile = newClazzBookLoaderClient.fetchUserBook(student, Subject.MATH);
//            if (newBookProfile != null && newBookProfile.getOldId() != null) {
//                MathBook mathBook = mathContentLoaderClient.loadMathBook(newBookProfile.getOldId());
//                mathPaintedSkin(Collections.singletonList(mathBook));
//                response.mathBook = toBabelBook(mathBook);
//                updateBookCandidate.put(BabelRole.MATH_BOOK_FIELD, String.valueOf(mathBook.getId()));
//            } else {//没书，需要手动选了
//                response.needChangeMathBook = true;
//            }
//
//        }
//
//        if (!updateBookCandidate.isEmpty()) {
//            MapMessage message = babelServiceClient.setBooks(role, updateBookCandidate);
//            if (!message.isSuccess()) {
//                if (updateBookCandidate.containsKey(BabelRole.ENGLISH_BOOK_FIELD)) {
//                    response.needChangeEnglishBook = true;
//                }
//                if (updateBookCandidate.containsKey(BabelRole.MATH_BOOK_FIELD)) {
//                    response.needChangeMathBook = true;
//                }
//            }
//        }
//    }
//
//    private static BabelBook toBabelBook(Book source) {
//        if (source == null) {
//            return null;
//        }
//        BabelBook target = new BabelBook();
//        target.id = source.getId() == null ? "" : source.getId().toString();
//        target.cname = StringUtils.defaultString(source.getCname());
//        target.viewContent = StringUtils.defaultString(source.getViewContent());
//        target.color = StringUtils.defaultString(source.getColor());
//        return target;
//    }
//
//    private static BabelBook toBabelBook(MathBook source) {
//        if (source == null) {
//            return null;
//        }
//        BabelBook target = new BabelBook();
//        target.id = source.getId() == null ? "" : source.getId().toString();
//        target.cname = StringUtils.defaultString(source.getCname());
//        target.viewContent = StringUtils.defaultString(source.getViewContent());
//        target.color = StringUtils.defaultString(source.getColor());
//        return target;
//    }
}
