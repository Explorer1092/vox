/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.toolkit;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/toolkit/babel")
@NoArgsConstructor
public class ToolKitBabelController extends ToolKitAbstractController {

//    @Inject private BabelVitalityServiceClient babelVitalityServiceClient;
//
//    @RequestMapping(value = "setVitality.vpage", method = RequestMethod.POST)
//    String setVitality(@RequestParam(value = "userIds", required = false) String userIds,
//                       @RequestParam(value = "vitality", required = false) Integer vitality) {
//        if (StringUtils.isEmpty(userIds) || vitality == null) {
//            getAlertMessageManager().addMessageError("参数不全！添加活力失败！");
//            return "toolkit/toolkit";
//        }
//        String[] ids = StringUtils.split(userIds, ",");
//        if (vitality > 0) {
//            vitality = vitality > 5 ? 5 : vitality;
//        }
//
//        for (String userId : ids) {
//            Long uid = conversionService.convert(userId, Long.class);
//            if (vitality > 0) {
//                String description = "CRM增加通天塔活力,操作者" + getCurrentAdminUser().getAdminUserName();
//                babelVitalityServiceClient.increaseVitality(uid, vitality, description);
//            } else {
//                int nowBalance = babelVitalityServiceClient.getCurrentBalance(uid).getBalance();
//                if (-vitality > nowBalance) {
//                    vitality = -nowBalance;
//                }
//                String description = "CRM减少通天塔活力,操作者" + getCurrentAdminUser().getAdminUserName();
//                babelVitalityServiceClient.decreaseVitality(uid, -vitality, description);
//            }
//        }
//        getAlertMessageManager().addMessageSuccess("添加活力成功");
//        return "toolkit/toolkit";
//    }
}
