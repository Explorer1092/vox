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

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@ServiceVersion(version = "20160209")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
@CyclopsMonitor("utopia")
public interface NewAccomplishmentLoader extends IPingable {
    /**
     * 根据作业的location加载对应的accomplishment
     *
     * @param location 作业Location
     * @return accomplishment
     */
    @Idempotent
    default NewAccomplishment loadNewAccomplishment(NewHomework.Location location) {
        if (location == null) {
            return null;
        }
        if (location.getId() == null || location.getSubject() == null || location.getCreateTime() == 0) {
            return null;
        }
        NewAccomplishment.ID id = NewAccomplishment.ID.build(location.getCreateTime(),
                location.getSubject(), location.getId());
        NewAccomplishment loaded = __loadNewAccomplishment(id.toString());
        if (loaded == null) {
            loaded = new NewAccomplishment();
            loaded.setId(id.toString());
            loaded.setDetails(new LinkedHashMap<>());
        }
        return loaded;
    }

    @Idempotent
    @CacheMethod(type = NewAccomplishment.class, writeCache = false)
    NewAccomplishment __loadNewAccomplishment(@CacheParameter String id);

    @Idempotent
    default Map<String, NewAccomplishment> loadNewAccomplishmentByHomework(Collection<NewHomework.Location> locations) {
        if (CollectionUtils.isEmpty(locations)) {
            return Collections.emptyMap();
        }
        Set<String> ids = locations
                .stream()
                .filter(location -> (location.getId() != null && location.getSubject() != null && location.getCreateTime() != 0))
                .map(location -> NewAccomplishment.ID.build(location.getCreateTime(), location.getSubject(), location.getId()).toString())
                .collect(Collectors.toSet());

        return loadNewAccomplishments(ids).values()
                .stream()
                .collect(Collectors.toMap(
                        o -> o.parseID().getHid(),
                        Function.identity()
                ));
    }

    @Idempotent
    @CacheMethod(type = NewAccomplishment.class, writeCache = false)
    Map<String, NewAccomplishment> loadNewAccomplishments(@CacheParameter(multiple = true) Collection<String> ids);
}
