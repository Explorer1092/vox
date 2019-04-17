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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.business.api.ResearchStaffManagement;
import lombok.Getter;

@Spring
public class ResearchStaffManagementClient {
    @Getter
    @ImportService(interfaceClass = ResearchStaffManagement.class)
    private ResearchStaffManagement remoteReference;
}
