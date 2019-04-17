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

package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/toolkit/nekketsu/parkour")
@NoArgsConstructor
public class ToolKitNekketsuParkourController extends ToolKitAbstractController {

    @RequestMapping(value = "setVitality.vpage", method = RequestMethod.POST)
    String setVitality(@RequestParam(value = "userIds", required = false) String userIds,
                       @RequestParam(value = "vitality", required = false) Integer vitality) {
        String[] ids = StringUtils.split(userIds, ",");
        vitality = vitality > 5 ? 5 : vitality < 0 ? 0 : vitality;
        long now = System.currentTimeMillis();
        for (String userId : ids) {
            Long uid = conversionService.convert(userId, Long.class);
            CacheSystem.CBS.getCache("persistence").set("VOX_NEKKETSU_PARKOUR_PLAYER_VITALITY:" + uid, 86400, new KeyValuePair<>(vitality, now));
        }
        getAlertMessageManager().addMessageSuccess("添加活力成功");
        return "toolkit/toolkit";
    }
}
