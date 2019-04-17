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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ClazzZoneService;
import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import com.voxlearning.utopia.service.zone.support.SendGiftCreator;
import lombok.Getter;

@Deprecated
public class ClazzZoneServiceClient {

    @Getter
    @ImportService(interfaceClass = ClazzZoneService.class)
    private ClazzZoneService clazzZoneService;

    public SendGiftCreator createGift(GiftHistoryType type) {
        return new SendGiftCreator(clazzZoneService, type);
    }

}
