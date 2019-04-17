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
import com.voxlearning.utopia.service.zone.api.*;
import com.voxlearning.utopia.service.zone.api.plot.PlotActivityService;
import lombok.Getter;

public class ClassCircleServiceClient {

    @Getter
    @ImportService(interfaceClass = ClassCircleService.class)
    private ClassCircleService classCircleService;
    @Getter
    @ImportService(interfaceClass = ClazzActivityService.class)
    private ClazzActivityService clazzActivityService;
    @Getter
    @ImportService(interfaceClass = ClassCircleBossService.class)
    private ClassCircleBossService classCircleBossService;
    @Getter
    @ImportService(interfaceClass = ClassCirclePlotService.class)
    private ClassCirclePlotService classCirclePlotService;
    @Getter
    @ImportService(interfaceClass = BrainActivityService.class)
    private BrainActivityService brainActivityService;
    @Getter
    @ImportService(interfaceClass = PlotActivityService.class)
    private PlotActivityService plotActivityService;
    @Getter
    @ImportService(interfaceClass = ClassCircleGivingService.class)
    private ClassCircleGivingService classCircleGivingService;
    @Getter
    @ImportService(interfaceClass = ClazzZoneCommonService.class)
    private ClazzZoneCommonService clazzZoneCommonService;
    @Getter
    @ImportService(interfaceClass = ClassCricleParentService.class)
    private ClassCricleParentService classCricleParentService;

}
