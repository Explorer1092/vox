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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * BABEL save controller implementation.
 *
 * @author Sadi Wan
 * @since Jun 12, 2014
 */
@Controller
@RequestMapping("/student/babel/api/save")
public class BabelSaveController extends AbstractBabelController {

//    @Inject private AsyncBabelCacheServiceClient asyncBabelCacheServiceClient;
//    @Inject private ClazzLoaderClient clazzLoaderClient;
//    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;
//
//    /**
//     * 更新选书
//     */
//    @RequestMapping(value = "updateMyBook.vpage", method = {RequestMethod.POST})
//    @ResponseBody
//    public MapMessage updateMyBook() {
//        String subjectValue = getRequestParameter("subject", "");
//        if (!Subject.isValidSubject(subjectValue)) {
//            logger.error("ILLEGAL SUBJECT VALUE SPECIFIED: {}", subjectValue);
//            return MapMessage.errorMessage("参数非法");
//        }
//        Subject subject = Subject.valueOf(subjectValue);
//
//        String bookIdValue = getRequestParameter("bookId", "");
//        Long bookId;
//        try {
//            bookId = Long.parseLong(bookIdValue);
//        } catch (Exception ex) {
//            logger.error("ILLEGAL BOOK ID SPECIFIED: {}", bookIdValue);
//            return MapMessage.errorMessage("参数非法");
//        }
//
//        StudentDetail student = currentStudentDetail();
//        if (student == null) {
//            logger.error("RE-LOGIN IS REQUIRED, NO USER FOUND");
//            return MapMessage.errorMessage("用户未登录");
//        }
//
//        if (student.getClazzLevelAsInteger() == null) {
//            logger.error("USER {} HAS NO CLAZZ", student.getId());
//            return MapMessage.errorMessage("用户无班级");
//        }
//
//        BabelRole role = babelLoaderClient.loadRole(student.getId());
//        if (role == null) {
//            logger.error("BABEL role {} not loaded", student.getId());
//            return MapMessage.errorMessage("用户无角色");
//        }
//        int clazzLevel = Math.min(ClazzLevel.SIXTH_GRADE.getLevel(), student.getClazzLevelAsInteger());
//        switch (subject) {
//            case ENGLISH: {
//                Book book = englishContentLoaderClient.loadEnglishBook(bookId);
//                if (null == book || (!Integer.valueOf(1).equals(book.getOpenExam()) && clazzLevel > 2)) {
//                    //不存在的书|书没开放应试
//                    return MapMessage.errorMessage("错误的书本id");
//                }
//
//                LinkedHashMap<String, String> input = new LinkedHashMap<>(1);
//                input.put(BabelRole.ENGLISH_BOOK_FIELD, bookIdValue);
//                if (!babelServiceClient.setBooks(role, input).isSuccess()) {
//                    return MapMessage.errorMessage("更换书本错误");
//                }
//                BabelEnglishStarMapManager babelEnglishStarMapManager = new BabelEnglishStarMapManager();
//                babelEnglishStarMapManager.clearBabelStarMap(student.getId());
//                break;
//            }
//            case MATH: {
//                MathBook mathBook = mathContentLoaderClient.loadMathBook(bookId);
//                if (null == mathBook) {
//                    return MapMessage.errorMessage("错误的书本id");
//                }
//
//                LinkedHashMap<String, String> input = new LinkedHashMap<>(1);
//                input.put(BabelRole.MATH_BOOK_FIELD, bookIdValue);
//                if (!babelServiceClient.setBooks(role, input).isSuccess()) {
//                    return MapMessage.errorMessage("更换书本错误");
//                }
//                BabelMathStarMapManager babelMathStarMapManager = new BabelMathStarMapManager();
//                babelMathStarMapManager.clearBabelStarMap(student.getId());
//                break;
//            }
//            default: {
//                logger.error("UNSUPPORTED SUBJECT: {}", subject);
//                return MapMessage.errorMessage("参数非法");
//            }
//        }
//        return MapMessage.successMessage().setInfo("操作成功");
//    }
//
//    /**
//     * 领取BOSS战奖品
//     */
//    @RequestMapping(value = "exchangeBossPrize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String exchangeBossPrize() {
//        ExchangeBossPrizeResponse response = ExchangeBossPrizeRequest.newResponse();
//        ExchangeBossPrizeRequest request = parseRequestFromParameter("data", ExchangeBossPrizeRequest.class);
//        if (request == null) {
//            logger.error("Illegal request parameter: {}", getRequestParameter("data", ""));
//            response.failReason = "网络错误";
//            return response.toResponse();
//        }
//        if (request.prize == null) {
//            return response.toResponse();
//        }
//        BabelRole role = currentBabelRole();
//        if (role == null) {
//            logger.error("Failed to load BABEL role {}", currentUserId());
//            return response.toResponse();
//        }
//
//        response.newSecureKey = getCurrentKey(currentUserId(), SecureKeyType.PK通天塔通用);
//        response.keyCheckOk = true;
//        if (null == request.prize) {
//            response.failReason = "参数错误";
//        }
//
//        BabelStoredPrize prize = new BabelStoredPrize(
//                NumberUtils.toLong(request.prize.receiveTime),
//                request.prize.randomId,
//                RewardType.valueOf(request.prize.rewardType),
//                request.prize.itemId,
//                request.prize.count
//        );
//
//        if (prize.getRewardType() == RewardType.PK_ITEM) {
//            //PK道具的话，需要先看是否背包空间足够
//            if (pkLoaderClient.getBagEquipmentSpaceLeft(currentUserId()) < prize.getCount()) {//要兑换PK武装，可背包没地方了
//                response.failReason = "NO_BAG_SPACE";
//                return response.toResponse();
//            }
//        }
//
//        MapMessage message = babelServiceClient.claimBossFightReward(role, prize);
//        if (!message.isSuccess()) {
//            response.failReason = "领奖失败:奖品已被领取/奖品不存在";
//            return response.toResponse();
//        }
//
//        if (message.containsKey("externalPrize")) {
//            prize = (BabelStoredPrize) message.get("externalPrize");
//            if (prize.getRewardType() == RewardType.PK_ITEM) {
//                MapMessage exRs = pkServiceClient.addEquipment(currentUserId(), prize.getItemId(), prize.getCount());
//                if (!exRs.isSuccess()) {
//                    logger.error("Boss prize FAILED pkServiceClient.addEquipment({})", prize.getItemId());
//                    response.failReason = "ADD_PK_ITEM_FAILED";
//                    return response.toResponse();
//                }
//            }
//        }
//
//        BabelBag bag = (BabelBag) message.get("bag");
//        if (CollectionUtils.isNotEmpty(bag.getItemList())) {
//            for (RoleItem item : bag.getItemList()) {
//                response.itemList.add(item.toBagItem());
//            }
//        }
//        if (CollectionUtils.isNotEmpty(bag.getBossPrizeList())) {
//            for (BabelStoredPrize storedPrize : bag.getBossPrizeList()) {
//                response.bossPrizeList.add(storedPrize.toBossPrizeInfo());
//            }
//        }
//        response.success = true;
//        if (message.containsKey("star")) {
//            response.starCount = (Integer) message.get("star");
//        }
//        return response.toResponse();
//    }
//
//    /**
//     * 战斗结束，发送答题结果，返回最新开启楼层关卡数
//     */
//    @RequestMapping(value = "saveBattleResult.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String saveBattleResult() {
//        SaveBattleResultResponse response = SaveBattleResultRequest.newResponse();
//
//        SaveBattleResultRequest request = parseRequestFromParameter("data", SaveBattleResultRequest.class);
//        if (request == null) {
//            response.failReason = "网络错误";
//            return response.toResponse();
//        }
//        if (null == request.resultList) {
//            request.resultList = Collections.emptyList();
//        }
//
//        StudentDetail student = currentStudentDetail();
//        if (student.getClazzId() == null) {
//            logger.error("Student {} has no clazzId, maybe cache crashed", student.getId());
//            response.failReason = "找不到学生班级";
//            return response.toResponse();
//        }
//
//        BabelRole role = babelLoaderClient.loadRole(student.getId());
//        if (role == null) {
//            logger.error("BABEL ROLE {} NOT LOADED", student.getId());
//            response.failReason = "找不到学生角色";
//            return response.toResponse();
//        }
//
//        BattleContext context = BattleContext.parse(request);
//
//        if (!context.validate(role)) {
//            logger.error("ILLEGAL REQUEST: FROM REQUEST (floor={},stageIndex={}), FROM ROLE (floor={},stageIndex={})",
//                    context.getFloor(), context.getStageIndex(), role.getFloor(), role.getStageIndex());
//            response.failReason = "当前关卡尚未开启";
//            return response.toResponse();
//        }
//        context.setClazzId(student.getClazzId());
//
//        BabelFloor floor = babelLoaderClient.loadFloor(context.getFloor());
//        if (floor == null) {
//            logger.error("BABEL FLOOR {} NOT LOADED", context.getFloor());
//            response.failReason = "无效的楼层" + context.getFloor();
//            return response.toResponse();
//        }
//
//        StageNpc npc = BabelFloor.getStageNpc(floor, context.getStageIndex(), context.getNpcIndex());
//        if (npc == null) {
//            logger.error("ILLEGAL REQUEST: NPC NOT FOUND (floor={},stageIndex={},npcIndex={})",
//                    context.getFloor(), context.getStageIndex(), context.getNpcIndex());
//            response.failReason = "无效的楼层Npc" + context.getFloor();
//            return response.toResponse();
//        }
//
//        context.getExtensionAttributes().put("floor", floor);
//        context.getExtensionAttributes().put("npc", npc);
//
//        final String checkKeyRs = checkKeyForCurrentUser(request.token, SecureKeyType.PK通天塔通用);
//        response.newSecureKey = genSecureKeyForCurrentUser(SecureKeyType.PK通天塔通用);
//        if (StringUtils.isNoneEmpty(checkKeyRs)) {
//            logger.warn("made an illegal request /saveBattleResult.vpage.{}", checkKeyRs);
//            response.failReason = "token错误";
//            return response.toResponse();
//        }
//
//        response.keyCheckOk = true;
//
//        //先把答题结果持久一下
//        try {
////            saveResults(role, npc, student.getClazzId(), request);
//        } catch (UtopiaRuntimeException e) {
//            response.failReason = "恶意刷题";
//            return response.toResponse();
//        }
//
//
//        if (request.npcIndex == 0) {//打的每关第一个怪，需要扣精力
//
//            BabelVitalityResponse bvr = null;
//            boolean decVitalitySuccess = false;
//            bvr = babelVitalityServiceClient.decreaseVitality(role.getRoleId(), 1, "打完每关第一个怪，扣活力");
//            decVitalitySuccess = bvr.isSuccess();
//
//            if (!decVitalitySuccess) {
//                int v = babelVitalityServiceClient.getCurrentBalance(role.getRoleId()).getBalance();
//                logger.debug("BABEL role {} has no enough vitality to decrease {}/-{}",
//                        role.getRoleId(), v, 1);
//                response.newVitality = v == 0 ? -1 : v;//打之前活力就不够,返回-1
//                if (response.newVitality == -1) {
//                    response.failReason = "活力不足";
//                    return response.toResponse();//活力本来就就不够的话，直接返回。
//                }
//            }
//            response.newVitality = bvr.getBalance();
//        } else {
//            response.newVitality = babelVitalityServiceClient.getCurrentBalance(role.getRoleId()).getBalance();
//        }
//
//        MapMessage message = babelServiceClient.battle(role, context);
//        if (!message.isSuccess()) {
//            response.failReason = "服务器错误";
//            return response.toResponse();
//        }
//
//        //加pk经验
//        pkServiceClient.addBabelExp(currentUserId(), request.win);
//
//        if (!request.win) {
//            //战斗失败，没有奖励
//            response.success = true;
//            return response.toResponse();
//        }
//
//        BabelReward externalDeliveredReward = claimExternalReward(role, (BabelReward) message.get("externalReward"));
//        if (message.containsKey("stageFinished")) {
//            asyncBabelCacheServiceClient.getAsyncBabelCacheService()
//                    .BabelDailyMissionManager_finish(role.getRoleId(), DailyMission.通过任意一关)
//                    .awaitUninterruptibly();
//        }
//
//        response.keyCount = 0;
//        response.newFloor = NumberUtils.toInt(String.valueOf(message.get("nextFloor")));
//        response.newStageIndex = NumberUtils.toInt(String.valueOf(message.get("nextStageIndex")));
//        response.success = true;
//
//        if (externalDeliveredReward != null) {
//            response.reward = externalDeliveredReward.toRewardInfo();
//        } else if (null != message.get("reward")) {
//            response.reward = ((BabelReward) message.get("reward")).toRewardInfo();
//        }
//
//        return response.toResponse();
//    }
//
//    /**
//     * 保存BOSS战结果，更新得分cache
//     */
//    @RequestMapping(value = "saveBossBattleResult.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String saveBossBattleResult() {
//        SaveBossBattleResultRequest req;
//        SaveBossBattleResultResponse resp = SaveBossBattleResultRequest.newResponse();
//        try {
//            req = SaveBossBattleResultRequest.parseRequest(getRequestParameter("data", ""));
//        } catch (Exception e) {
//            resp.failReason = "网络错误";
//            return resp.toResponse();
//        }
//
//        try {
//            final String checkKeyRs = checkKeyForCurrentUser(req.token, SecureKeyType.PK通天塔通用);
//            resp.newSecureKey = genSecureKeyForCurrentUser(SecureKeyType.PK通天塔通用);
//            if (StringUtils.isNoneEmpty(checkKeyRs)) {
//                logger.warn("made an illegal request /saveBattleResult.vpage.{}", checkKeyRs);
//                resp.failReason = "token错误";
//                return resp.toResponse();
//            }
//            resp.keyCheckOk = true;
//
//            if (currentStudentDetail().getClazzLevel().getLevel() < 3) {//3年级以下的不许玩
//                logger.warn("made an illegal request.STUDENT CLASS LEVEL LESS THAN 3");
//                resp.failReason = "BOSS战对3年及以上同学开放";
//                resp.newVitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
//                return resp.toResponse();
//            }
//            //开战时间非法
//            if (!BabelBattleTimeCalculator.getMongoSuffix(req.fightStartTime, BabelBossBattleConf.OPEN_TIME).equals(BabelBattleTimeCalculator.getUsableSuffixForNow(BabelBossBattleConf.OPEN_TIME)) || !BabelBattleTimeCalculator.isTimeLegalToBattle(req.fightStartTime, BabelBossBattleConf.OPEN_TIME)) {
//                resp.failReason = "开战时间非法";
//                resp.newVitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
//                return resp.toResponse();
//            }
//
//            final int possibleBestScore = 500;//boss战单场战斗理论最高得分。防作弊用
//            if (req.score > possibleBestScore || req.score < 0) {
//                logger.warn("USER{} TRIED_TO_SAVE_A_BOSS_BATTLE_SCORE_BEYOND_{},abort.", possibleBestScore);
//                resp.failReason = "非法的分数";
//                resp.newVitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
//                return resp.toResponse();
//            }
//            BabelVitalityResponse bvr = null;
//            boolean decVitalitySuccess = false;
//            //2014/11/6 丁老师要求BOSS战精力消耗从2改为1
//            bvr = babelVitalityServiceClient.decreaseVitality(currentUserId(), 1, "通天塔BOSS战消耗1点精力");
//            decVitalitySuccess = bvr.isSuccess();
//            if (!decVitalitySuccess) {
//                //活力不足
//                resp.newVitality = babelVitalityServiceClient.getCurrentBalance(currentUserId()).getBalance();
//                resp.failReason = "活力不足";
//                return resp.toResponse();
//            }
//            resp.newVitality = bvr.getBalance();
//            long curUserId = currentUserId();
//            BabelBossFightScore newTotal = babelServiceClient.saveScoreReturnNewTotal(curUserId, req.score, req.fightStartTime);
//
//            if (null != newTotal) {
//                resp.success = true;
//                resp.totalScore = newTotal.getScore();
//            }
//        } catch (Exception e) {
//            resp.failReason = "服务器错误";
//            logger.error("{}", e.getMessage());
//        }
//        return resp.toResponse();
//    }
//
//    /**
//     * 领取礼品
//     */
//    @RequestMapping(value = "exchangeGift.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String exchangeGift() {
//        ExchangeGiftResponse resp = ExchangeGiftRequest.newResponse();
//
//        ExchangeGiftRequest req;
//        String data = getRequestParameter("data", "");
//        try {
//            req = ExchangeGiftRequest.parseRequest(data);
//        } catch (Exception ex) {
//            logger.error("FAILED TO PARSE BABEL EXCHANGE GIFT REQUEST: {}", data);
//            resp.failReason = "网络错误";
//            return resp.toResponse();
//        }
//
//        RoleGift roleGift = new RoleGift();
//        try {
//            roleGift.setCount(req.giftItem.count);
//            roleGift.setItemId(req.giftItem.itemId);
//            roleGift.setReceiveTime(NumberUtils.toLong(req.giftItem.receiveTime));
//            roleGift.setRewardType(RewardType.valueOf(req.giftItem.rewardType));
//            roleGift.setSenderId(Long.parseLong(req.giftItem.senderId));
//            roleGift.setSenderName(req.giftItem.senderName);
//        } catch (Exception ex) {
//            resp.failReason = "参数错误";
//            return resp.toResponse();
//        }
//
//        MapMessage message = babelServiceClient.receiveGift(currentBabelRole(), roleGift);
//        if (!message.isSuccess()) {
//            resp.failReason = "服务器错误";
//            return resp.toResponse();
//        }
//        BabelRole babelRole = (BabelRole) message.get("role");
//        BabelBag babelBag = (BabelBag) message.get("bag");
//
//        resp.starCount = babelRole.getStarCount();
//        resp.fillFieldValue("itemList", babelBag.getItemList());
//        resp.fillFieldValue("giftList", babelBag.getGiftList());
//        resp.newSecureKey = getCurrentKey(currentUserId(), SecureKeyType.PK通天塔通用);
//        resp.keyCheckOk = true;
//        resp.success = true;
//        return resp.toResponse();
//    }
//
//    /**
//     * 战胜关卡BOSS获得奖励后，发送班级动态
//     */
//    @RequestMapping(value = "sendShare.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String sendShare() {
//        SendShareResponse response = SendShareRequest.newResponse();
//        SendShareRequest request = parseRequestFromParameter("data", SendShareRequest.class);
//        if (request == null) {
//            logger.error("Illegal request parameter: {}", getRequestParameter("data", ""));
//            return response.toResponse();
//        }
//
//        final String checkKeyRs = checkKeyForCurrentUser(request.token, SecureKeyType.PK通天塔通用);
//        if (!(checkKeyRs.isEmpty())) {
//            logger.warn("made an illegal request /sendShare.vpage.{}", checkKeyRs);
//            return response.toResponse();
//        }
//
//        StudentDetail student = currentStudentDetail();
//        try {
//            LinkedHashMap<String, String> map = new LinkedHashMap<>(5);
//            map.put("text", "我在通天塔" + String.valueOf(request.floor) + "层第" + String.valueOf(request.stageIndex + 1) + "关获得了".toString());
//            map.put("itemName", request.itemName);
//            map.put("itemType", request.itemType);
//            map.put("color", request.color);
//            map.put("img", request.img);
//            Clazz clazz = clazzLoaderClient.getClazzLoader().loadClazz(student.getClazzId()).getUninterruptibly();
//            if (!clazz.isSystemClazz()) {// 非系统自建班级
//                zoneQueueServiceClient.createClazzJournal(student.getClazzId())
//                        .withUser(student.getId())
//                        .withUser(student.fetchUserType())
//                        .withClazzJournalType(ClazzJournalType.BABEL_WIN_PRIZE)
//                        .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
//                        .withJournalJson(JsonUtils.toJson(map))
//                        .commit();
//            } else {// 系统自建班级
//                List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);
//                for (GroupMapper group : groups) {
//                    zoneQueueServiceClient.createClazzJournal(student.getClazzId())
//                            .withUser(student.getId())
//                            .withUser(student.fetchUserType())
//                            .withClazzJournalType(ClazzJournalType.BABEL_WIN_PRIZE)
//                            .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
//                            .withJournalJson(JsonUtils.toJson(map))
//                            .withGroup(group.getId())
//                            .commit();
//                }
//            }
//            asyncBabelCacheServiceClient.getAsyncBabelCacheService()
//                    .BabelDailyMissionManager_finish(student.getId(), DailyMission.分享状态)
//                    .awaitUninterruptibly();
//        } catch (Exception ex) {
//            logger.error("BABEL role {} failed to send share", student.getId());
//            return response.toResponse();
//        }
//
//        response.newSecureKey = getCurrentKey(student.getId(), SecureKeyType.PK通天塔通用);
//        response.keyCheckOk = true;
//        response.success = true;
//        return response.toResponse();
//    }
//
//    /**
//     * 打开班级争霸赛窗口
//     */
//    @RequestMapping(value = "exchangeClazzBattlePrize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String exchangeClazzBattlePrize() {
//        ExchangeClazzBattlePrizeResponse response = new ExchangeClazzBattlePrizeResponse();
//        ExchangeClazzBattlePrizeRequest request = parseRequestFromParameter("data", ExchangeClazzBattlePrizeRequest.class);
//        if (request == null) {
//            logger.error("Illegal request parameter: {}", getRequestParameter("data", ""));
//            response.failReason = "前端参数错误";
//            return response.toResponse();
//        }
//
//        BabelRole role = currentBabelRole();
//        if (role == null) {
//            logger.error("Failed to load BABEL role {}", currentUserId());
//            return response.toResponse();
//        }
//
//        RewardType rewardType;
//        try {
//            rewardType = RewardType.valueOf(request.prize.rewardType);
//        } catch (Exception ex) {
//            logger.error("Illegal reward type specified");
//            return response.toResponse();
//        }
//        if (!rewardType.isProcessInternal() && rewardType == RewardType.PK_ITEM) {
//            //PK道具的话，需要先看是否背包空间足够
//            if (pkLoaderClient.getBagEquipmentSpaceLeft(currentUserId()) < request.prize.count) {
//                //要兑换PK武装，可背包没地方了
//                response.failReason = "NO_BAG_SPACE";
//                return response.toResponse();
//            }
//        }
//
//        BabelStoredPrize prize = BabelStoredPrize.newInstance(request.prize);
//        MapMessage message = babelServiceClient.claimClazzBattleReward(role, prize);
//        if (!message.isSuccess()) {
//            response.failReason = "服务器端错误_远程错误";
//            return response.toResponse();
//        }
//
//        BabelClazzBattleUserPrize userPrize = (BabelClazzBattleUserPrize) message.get("clazzBattleUserPrize");
//        if (MapUtils.isNotEmpty(userPrize.getPrizeMap())) {
//            for (BabelStoredPrize storedPrize : userPrize.getPrizeMap().values()) {
//                response.clazzBattlePrizeInfo.add(storedPrize.toClazzBattlePrizeInfo());
//            }
//        }
//        if (message.containsKey("externalPrize")) {
//            BabelStoredPrize externalPrize = (BabelStoredPrize) message.get("externalPrize");
//            if (externalPrize.getRewardType() == RewardType.PK_ITEM) {
//                MapMessage mapMessage = pkServiceClient.addEquipment(currentUserId(), request.prize.itemId, request.prize.count);
//                if (!mapMessage.isSuccess()) {
//                    response.failReason = "INCREMENT_FAILED";//完蛋艹 #_#
//                    logger.error("User {} exchanged for Class battle prize {}, BUT FAIELD!", currentUserId(), JsonUtils.toJson(request.prize));
//                    return response.toResponse();
//                }
//            }
//        } else {
//            String refreshType = StringUtils.defaultString((String) message.get("refreshType"));
//            if (StringUtils.isNoneEmpty(refreshType)) {
//                response.refreshType = refreshType;
//            }
//            switch (refreshType) {
//                case "itemList": {
//                    BabelBag bag = (BabelBag) message.get("bag");
//                    if (CollectionUtils.isNotEmpty(bag.getItemList())) {
//                        for (RoleItem item : bag.getItemList()) {
//                            response.itemList.add(item.toBagItem());
//                        }
//                    }
//                    break;
//                }
//                case "petList": {
//                    BabelRolePet rolePet = (BabelRolePet) message.get("rolePet");
//                    if (CollectionUtils.isNotEmpty(rolePet.getPetList())) {
//                        for (RolePet pet : rolePet.getPetList()) {
//                            response.petList.add(pet.toRolePetInfo());
//                        }
//                    }
//                    break;
//                }
//                case "starCount": {
//                    role = (BabelRole) message.get("role");
//                    response.starCount = role.getStarCount();
//                    break;
//                }
//                default: {
//                    break;
//                }
//            }
//        }
//        response.success = true;
//        return response.toResponse();
//    }
}