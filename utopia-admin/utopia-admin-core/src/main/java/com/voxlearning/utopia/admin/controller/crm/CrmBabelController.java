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

package com.voxlearning.utopia.admin.controller.crm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Longlong Yu
 * @since 下午8:39,13-6-25.
 */
@Controller
@RequestMapping("/crm/babel")
public class CrmBabelController extends CrmAbstractController {

//    @Inject private BabelVitalityServiceClient babelVitalityServiceClient;
//
//    /**
//     * ***********************查询相关*****************************************************************
//     */
//    @RequestMapping(value = "babeldetail.vpage", method = RequestMethod.GET)
//    public String babeldetail(@RequestParam("userId") Long userId, Model model) {
//        BabelRole role = babelLoaderClient.loadRole(userId);
//        BabelBag bag = babelLoaderClient.loadBag(userId);
//        StringBuilder bagInfo = new StringBuilder();
//        for (RoleItem item : bag.getItemList()) {
//            bagInfo.append(BabelItem.getByItemId(item.getItemId()).getItemName()).append(item.getCount()).append("个; ");
//        }
//        model.addAttribute("role", role);
//        model.addAttribute("bag", bagInfo.toString());
//        model.addAttribute("play", babelManagementClient.loadBabelPlayLogs(userId, 0, 60));
//        model.addAttribute("vitalityHistory", babelManagementClient.loadBabelVitalityChangeLogs(userId, 0, 60));
//        model.addAttribute("star", babelManagementClient.loadBabelStarChangeLogs(userId, 0, 600));
//        model.addAttribute("userId", userId);
//        model.addAttribute("vitality", babelVitalityServiceClient.getCurrentBalance(userId).getBalance());
//        User student = userLoaderClient.loadUser(userId, UserType.STUDENT);
//        if (null != student && null != student.getProfile() && StringUtils.isNotEmpty(student.getProfile().getRealname())) {
//            model.addAttribute("userName", student.getProfile().getRealname());
//        }
//
//        BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
//        try {
//            model.addAttribute("BabelReward", wrapper.getStaticModels().get(BabelReward.class.getName()));
//        } catch (Exception e) {
//        }
//
//        model.addAttribute("itemList", BabelItem.values());
//        return "crm/babel/babeldetail";
//    }
//
//    @RequestMapping(value = "modVitality.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage modVitality(@RequestParam("userId") Long userId) {
//        String mod = getRequestParameter("count", "");
//        int count = NumberUtils.toInt(mod, 0);
//        String description = "CRM修改通天塔活力" + getCurrentAdminUser().getAdminUserName();
//        BabelVitalityResponse resp = babelVitalityServiceClient.increaseVitality(userId, count, description);
//        return MapMessage.successMessage().setSuccess(resp.isSuccess()).add("newBalance", resp.getBalance());
//    }
//
//    @RequestMapping(value = "modStar.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage modStar(@RequestParam("userId") Long userId) {
//        String mod = getRequestParameter("count", "");
//        int count = NumberUtils.toInt(mod, 0);
//        BabelRole role = babelLoaderClient.loadRole(userId);
//        return babelManagementClient.increaseStar(role, count, BabelStarChange.CRM_CHANGE, "CRM修改通天塔星星,操作者" + getCurrentAdminUser().getAdminUserName());
//    }
//
//    @RequestMapping(value = "modItem.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage modItem(@RequestParam("userId") Long userId) {
//        System.out.println(getRequestParameter("changeList", ""));
//        List<Map> changeMap = JsonUtils.fromJsonToList(getRequestParameter("changeList", ""), Map.class);
//        Map<Integer, Integer> daoMap = new HashMap<>();
//        for (Map mp : changeMap) {
//            Integer itemId = NumberUtils.toInt(mp.get("itemId").toString());
//            Integer count = NumberUtils.toInt(mp.get("count").toString());
//            if (count != 0) {
//                daoMap.put(itemId, count);
//            }
//        }
//        BabelBag bag = babelManagementClient.changeItemCount(userId, daoMap);
//        StringBuilder bagInfo = new StringBuilder();
//        if (null != bag) {
//            for (RoleItem item : bag.getItemList()) {
//                bagInfo.append(BabelItem.getByItemId(item.getItemId()).getItemName()).append(item.getCount()).append("个; ");
//            }
//        } else {
//            return MapMessage.errorMessage().add("bag", "操作失败，请刷新页面重试");
//        }
//
//        return MapMessage.successMessage().add("bag", bagInfo.toString());
//    }
}