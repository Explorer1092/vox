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

package com.voxlearning.utopia.admin.controller.nekketsu;

import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author sadi.wan
 * @since 2014-6-20
 */
@Controller
@RequestMapping(value = "appmanager/nekketsu/parkourConfig")
public class NekketsuParkourConfigController extends CrmAbstractController {

//    @Inject private NekketsuParkourClient nekketsuParkourClient;
//    @Inject private PkLoaderClient pkLoaderClient;

//    @RequestMapping(value = "getInit.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public String getInit() {
//        Map<Gender, Map<String, String>> pkFashionList = pkLoaderClient.getProductInitials(SourceCategory.PARKOUR);
//
//        Map<String, String> selectMp = pkFashionList.get(Gender.MALE);
//        List<Map<String, String>> pkItemMaleCollection = new ArrayList<>();
//        for (Map.Entry<String, String> entry : selectMp.entrySet()) {
//            Map<String, String> map = new HashMap<>();
//            map.put("itemId", entry.getKey());
//            map.put("itemName", entry.getValue());
//            pkItemMaleCollection.add(map);
//        }
//
//        selectMp = pkFashionList.get(Gender.FEMALE);
//        List<Map<String, String>> pkItemFemaleCollection = new ArrayList<>();
//        for (Map.Entry<String, String> entry : selectMp.entrySet()) {
//            Map<String, String> map = new HashMap<>();
//            map.put("itemId", entry.getKey());
//            map.put("itemName", entry.getValue());
//            pkItemFemaleCollection.add(map);
//        }
//
//        Map<String, Object> json = new HashMap<>();
//        json.put("pkItemMale", pkItemMaleCollection);
//        json.put("pkItemFemale", pkItemFemaleCollection);
//        json.put("levelSpeed", nekketsuParkourClient.loadLevelSpeed());
//        json.put("stageList", nekketsuParkourClient.listAllStage());
//        json.put("monthPrizeConf", nekketsuParkourClient.loadLoginPrizeConf());
//        json.put("shopItem", nekketsuParkourClient.loadShopItemCrm());
//        return JsonUtils.toJson(json);
//    }

//    /**
//     * ajax列出全部楼层全部关卡
//     * @return
//     */
//    @RequestMapping(value = "parkouConf.vpage", method = RequestMethod.GET)
//    public String stageConf() {
//        return "nekketsu/parkour_conf";
//    }

//    /**
//     * ajax列出全部楼层全部关卡
//     * @return
//     */
//    @RequestMapping(value = "saveAll.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveAll() {
//
//        HttpServletRequest request = getRequest();
//
//        List<LevelSpeedInfo> levelConf = JsonUtils.fromJsonToList(request.getParameter("levelSpeed"), LevelSpeedInfo.class);
//        List<ParkourStage> stageConf = JsonUtils.fromJsonToList(request.getParameter("stageConf"), ParkourStage.class);
//        LoginPrizeMonthConf thisMonthConf = new LoginPrizeMonthConf();
//
//        thisMonthConf.setPrizeItemId(toListString(JsonUtils.fromJsonToMap(request.getParameter("thisMonthPrize"), Gender.class, List.class)));
//        LoginPrizeMonthConf nextMonthConf = new LoginPrizeMonthConf();
//        nextMonthConf.setPrizeItemId(toListString(JsonUtils.fromJsonToMap(request.getParameter("nextMonthPrize"), Gender.class, List.class)));
//        List<ParkourShopItem> shopItemList = JsonUtils.fromJsonToList(request.getParameter("shopItemList"), ParkourShopItem.class);
//
//        Map<Gender, Map<String, String>> pkFashionList = pkLoaderClient.getProductInitials(SourceCategory.PARKOUR);
//
//        Map selectMpMale = pkFashionList.get(Gender.MALE);
//        Map selectMpFemale = pkFashionList.get(Gender.FEMALE);
//
//        for (ParkourShopItem it : shopItemList) {
//            if (selectMpMale.containsKey(it.getItemId())) {
//                it.setGender(Gender.MALE);
//            } else if (selectMpFemale.containsKey(it.getItemId())) {
//                it.setGender(Gender.FEMALE);
//            } else {
//                it.setGender(Gender.NOT_SURE);
//            }
//        }
//        nekketsuParkourClient.saveLevelSpeed(levelConf);
//        nekketsuParkourClient.saveThisMonth(thisMonthConf);
//        nekketsuParkourClient.saveNextMonth(nextMonthConf);
//        nekketsuParkourClient.replaceAll(stageConf);
//        nekketsuParkourClient.saveShopItem(shopItemList);
//        return MapMessage.successMessage();
//    }

//    private Map<Gender, List<String>> toListString(Map<Gender, List> from) {
//        Map<Gender, List<String>> map = new LinkedHashMap<>();
//        for (Map.Entry<Gender, List> entry : from.entrySet()) {
//            List<String> value = (entry.getValue() == null) ? null : (List<String>)entry.getValue();
//            map.put(entry.getKey(), value);
//        }
//        return map;
//    }
}
