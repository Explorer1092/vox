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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.entity.SelfStudyConfigWrapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangpeng on 16/8/5.
 * 自学工具tab配置loader,没有用client那一套。因为功能不复杂用处不多。
 */
@ServiceVersion(version = "2016.08.12")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface SelfStudyConfigLoader extends IPingable {

    public enum RequestSource {
        H5,
        APP
    }

    /**
     * 返回结构
     * {
     * "success":true,
     * "result": List<SelfStudyConfigWrapper>
     * }
     *
     * @param clazzLevel 年级 没有孩子 传0。有孩子,孩子没班级,也传0
     * @return mapmessage
     */
    @CacheMethod(
            type = MapMessage.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 300)
    )
    public MapMessage loadSelfStudyShowConfigByClazzLevel(@CacheParameter("self_study_config_cl") Integer clazzLevel);



    public List<SelfStudyConfigWrapper> loadSelfStudyConfig4LearnGrowth(Integer clazzLevel, String version);
}
