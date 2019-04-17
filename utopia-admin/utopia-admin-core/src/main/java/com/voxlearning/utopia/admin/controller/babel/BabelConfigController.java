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

package com.voxlearning.utopia.admin.controller.babel;

import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author sadi.wan
 * @since 2014-6-20
 */
@Controller
@RequestMapping(value = "appmanager/babelConfig")
public class BabelConfigController extends CrmAbstractController {

//    /**
//     * ajax列出全部楼层全部关卡
//     *
//     * @return
//     */
//    @RequestMapping(value = "stageConfList.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String stageConfList() {
//        return JsonUtils.toJson(babelManagementClient.loadFloors());
//    }
//
//    @RequestMapping(value = "getThisClazzBattleConf.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getThisClazzBattleConf() {
//        return JsonUtils.toJson(babelManagementClient.loadBabelClazzBattleConfig(BabelBattleTimeCalculator.getUsableSuffixForNow(BabelClazzBattleConf.OPEN_TIME)));
//    }
//
//    @RequestMapping(value = "getNextClazzBattleConf.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getNextClazzBattleConf() {
//        return JsonUtils.toJson(babelManagementClient.loadBabelClazzBattleConfig(BabelBattleTimeCalculator.getNextSuffix(BabelClazzBattleConf.OPEN_TIME)));
//    }
//
//    @RequestMapping(value = "saveClazzBattleConf.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String saveClazzBattleConf() {
//        String id = getRequestString("id");
//        String nowSuffix = BabelBattleTimeCalculator.getUsableSuffixForNow(BabelClazzBattleConf.OPEN_TIME);
//        String nextSuffix = BabelBattleTimeCalculator.getNextSuffix(BabelClazzBattleConf.OPEN_TIME);
//        BabelClazzBattleConfig targetConf = null;
//        int openFloor = getRequestInt("openFloor");
//        int finishCount = getRequestInt("finishCount");
//        List<Map> prizeConf = JsonUtils.fromJsonToList(getRequestString("prizeList"), Map.class);
//        List<BabelStoredPrize> topList = new ArrayList<>();
//        for (Map<String, String> mp : prizeConf) {
//            BabelStoredPrize bp = new BabelStoredPrize(0l, 0, RewardType.valueOf(mp.get("rewardType")), mp.get("itemId"), Integer.parseInt(mp.get("count")));
//            topList.add(bp);
//        }
//        if (nowSuffix.equals(id)) {
//            if (BabelBattleTimeCalculator.isNowLegalToBattle(BabelClazzBattleConf.OPEN_TIME)) {
//                targetConf = babelManagementClient.loadBabelClazzBattleConfig(id);
//                targetConf.setWinnerPrizeList(topList);
//            } else {
//                Map<String, Object> json = new HashMap<>();
//                json.put("success", false);
//                json.put("reason", "已不可编辑");
//                return JsonUtils.toJson(json);
//            }
//        } else if (nextSuffix.equals(id)) {
//            targetConf = babelManagementClient.loadBabelClazzBattleConfig(id);
//            targetConf.setWinnerPrizeList(topList);
//            targetConf.setOpenFloor(openFloor);
//            targetConf.setMinDoneHomeworkCount(finishCount);
//        } else {
//            Map<String, Object> json = new HashMap<>();
//            json.put("success", false);
//            json.put("reason", "非法时间段");
//            return JsonUtils.toJson(json);
//        }
//
//        MapMessage message;
//        try {
//            message = atomicLockManager.wrapAtomic(babelManagementClient)
//                    .keyPrefix("BABEL_MANAGEMENT:SAVE_CLAZZ_BATTLE_CONFIG")
//                    .keys(targetConf.getId())
//                    .proxy()
//                    .saveClazzBattleConfig(targetConf);
//        } catch (CannotAcquireLockException ex) {
//            logger.error("Failed to save BABEL clazz battle config {}: DUPLICATED OPERATION", targetConf.getId());
//            message = MapMessage.errorMessage();
//        } catch (Exception ex) {
//            logger.error("Failed to save BABEL clazz battle config {}", targetConf.getId(), ex);
//            message = MapMessage.errorMessage();
//        }
//        if (!message.isSuccess()) {
//            Map<String, Object> json = new HashMap<>();
//            json.put("success", false);
//            return JsonUtils.toJson(json);
//        }
//        BabelClazzBattleConfig saveRs = (BabelClazzBattleConfig) message.get("config");
//        logger.info("saved for BabelFloorClazzBattleConfig {},\nreturn {}", JsonUtils.toJsonPretty(targetConf), JsonUtils.toJsonPretty(saveRs));
//        Map<String, Object> json = new HashMap<>();
//        json.put("success", true);
//        json.put("newConf", saveRs);
//        return JsonUtils.toJson(json);
//    }
//
//    /**
//     * ajax列出全部奖励类型
//     *
//     * @return
//     */
//    @RequestMapping(value = "listRewardType.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String listRewardType() {
//        List<Map<String, Object>> collection = new ArrayList<>();
//        for (RewardType rewardType : RewardType.values()) {
//            Map<String, Object> typeObj = new HashMap<>();
//            typeObj.put("processInternal", rewardType.isProcessInternal());
//            typeObj.put("chnName", rewardType.getChnName());
//            typeObj.put("name", rewardType.name());
//            typeObj.put("idRequired", rewardType.isIdRequired());
//            collection.add(typeObj);
//        }
//        return JsonUtils.toJson(collection);
//    }
//
//    /**
//     * ajax列出BOSS战奖励
//     *
//     * @return
//     */
//    @RequestMapping(value = "listBossPrize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String listBossPrize() {
//        return JsonUtils.toJson(babelLoaderClient.loadBabelBossPrizeConf());
//    }
//
//    /**
//     * ajax列出全部奖励类型
//     *
//     * @return
//     */
//    @RequestMapping(value = "saveBossPrizeConf.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String saveBossPrizeConf() {
//        Map<String, Object> prizeConf = JsonUtils.fromJson(getRequestString("prizeConf"));
//
//        List<Map<String, String>> topPrize = (List<Map<String, String>>) prizeConf.get("topPrize");
//        List<BabelStoredPrize> topList = new ArrayList<>();
//        for (Map<String, String> mp : topPrize) {
//            BabelStoredPrize bp = new BabelStoredPrize(0l, 0, RewardType.valueOf(mp.get("rewardType")), mp.get("itemId"), Integer.parseInt(mp.get("count")));
//            topList.add(bp);
//        }
//
//        List<Map<String, String>> allPrize = (List<Map<String, String>>) prizeConf.get("allPrize");
//        List<BossPrizeRangeConf> allList = new ArrayList<>();
//        for (Map<String, String> mp : allPrize) {
//            BossPrizeRangeConf bc = new BossPrizeRangeConf(Integer.parseInt(mp.get("divider").toString())
//                    , new BabelStoredPrize(0l, 0, RewardType.valueOf(mp.get("rewardType")), mp.get("itemId"), Integer.parseInt(mp.get("count"))));
//            allList.add(bc);
//        }
//
//        BabelBossPrizeConf conf = new BabelBossPrizeConf(null, topList, allList);
//
//        MapMessage message;
//        try {
//            message = atomicLockManager.wrapAtomic(babelManagementClient)
//                    .keyPrefix("BABEL_MANAGEMENT:SAVE_BOSS_FIGHT_PRIZE_CONFIG")
//                    .keys("NA")
//                    .proxy()
//                    .saveBossFightPrizeConfig(conf);
//        } catch (CannotAcquireLockException ex) {
//            logger.error("Failed to save BABEL boss fight prize config: DUPLICATED OPERATION");
//            message = MapMessage.errorMessage();
//        } catch (Exception ex) {
//            logger.error("Failed to save BABEL boss fight prize config", ex);
//            message = MapMessage.errorMessage();
//        }
//        conf = (BabelBossPrizeConf) message.get("config");
//        return JsonUtils.toJson(conf);
//    }
//
//    /**
//     * ajax列出全部道具
//     *
//     * @return
//     */
//    @RequestMapping(value = "listItemType.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String listItemType() {
//        List<Map<String, Object>> collection = new ArrayList<>();
//        for (BabelItem item : BabelItem.values()) {
//            Map<String, Object> typeObj = new HashMap<>();
//            typeObj.put("itemId", item.getItemId());
//            typeObj.put("starPrice", item.getStarPrice());
//            typeObj.put("integralPrice", item.getIntegralPrice());
//            typeObj.put("itemDesc", item.getItemDesc());
//            typeObj.put("itemName", item.getItemName());
//            collection.add(typeObj);
//        }
//        return JsonUtils.toJson(collection);
//    }
//
//    /**
//     * ajax列出全部PK道具
//     *
//     * @return
//     */
//    @RequestMapping(value = "listPkItem.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String listPkItem() {
//        MapMessage allEq = pkLoaderClient.getEquipmentConfigs4Select();
//        Map<String, String> selectMp = (Map) allEq.get("selectMap");
//
//        List<Map<String, Object>> collection = new ArrayList<>();
//        for (Map.Entry entry : selectMp.entrySet()) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("itemId", entry.getKey());
//            map.put("itemName", entry.getValue());
//            collection.add(map);
//        }
//        return JsonUtils.toJson(collection);
//    }
//
//    /**
//     * 编辑关卡npc
//     *
//     * @return
//     */
//    @RequestMapping(value = "saveStageNpc.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveStageNpc() {
//        int floor = getRequestInt("floor");
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        if (floor > allFloor.get(allFloor.size() - 1).getFloorNo()) {   // 新楼层
//            return this.newFloor();
//        }
//
//        int stageIndex = getRequestInt("stageIndex");
//        if (0 >= floor) {
//            return MapMessage.errorMessage();
//        }
//        BabelFloor updateFloor = allFloor.get(floor - 1);
//        if (stageIndex >= updateFloor.getStageList().size()) {  // 新关卡
//            return this.newStage();
//        }
//
//        List<StageNpc> npcList = updateFloor.getStageList().get(stageIndex).getNpcList();
//        int npcIndex = getRequestInt("npcIndex");
//        if (npcIndex >= npcList.size()) {   // 新npc
//            return this.newStageNpc();
//        }
//
//        // 编辑npc
//        Map<String, Object> npcInfo = JsonUtils.fromJson(getRequestParameter("npcInfo", ""));
//        BabelNpc npcPrototype = babelLoaderClient.loadNpcs().get(Integer.valueOf(npcInfo.get("id").toString()));
//        if (null == npcPrototype) {
//            MapMessage.errorMessage();
//        }
//
//        StageNpc npc = this.buildStageNpcFromJson(getRequestParameter("npcInfo", ""), floor, stageIndex, npcIndex);
//        npcList.set(npcIndex, npc);
//        MapMessage message = babelManagementClient.saveFloor(updateFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " edit stage npc(floor:" + floor + ",stageIndex" + stageIndex
//                + ",npcIndex:" + npcIndex + ",npcInfo:" + getRequestParameter("npcInfo", ""));
//        return MapMessage.successMessage().add("npc", npc).add("allFloor", allFloor);
//    }
//
//    /**
//     * 编辑npc原型
//     *
//     * @return
//     */
//    @RequestMapping(value = "saveNpc.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveNpc() {
//
//        HttpServletRequest request = getRequest();
//
//        int id = NumberUtils.toInt(request.getParameter("id"), -1);
//        if (-1 == id) {
//            return MapMessage.errorMessage();
//        }
//
//        String name = getRequestParameter("name", "");
//        if (name.isEmpty()) {
//            return MapMessage.errorMessage();
//        }
//
//        AttackType attackType = AttackType.valueOf(getRequestParameter("attackType", ""));
//
//        int baseAp = NumberUtils.toInt(request.getParameter("baseAp"), -1);
//        int baseDp = NumberUtils.toInt(request.getParameter("baseDp"), -1);
//        int advAp = NumberUtils.toInt(request.getParameter("advAp"), -1);
//        int advDp = NumberUtils.toInt(request.getParameter("advDp"), -1);
//        String npcDesc = getRequestString("npcDesc");
//        if (baseAp < 0 || baseDp < 0 || advAp < 0 || advDp < 0) {
//            return MapMessage.errorMessage();
//        }
//
//
//        BabelNpc npc = new BabelNpc();
//        npc.setId(id);
//        npc.setName(name);
//        npc.setAttackType(attackType);
//        npc.setBaseAp(baseAp);
//        npc.setBaseDp(baseDp);
//        npc.setAdvAp(advAp);
//        npc.setAdvDp(advDp);
//        npc.setNpcDesc(npcDesc);
//        MapMessage message = babelManagementClient.saveNpc(npc);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        npc = (BabelNpc) message.get("npc");
//        logger.info(getCurrentAdminUser().getAdminUserName() + " save npc(" + JsonUtils.toJson(npc) + ")");
//
//        Map<Integer, BabelNpc> babelNpcs = babelLoaderClient.loadNpcs();
//        List<BabelNpc> availableNpcs = new ArrayList<BabelNpc>(BabelNpc.availables(babelNpcs).values());
//        int newNpcId = babelNpcs.isEmpty() ? 1 : new TreeSet<Integer>(babelNpcs.keySet()).last() + 1;
//        return MapMessage.successMessage().add("allNpc", availableNpcs).add("newNpcId", newNpcId).add("editNpc", npc);
//    }
//
//    /**
//     * 编辑pet原型
//     *
//     * @return
//     */
//    @RequestMapping(value = "savePet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage savePet() {
//
//        HttpServletRequest request = getRequest();
//
//        int id = NumberUtils.toInt(request.getParameter("id"), -1);
//        if (-1 == id) {
//            MapMessage.errorMessage();
//        }
//
//        String name = getRequestParameter("name", "");
//        if (name.isEmpty()) {
//            return MapMessage.errorMessage();
//        }
//
//        String attackType = getRequestParameter("attackType", "");
//        if (name.isEmpty()) {
//            return MapMessage.errorMessage();
//        }
//
//        String mapNo = getRequestParameter("mapNo", "");
//        if (mapNo.isEmpty()) {
//            return MapMessage.errorMessage();
//        }
//
//        BabelPet pet = new BabelPet();
//        pet.setId(id);
//        pet.setPetName(name);
//        pet.setAttackType(AttackType.valueOf(attackType));
//        pet.setMapNo(Integer.parseInt(mapNo));
//
//        MapMessage message = babelManagementClient.savePet(pet);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        pet = (BabelPet) message.get("pet");
//
//        logger.info(getCurrentAdminUser().getAdminUserName() + " save pet(" + JsonUtils.toJson(pet) + ")");
//
//        Map<Integer, BabelPet> babelPets = babelLoaderClient.loadPets();
//        int newPetId = babelPets.isEmpty() ? 1 : new TreeSet<>(babelPets.keySet()).last() + 1;
//
//        Map<Integer, BabelPet> availableBabelPets = BabelPet.availables(babelPets);
//
//        return MapMessage.successMessage()
//                .add("allPet", new ArrayList<>(availableBabelPets.values()))
//                .add("newPetId", newPetId)
//                .add("editPet", pet);
//    }
//
//    /**
//     * 删除npc原型
//     *
//     * @return
//     */
//    @RequestMapping(value = "deleteNpc.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage deleteNpc() {
//        int npcId = NumberUtils.toInt(getRequest().getParameter("id"));
//        MapMessage message = babelManagementClient.disableNpc(npcId);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " delete npc(" + npcId + ")");
//        return MapMessage.successMessage();
//    }
//
//    /**
//     * 删除pet原型
//     *
//     * @return
//     */
//    @RequestMapping(value = "deletePet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage deletePet() {
//        int petId = NumberUtils.toInt(getRequest().getParameter("id"));
//
//        MapMessage message = babelManagementClient.disablePet(petId);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " delete pet(" + petId + ")");
//        return MapMessage.successMessage();
//    }
//
//    /**
//     * ajax列出全部npc原型
//     *
//     * @return
//     */
//    @RequestMapping(value = "npcList.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String npcList() {
//        Map<Integer, BabelNpc> babelNpcs = babelLoaderClient.loadNpcs();
//        return JsonUtils.toJson(new ArrayList<>(BabelNpc.availables(babelNpcs).values()));
//    }
//
//    /**
//     * ajax列出全部pet原型
//     *
//     * @return
//     */
//    @RequestMapping(value = "petList.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String petList() {
//        return JsonUtils.toJson(new ArrayList<BabelPet>(babelLoaderClient.loadAvailablePets().values()));
//    }
//
//    /**
//     * 新的NPCid
//     *
//     * @return
//     */
//    @RequestMapping(value = "getNewNpcId.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getNewNpcId() {
//        Map<Integer, BabelNpc> babelNpcs = babelLoaderClient.loadNpcs();
//        int newNpcId = babelNpcs.isEmpty() ? 1 : new TreeSet<Integer>(babelNpcs.keySet()).last() + 1;
//        return Integer.toString(newNpcId);
//    }
//
//    /**
//     * 新的petid
//     *
//     * @return
//     */
//    @RequestMapping(value = "getNewPetId.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getNewPetId() {
//        Map<Integer, BabelPet> babelPets = babelLoaderClient.loadPets();
//        int newPetId = babelPets.isEmpty() ? 1 : new TreeSet<Integer>(babelPets.keySet()).last() + 1;
//        return Integer.toString(newPetId);
//    }
//
//    /**
//     * 新建楼层，一并新建关卡和关卡npc
//     *
//     * @return
//     */
//    @RequestMapping(value = "newFloor.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage newFloor() {
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        BabelFloor newFloor = new BabelFloor();
//
//        BabelStage newStage = new BabelStage();
//
//
//        newFloor.setFloorNo(allFloor.get(allFloor.size() - 1).getFloorNo() + 1);
//        StageNpc npc = this.buildStageNpcFromJson(getRequestParameter("npcInfo", ""), newFloor.getFloorNo(), 0, 0);
//        List<StageNpc> npcList = new ArrayList<>();
//        npcList.add(npc);
//        newStage.setNpcList(npcList);
//        newFloor.setFirstEntryReward(new ArrayList<BabelReward>());
//        List<BabelStage> stageList = new ArrayList<>();
//        stageList.add(newStage);
//        newFloor.setStageList(stageList);
//        allFloor.add(newFloor);
//        MapMessage message = babelManagementClient.saveFloor(newFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " new floor firstnpc(floor:" + newFloor.getFloorNo()
//                + ",stageIndex${0},npcIndex:${0},npcInfo:" + getRequestParameter("npcInfo", ""));
//        return MapMessage.successMessage().add("npc", npc).add("allFloor", allFloor);
//    }
//
//    /**
//     * 新建关卡，一并新建关卡npc
//     *
//     * @return
//     */
//    @RequestMapping(value = "newStage.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage newStage() {
//        int floor = getRequestInt("floor");
//
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        BabelFloor updateFloor = allFloor.get(floor - 1);
//        List<BabelStage> stageList = updateFloor.getStageList();
//        BabelStage newStage = new BabelStage();
//        stageList.add(newStage);
//        List<StageNpc> npcList = new ArrayList<>();
//        StageNpc npc = this.buildStageNpcFromJson(getRequestParameter("npcInfo", ""), updateFloor.getFloorNo(), stageList.size() - 1, 0);
//        npcList.add(npc);
//        newStage.setNpcList(npcList);
//
//        MapMessage message = babelManagementClient.saveFloor(updateFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " new stage firstnpc(floor:" + floor + ",stageIndex" + (updateFloor.getStageList().size() - 1)
//                + ",npcIndex:${0},npcInfo:" + getRequestParameter("npcInfo", ""));
//        return MapMessage.successMessage().add("npc", npc).add("allFloor", allFloor);
//    }
//
//    /**
//     * 新建关卡npc
//     *
//     * @return
//     */
//    @RequestMapping(value = "newStageNpc.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage newStageNpc() {
//        int floor = getRequestInt("floor");
//        int stageIndex = getRequestInt("stageIndex");
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        BabelFloor updateFloor = allFloor.get(floor - 1);
//        BabelStage updateStage = updateFloor.getStageList().get(stageIndex);
//        List<StageNpc> npcList = updateStage.getNpcList();
//        StageNpc npc = this.buildStageNpcFromJson(getRequestParameter("npcInfo", ""), floor, stageIndex, npcList.size());
//        npcList.add(npc);
//        MapMessage message = babelManagementClient.saveFloor(updateFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " new stagenpc(floor:" + floor + ",stageIndex" + stageIndex
//                + ",npcIndex:" + (npcList.size() - 1) + ",npcInfo:" + getRequestParameter("npcInfo", ""));
//        return MapMessage.successMessage().add("npc", npc).add("allFloor", allFloor);
//    }
//
//    private StageNpc buildStageNpcFromJson(String json, int floor, int stageIndex, int npcIndex) {
//        Map<String, Object> npcInfo = JsonUtils.fromJson(json);
//        BabelNpc npcPrototype = babelLoaderClient.loadNpcs().get(Integer.valueOf(npcInfo.get("id").toString()));
//        if (null == npcPrototype) {
//            return null;
//        }
//
//        StageNpc npc = new StageNpc(npcPrototype);
//        npc.setNpcType(StageNpcType.valueOf(npcInfo.get("npcType").toString()));
//        npc.setHp(Integer.valueOf(npcInfo.get("hp").toString()));
//        npc.setBaseAp(Integer.valueOf(npcInfo.get("baseAp").toString()));
//        npc.setBaseDp(Integer.valueOf(npcInfo.get("baseDp").toString()));
//        npc.setAdvAp(Integer.valueOf(npcInfo.get("advAp").toString()));
//        npc.setAdvDp(Integer.valueOf(npcInfo.get("advDp").toString()));
//        npc.setNpcDesc(npcInfo.get("npcDesc").toString());
//        List<BabelReward> reward = new ArrayList<>();
//        List<Map<String, Object>> rewardFromReq = (List<Map<String, Object>>) npcInfo.get("reward");
//        for (Map<String, Object> rewardMap : rewardFromReq) {
//            BabelReward rwd = new BabelReward();
////            System.out.println("=================********==============" + rewardMap.get("count"));
//            rwd.setCount((Integer) rewardMap.get("count"));
//            rwd.setItemId((String) rewardMap.get("itemId"));
//            rwd.setKeyRate((Integer) rewardMap.get("keyRate"));
//            rwd.setMaxKeyCount((Integer) rewardMap.get("maxKeyCount"));
//            rwd.setRewardRate((Integer) rewardMap.get("rewardRate"));
//            rwd.setRewardType(RewardType.valueOf((String) rewardMap.get("rewardType")));
//            reward.add(rwd);
//        }
//        npc.setReward(reward);
//
//        npc.setFloor(floor);
//        npc.setStageIndex(stageIndex);
//        npc.setNpcIndex(npcIndex);
//        return npc;
//
//    }
//
//    /**
//     * ajax列出全部楼层全部关卡
//     *
//     * @return
//     */
//    @RequestMapping(value = "stageConf.vpage", method = RequestMethod.GET)
//    public String stageConf() {
//        return "babel/stage_conf";
//    }
//
//    /**
//     * ajax列出全部楼层全部关卡
//     *
//     * @return
//     */
//    @RequestMapping(value = "npcConf.vpage", method = RequestMethod.GET)
//    public String npcConf() {
//        return "babel/npc_conf";
//    }
//
//    /**
//     * 删除关卡npc
//     *
//     * @return
//     */
//    @RequestMapping(value = "deleteStageNpc.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage deleteStageNpc() {
//        int floor = getRequestInt("floor");
//        int stageIndex = getRequestInt("stageIndex");
//        int npcIndex = getRequestInt("npcIndex");
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        BabelFloor updateFloor = allFloor.get(floor - 1);
//        List<StageNpc> npcList = updateFloor.getStageList().get(stageIndex).getNpcList();
//        npcList.remove(npcIndex);
//        MapMessage message = babelManagementClient.saveFloor(updateFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " delete stagenpc(floor:" + floor + ",stageIndex" + stageIndex + ",npcIndex:" + npcIndex + ")");
//        return MapMessage.successMessage().add("allFloor", allFloor);
//    }
//
//    /**
//     * 查询用户通天塔活力消耗记录
//     *
//     * @return
//     */
//    @RequestMapping(value = "queryVitalityConsumeHistory.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage queryVitalityConsumeHistory() {
//        long userId = getRequestLong("userId", 0l);
//        if (userId <= 0l) {
//            return MapMessage.errorMessage();
//        }
//        Collection<BabelVitalityChangeLog> logs = babelManagementClient.loadBabelVitalityChangeLogs(userId, 0, 30);
//        List<Map<String, Object>> hs = new ArrayList<>();
//        for (BabelVitalityChangeLog log : logs) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("time", DateUtils.dateToString(log.getChangeTime()));
//            map.put("count", log.getChangeCount());
//            map.put("before", log.getBefore());
//            map.put("after", log.getAfter());
//
//            hs.add(map);
//        }
//        return MapMessage.successMessage().add("history", hs);
//    }
//
//    /**
//     * 查活力消耗页面
//     *
//     * @return
//     */
//    @RequestMapping(value = "vitalityConsumeHistory.vpage", method = RequestMethod.GET)
//    public String vitalityConsumeHistory() {
//        return "babel/vitality_history";
//    }
//
//    /**
//     * 删除关卡
//     *
//     * @return
//     */
//    @RequestMapping(value = "deleteStage.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage deleteStage() {
//        int floor = getRequestInt("floor");
//        int stageIndex = getRequestInt("stageIndex");
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        BabelFloor updateFloor = allFloor.get(floor - 1);
//        updateFloor.getStageList().remove(stageIndex);
//        MapMessage message = babelManagementClient.saveFloor(updateFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " delete stage(floor:" + floor + ",stageIndex" + stageIndex + ")");
//        return MapMessage.successMessage().add("allFloor", allFloor);
//    }
//
//    /**
//     * 删除楼层
//     *
//     * @return
//     */
//    @RequestMapping(value = "deleteFloor.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage deleteFloor() {
//        MapMessage message;
//        try {
//            message = atomicLockManager.wrapAtomic(babelManagementClient)
//                    .keyPrefix("BABEL_MANAGEMENT:DELETE_HIGHEST_FLOOR")
//                    .keys("NA")
//                    .proxy()
//                    .deleteHighestFloor();
//        } catch (CannotAcquireLockException ex) {
//            logger.error("Failed to delete highest floor: DUPLICATED OPERATION");
//            message = MapMessage.errorMessage();
//        } catch (Exception ex) {
//            logger.error("Failed to delete highest floor", ex);
//            message = MapMessage.errorMessage();
//        }
//        if (!message.isSuccess()) {
//            return message;
//        }
//        int floorId = (Integer) message.get("floorId");
//        logger.info(getCurrentAdminUser().getAdminUserName() + " delete stage(floor:" + floorId + ")");
//        return MapMessage.successMessage().add("allFloor", babelManagementClient.loadFloors());
//    }
//
//    /**
//     * 编辑开启楼层奖励
//     *
//     * @return
//     */
//    @RequestMapping(value = "saveFloorReward.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveFloorReward() {
//        int floor = NumberUtils.toInt(getRequest().getParameter("floor"), -1);
//        if (floor < 0) {
//            return MapMessage.errorMessage();
//        }
//        List<Map> rewardMapList = JsonUtils.fromJsonToList(getRequestParameter("rewardList", ""), Map.class);
//        List<BabelReward> rewardLst = new ArrayList<>();
//        for (Map<String, String> rwdMp : rewardMapList) {
//            BabelReward rwd = new BabelReward();
//            rwd.setCount(NumberUtils.toInt(rwdMp.get("count"), -1));
//            rwd.setItemId(rwdMp.get("itemId"));
//            rwd.setRewardType(RewardType.valueOf(rwdMp.get("rewardType")));
//            if (rwd.getCount() <= 0) {
//                continue;
//            }
//            rewardLst.add(rwd);
//        }
//        List<BabelFloor> allFloor = new ArrayList<>(babelManagementClient.loadFloors());
//        BabelFloor babelFloor = allFloor.get(floor - 1);
//        babelFloor.setFirstEntryReward(rewardLst);
//        MapMessage message = babelManagementClient.saveFloor(babelFloor);
//        if (!message.isSuccess()) {
//            return message;
//        }
//        logger.info(getCurrentAdminUser().getAdminUserName() + " edit floor reward(" + getRequestParameter("rewardList", "") + ")");
//        return MapMessage.successMessage().add("allFloor", allFloor);
//    }
//
//    @RequestMapping(value = "saveTestNpc.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public String saveTestNpc() {
//        for (int i = 0; i < 40; i++) {
//            BabelNpc npc = new BabelNpc();
//            npc.setId(i + 1);
//            npc.setAttackType(AttackType.values()[i % 4]);
//            npc.setName(npc.getAttackType().toString() + i);
//            babelManagementClient.saveNpc(npc);
//        }
//        return null;
//    }
//
//    @RequestMapping(value = "saveTestFloor.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public String saveTestFloor() {
//        BabelFloor floor = new BabelFloor();
//        List<BabelStage> stageList = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            BabelStage stage = new BabelStage();
//            List<StageNpc> npcList = new ArrayList<>();
//            for (int j = 0; j < 5; j++) {
//                StageNpc npc = new StageNpc(babelLoaderClient.loadNpcs().get(i * 5 + (j + 1)));
//                npc.setNpcType(j < 3 ? StageNpcType.NORMAL : j < 4 ? StageNpcType.EXPERT : StageNpcType.BOSS);
//                npc.setBaseAp(j < 4 ? 10 : 15);
//                npc.setBaseDp(j < 4 ? 10 : 15);
//                npc.setAdvAp(j < 3 ? 0 : j < 4 ? 20 : 25);
//                npc.setAdvDp(j < 4 ? 15 : 37);
//                npc.setHp(j < 4 ? 100 : 150);
//                List<BabelReward> beatReward = new ArrayList<>();
//                BabelReward rwd = new BabelReward();
//                rwd.setCount(1);
//                rwd.setKeyRate(5);
//                rwd.setRewardRate(100);
//                rwd.setRewardType(RewardType.BABEL_STAR);
//                beatReward.add(rwd);
//                npc.setReward(beatReward);
//                npcList.add(npc);
//            }
//            stage.setNpcList(npcList);
//            stageList.add(stage);
//        }
//        floor.setStageList(stageList);
//        List<BabelReward> rewardList = new ArrayList<>();
//        BabelReward reward = new BabelReward();
//        reward.setCount(3);
//        reward.setKeyRate(5);
//        reward.setRewardType(RewardType.BABEL_STAR);
//        reward.setRewardRate(100);
//        rewardList.add(reward);
//        floor.setFirstEntryReward(rewardList);
//        return String.valueOf(babelManagementClient.saveFloor(floor));
//    }


}
