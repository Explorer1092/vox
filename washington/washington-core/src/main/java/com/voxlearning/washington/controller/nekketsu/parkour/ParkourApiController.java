/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.nekketsu.parkour;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.nekketsu.consumer.NekketsuParkourClient;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourItem;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourRole;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourShopItem;
import com.voxlearning.utopia.service.nekketsu.parkour.net.messages.*;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.*;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.*;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.washington.service.LocalEnglishWordStockBuffer;
import com.voxlearning.washington.support.AbstractGameSupportController;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */

/**
 * Created by Sadi.Wan on 2014/6/12.
 */
@Controller
@RequestMapping("/student/nekketsu/parkour")
@NoArgsConstructor
public class ParkourApiController extends AbstractGameSupportController {

    @Inject private LocalEnglishWordStockBuffer localEnglishWordStockBuffer;
    @Inject private NekketsuParkourClient parkourConsumer;

    Map<String, StageWord> spStageWordMap = new HashMap<>();

    @RequestMapping(value = "load/initInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String initInfo() {
        AppPayMapper payStatus = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.Walker.name(), currentUserId());
        boolean paidUser = payStatus != null && payStatus.isActive();

        Gender gender = currentUser().fetchGender();
        if (Gender.NOT_SURE == gender) {
            gender = Gender.MALE;
        }

        MapMessage msg = parkourConsumer.initInfo(currentStudentDetail(), gender, paidUser);
        msg.add("payOpen", true);
        msg.add("paidUser", paidUser);
        msg.add("secureKey", genSecureKeyForCurrentUser(AbstractGameSupportController.SecureKeyType.沃克跑酷));
        if (msg.isSuccess()) {
            ParkourRoleInfo role = (ParkourRoleInfo) msg.get("roleInfo");
            KeyValuePair<Integer, Integer> vital = AbstractGameSupportController.GameVitalityType.热血跑酷活力.refreshVitality(
                    currentUserId(),
                    washingtonCacheSystem.CBS.unflushable
            );
            role.vitality = vital.getKey();
            role.vitalityRefillCountDown = vital.getValue();
        }
        msg.add("serverTime", String.valueOf(System.currentTimeMillis()));
        return JsonUtils.toJson(msg);
    }

    @RequestMapping(value = "load/getAllStagePuzzleList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String getAllStagePuzzleList() {
        GetAllStagePuzzleListResponse resp = parkourConsumer.getAllStagePuzzleList(currentUserId());
        List<Long> idLst = new ArrayList<>();
        AllStagePuzzleList allStagePuzzleList = resp.allStagePuzzle;
        for (StagePuzzleList pl : allStagePuzzleList.stagePuzzleList) {
            for (StageWord sw : pl.wordList) {
                idLst.add(Long.valueOf(sw.wordId));
            }
        }
        for (StagePuzzleList pl : allStagePuzzleList.stagePuzzleList) {
            for (StageWord sw : pl.wordList) {
                WordStock wordStock = localEnglishWordStockBuffer.loadWordStock(Long.valueOf(sw.wordId));
                if (wordStock != null) {
                    sw.fillFrom(wordStock);
                }
            }
        }
        return resp.toResponse();
    }

    @RequestMapping(value = "load/getStageInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String getStageInfo() {
        GetStageInfoRequest req;
        try {
            req = GetStageInfoRequest.parseRequest(getRequestParameter("data", ""));
        } catch (Exception e) {
            return new GetStageInfoResponse().toResponse();
        }
        if (req.stageId < 1) {
            return new GetStageInfoResponse().toResponse();
        }
        GetStageInfoResponse resp = parkourConsumer.getStageInfo(currentStudentDetail(), req.stageId);
        if (resp.success) {
            List<Long> idLst = new ArrayList<>(resp.wordList.size());
            for (StageWord sw : resp.wordList) {
                idLst.add(Long.parseLong(sw.wordId));
            }
            Iterator<StageWord> iter = resp.wordList.iterator();
            while (iter.hasNext()) {
                StageWord sw = iter.next();
                WordStock ws = localEnglishWordStockBuffer.loadWordStock(Long.parseLong(sw.wordId));
                if (null == ws) {
                    iter.remove();
                    continue;
                }
                sw.fillFrom(ws);
            }
        }
        return resp.toResponse();
    }

    @RequestMapping(value = "load/startGame.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String startGame() {
        StartGameRequest req;
        try {
            req = StartGameRequest.parseRequest(getRequestParameter("data", ""));
        } catch (Exception e) {
            return new StartGameResponse().toResponse();
        }
        StartGameResponse resp = StartGameRequest.newResponse();
        ParkourRole meRole = parkourConsumer.loadRole(currentUserId());
        if (null == meRole || req.stageId > meRole.getOpenStage()) {
            return resp.toResponse();
        }
        KeyValuePair<Integer, Integer> dec = AbstractGameSupportController.GameVitalityType.热血跑酷活力.incVitality(
                meRole.getRoleId(),
                washingtonCacheSystem.CBS.unflushable,
                -1);
        resp.vitality = dec.getKey();

        if (resp.vitality >= 0) {//活力还够，成功
            resp.success = true;
            resp.refreshCountdown = dec.getValue();
        } else {
            dec = AbstractGameSupportController.GameVitalityType.热血跑酷活力.refreshVitality(meRole.getRoleId(),
                    washingtonCacheSystem.CBS.unflushable);
            resp.vitality = dec.getKey();
            resp.refreshCountdown = dec.getValue();
        }
        return resp.toResponse();
    }

    private boolean isPaid() {
        boolean rtn = false;
        try {
            AppPayMapper payStatus = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.Walker.name(), currentUserId());
            rtn = payStatus != null && payStatus.isActive();
        } catch (Exception e) {
            logger.error("{} Check parkour isPaid failed. ", currentUserId());
        }
        return rtn;
    }

    @RequestMapping(value = "save/saveGameResult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String SaveGameResult() {
        SaveGameResultRequest req;
        try {
            req = SaveGameResultRequest.parseRequest(getRequestParameter("data", ""));
        } catch (Exception e) {
            return new SaveGameResultResponse().toResponse();
        }

        if (req.stageId < 1) {
            return new GetStageInfoResponse().toResponse();
        }

        String checkKeyRs = checkKeyForCurrentUser(req.token, AbstractGameSupportController.SecureKeyType.沃克跑酷);
        String newSecureKey = genSecureKeyForCurrentUser(AbstractGameSupportController.SecureKeyType.沃克跑酷);
        // FIXME COMMENT BY ZHAO REX for maybe not initialized for variable resp
        SaveGameResultResponse resp = null;
        if (!(checkKeyRs.isEmpty())) {
            logger.warn("made an illegal request.${checkKeyRs}.");
            resp = SaveGameResultRequest.newResponse();
            resp.newSecureKey = newSecureKey;
            return resp.toResponse();
        }
        switch (req.fightType) {
            case "AI":
                resp = parkourConsumer.saveNpcGameResult(currentStudentDetail(), req, isPaid());
                break;
            case "CLASSMATE":
            case "RANDOM":
                resp = parkourConsumer.saveLostOrNonNpcGameResult(currentStudentDetail(), req, isPaid());
                break;
            default:
                break;
        }
        // FIXME COMMENT BY ZHAO REX for may be NullPointerException
        resp.newSecureKey = newSecureKey;
        return resp.toResponse();
    }

    @RequestMapping(value = "load/getLoginRewardList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String getLoginRewardList() {
        Gender gender = currentUser().fetchGender();
        return parkourConsumer.getLoginRewardList(currentUserId(), gender).toResponse();
    }

    @RequestMapping(value = "load/exchangeLoginReward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String exchangeLoginReward() {
        ExchangeLoginRewardRequest req;
        try {
            req = ExchangeLoginRewardRequest.parseRequest(getRequestParameter("data", ""));
        } catch (Exception e) {
            return new ExchangeLoginRewardResponse().toResponse();
        }
        ExchangeLoginRewardResponse resp = new ExchangeLoginRewardResponse();

        if (!isPaid()) {
            resp.failReason = "NOT_PAID";
            return resp.toResponse();
        }

        Gender gender = currentUser().fetchGender();
        resp = parkourConsumer.exchangeLoginReward(currentUserId(), req.id, req.prizeDetail, gender);
//        if (resp.success) {
//            MapMessage ms = pkServiceClient.addFashions(currentUserId(), new HashSet<>(Arrays.asList(req.prizeDetail.itemId)), SourceCategory.PARKOUR);
//            if (!ms.isSuccess()) {//奖兑了，可物品没加成，麻烦了。联系客服吧
//                resp.success = false;
//                resp.failReason = "EXCHANGE_FAIL";
//                logger.error("Successfully exchanged ${req.prizeDetail.itemId} BUT FAILED in addEquipment!");
//            }
//        }
        return resp.toResponse();
    }

    @RequestMapping(value = "load/refreshMisc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String refreshMisc() {
        RefreshMiscResponse resp = new RefreshMiscResponse();
        KeyValuePair<Integer, Integer> refreshV = AbstractGameSupportController.GameVitalityType.热血跑酷活力.refreshVitality(
                currentUserId(),
                washingtonCacheSystem.CBS.unflushable
        );
        resp.newVitality = refreshV.getKey();
        resp.refreshCountdown = refreshV.getValue();
        return resp.toResponse();
    }

    @RequestMapping(value = "load/sendShopping.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String sendShopping() {
        SendShoppingRequest req;
        try {
            req = SendShoppingRequest.parseRequest(getRequestParameter("data", ""));
        } catch (Exception ignored) {
            return new SendShoppingResponse().toResponse();
        }
        SendShoppingResponse resp = new SendShoppingResponse();
        int oldVitalityTotal = AbstractGameSupportController.GameVitalityType.热血跑酷活力.refreshVitality(
                currentUserId(),
                washingtonCacheSystem.CBS.unflushable
        ).getKey();
        List<ParkourShopItem> shopItemList = parkourConsumer.loadShopItemWithCache();
        ParkourShopItem targetItem = null;
        for (ParkourShopItem it : shopItemList) {
            if (it.getId() == req.id) {
                targetItem = it;
                break;
            }
        }
        ParkourRole me = parkourConsumer.loadRole(currentUserId());
        int coinTotal = me.getCoinCount();
        resp.newVitality = oldVitalityTotal;
        resp.coinTotal = coinTotal;
        if (null == targetItem) {//买了不存在的
            resp.failReason = "ILLEGAL_ITEM";
            return resp.toResponse();
        }

        if (coinTotal < SafeConverter.toInt(targetItem.getCoinPrice())) {//钱不够
            resp.failReason = "NOT_ENOUGH_COIN";
            return resp.toResponse();
        }

        switch (targetItem.getItemType()) {
            case PK_FASHION:
//                Set<String> idSet = new HashSet<>();
//                idSet.add(targetItem.getItemId());
//                MapMessage addResp = pkServiceClient.addFashions(currentUserId(), idSet, SourceCategory.PARKOUR);
//                if (!addResp.isSuccess()) {
//                    resp.failReason = "PK_ADD_FAILED";
//                    return resp.toResponse();
//                }
                break;
            case PARKOUR_ITEM:
                switch (ParkourItem.valueOf(targetItem.getItemId())) {
                    case PI_00001://活力加满
                        if (oldVitalityTotal == AbstractGameSupportController.GameVitalityType.热血跑酷活力.getMaxVitality()) {
//体力已满，不可购买
                            resp.failReason = "VITALITY_ALREADY_FULL";
                            return resp.toResponse();
                        }
                        resp.newVitality = AbstractGameSupportController.GameVitalityType.热血跑酷活力.fillVitality(
                                currentUserId(),
                                washingtonCacheSystem.CBS.unflushable
                        ).getKey();
                        resp.vitalityAdd = resp.newVitality - oldVitalityTotal;
                        break;
                }
                break;
            default:
                break;
        }
        resp.success = true;
        Map<String, String> detail = new HashMap<>();
        detail.put("ITEM_TYPE", targetItem.getItemType().name());
        detail.put("ITEM_ID", targetItem.getItemId());
        detail.put("ITEM_PRICE", String.valueOf(SafeConverter.toInt(targetItem.getCoinPrice())));
        ParkourRole roleAfterShopping = parkourConsumer.modifyCoin(currentUserId(), -1 * SafeConverter.toInt(targetItem.getCoinPrice()), JsonUtils.toJson(detail));
        if (null == roleAfterShopping) {//减少金币出错了
            logger.error("USER {} shopped for item {} in Nekketsu Parkour which successfully got his item but failed to decrease {} coin.", currentUserId(), targetItem.getItemId(), SafeConverter.toInt(targetItem.getCoinPrice()));
            return resp.toResponse();
        }
        resp.coinTotal = roleAfterShopping.getCoinCount();
        resp.coinCost = coinTotal - resp.coinTotal;
        return resp.toResponse();
    }

    @RequestMapping(value = "load/startspgame.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String startSpGame() {
        StartSpGameResponse resp = StartSpGameRequest.newResponse();
        ParkourAi ai = new ParkourAi();//直接给个假ai
        ai.correctRate = 0.8d;
        ai.timePerQuestion = 13400;
        ai.level = 7;
        KeyValuePair<Integer, Integer> dec = AbstractGameSupportController.GameVitalityType.热血跑酷活力.incVitality(
                currentUserId(),
                washingtonCacheSystem.CBS.unflushable,
                -1);
        resp.newVitality = dec.getKey();

        if (resp.newVitality >= 0) {//活力还够，成功
            resp.success = true;
            resp.refreshCountdown = dec.getValue();
        } else {
            dec = AbstractGameSupportController.GameVitalityType.热血跑酷活力.refreshVitality(currentUserId(),
                    washingtonCacheSystem.CBS.unflushable);
            resp.newVitality = dec.getKey();
            resp.refreshCountdown = dec.getValue();
        }
        resp.stageInfo = new GetStageInfoResponse();
        resp.stageInfo.wordList = getRandomSpStageWord();
        resp.stageInfo.distance = 1000;
        resp.stageInfo.failErrorCount = 50;
        resp.stageInfo.barricadeCount = 25;
        resp.stageInfo.stageAi = ai;
        resp.stageInfo.topic = "错题";//TODO
        return resp.toResponse();
    }

    @RequestMapping(value = "save/saveSpGameResult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String saveSpGameResult() {
        SaveSpGameResultRequest req;
        try {
            req = SaveSpGameResultRequest.parseRequest(getRequestParameter("data", ""));
        } catch (Exception e) {
            return new SaveSpGameResultResponse().toResponse();
        }

        SaveSpGameResultResponse resp = SaveSpGameResultRequest.newResponse();
        String checkKeyRs = checkKeyForCurrentUser(req.token, AbstractGameSupportController.SecureKeyType.沃克跑酷);
        String newSecureKey = genSecureKeyForCurrentUser(AbstractGameSupportController.SecureKeyType.沃克跑酷);

        if (!(checkKeyRs.isEmpty())) {
            logger.warn("made an illegal request.{}.", checkKeyRs);
            resp = SaveSpGameResultRequest.newResponse();
            resp.newSecureKey = newSecureKey;
            return resp.toResponse();
        }

        final int coinBonus = 100;
        ParkourRole role;
        if (req.win) {
            role = parkourConsumer.getRemoteReference().modifyCoin(currentUserId(), coinBonus, "做题-家长微信开通7天做错题试用");
            if (null == role) {
                return resp.toResponse();
            }
            resp.coinBonus = coinBonus;
        } else {
            role = parkourConsumer.loadRole(currentUserId());
            resp.coinBonus = 0;
        }

        resp.coinTotal = role.getCoinCount();
        resp.newSecureKey = newSecureKey;
        resp.success = true;
        return resp.toResponse();
    }


    private void reloadSpStageWord() {
        if (this.spStageWordMap.isEmpty()) {
            Map<Integer, List<String>> wordMap = parkourConsumer.getRemoteReference().getStageWordMap(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
            for (List<String> wordIdList : wordMap.values()) {
                for (String wordId : wordIdList) {
                    StageWord sw = new StageWord();
                    WordStock wordStock = localEnglishWordStockBuffer.loadWordStock(Long.valueOf(wordId));
                    if (wordStock != null) {
                        sw.fillFrom(wordStock);
                        sw.wordId = String.valueOf(wordStock.getId());
                    }
                    this.spStageWordMap.put(wordId, sw);
                }
            }
        }
    }

    private List<StageWord> getRandomSpStageWord() {
        if (this.spStageWordMap.isEmpty()) {
            reloadSpStageWord();
        }
        StageWord[] rdWords = new StageWord[20];
        RandomUtils.randomPickFew(new ArrayList<>(this.spStageWordMap.values()), 20, rdWords);
        return Arrays.asList(rdWords);
    }
}
