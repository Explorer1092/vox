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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;

import java.util.concurrent.TimeUnit;

/**
 * 用户广告信息服务接口
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-09-27
 */
@ServiceVersion(version = "20180927")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
public interface UserAdvertisementInfoService extends IPingable {

    /**
     * 新增用户广告信息
     *
     * @param studentAdvertisementInfo
     * @return
     */
    MapMessage insert(StudentAdvertisementInfo studentAdvertisementInfo);

}