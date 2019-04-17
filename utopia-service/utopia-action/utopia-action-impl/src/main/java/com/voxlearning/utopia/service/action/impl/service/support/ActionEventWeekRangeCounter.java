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

package com.voxlearning.utopia.service.action.impl.service.support;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.impl.support.ActionCacheSystem;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 用于控制每周的计数
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@Named
public class ActionEventWeekRangeCounter implements InitializingBean {

    @Inject
    private ActionCacheSystem actionCacheSystem;
    private Cache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        cache = actionCacheSystem.CBS.storage;
    }

    public long increase(ActionEvent event) {
        String week = WeekRange.newInstance(event.getTimestamp()).toString();
        String key = CacheKeyGenerator.generateCacheKey("ActionEventWeekRangeCounter",
                new String[]{"userId", "type", "week"},
                new Object[]{event.getUserId(), event.getType(), week});
        int expiration = DateUtils.getCurrentToWeekEndSecond() + 86400;
        return SafeConverter.toLong(cache.incr(key, 1, 1, expiration));
    }
    public long increase(ActionEvent event, Subject subject) {
        String week = WeekRange.newInstance(event.getTimestamp()).toString();
        String key = CacheKeyGenerator.generateCacheKey("ActionEventWeekRangeCounter",
                new String[]{"userId", "type", "week", "subject"},
                new Object[]{event.getUserId(), event.getType(), week, subject.name()});
        int expiration = DateUtils.getCurrentToWeekEndSecond() + 86400;
        return SafeConverter.toLong(cache.incr(key, 1, 1, expiration));
    }
}
