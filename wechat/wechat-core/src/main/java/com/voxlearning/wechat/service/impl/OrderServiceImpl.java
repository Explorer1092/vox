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

package com.voxlearning.wechat.service.impl;

import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.util.RegionGrayUtils;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.wechat.service.OrderService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 10/26/15
 */
@Named
public class OrderServiceImpl implements OrderService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private GlobalTagServiceClient globalTagServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public boolean canBuyAfentiExam(Long studentId) {
        if (null == studentId) {
            return false;
        }

        Set<String> blackUsers = globalTagServiceClient.getGlobalTagBuffer()
                .findByName(GlobalTagName.AfentiBlackListUsers.name())
                .stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        if (blackUsers.contains(studentId.toString())) {
            return false;
        }

        String grayTagName = OrderProductServiceType.AfentiExam.name() + RegionConstants.TAG_GRAY_REGION_SUFFIX;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        RaikouRegionBufferDelegator buffer = new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer());
        if (!studentDetail.isInPaymentBlackListRegion() && studentDetail.getClazzLevelAsInteger() != null
                && studentDetail.getClazzLevelAsInteger() >= 3 && studentDetail.getClazzLevelAsInteger() <= 6
                && RegionGrayUtils.checkRegionGrayStatus(studentDetail.getStudentSchoolRegionCode(), grayTagName, buffer)) {
            return true;
        }
        return false;
    }

}
