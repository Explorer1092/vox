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

package com.voxlearning.utopia.service.zone.provider.module.management;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;
import com.voxlearning.utopia.service.zone.data.VersionedGiftData;
import com.voxlearning.utopia.service.zone.data.VersionedUserMoodData;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventStatistics;
import com.voxlearning.utopia.service.zone.impl.service.UserMoodServiceImpl;
import com.voxlearning.utopia.service.zone.impl.service.ZoneGiftServiceImpl;
import com.voxlearning.utopia.service.zone.impl.service.ZoneProductServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/zone-provider")
final public class ZoneController {

    public static final ZoneController INSTANCE = new ZoneController();

    @RequestMapping(value = "index.do", method = RequestMethod.GET)
    public String index(Model model) {
        VersionedUserMoodData userMoodBufferData = ApplicationContextScanner.getInstance()
                .getBean(UserMoodServiceImpl.class)
                .getUserMoodBuffer()
                .dump();
        model.addAttribute("userMoodBufferData", userMoodBufferData);

        VersionedGiftData giftBufferData = ApplicationContextScanner.getInstance()
                .getBean(ZoneGiftServiceImpl.class)
                .getGiftBuffer()
                .dump();
        model.addAttribute("giftBufferData", giftBufferData);

        VersionedClazzZoneProductData clazzZoneProductBufferData = ApplicationContextScanner.getInstance()
                .getBean(ZoneProductServiceImpl.class)
                .getClazzZoneProductBuffer()
                .dump();
        model.addAttribute("clazzZoneProductBufferData", clazzZoneProductBufferData);

        model.addAttribute("zoneEventTypes", Arrays.asList(ZoneEventType.values()));
        model.addAttribute("zoneEventStatistics", ZoneEventStatistics.INSTANCE);

        return "zone-provider/index";
    }
}
