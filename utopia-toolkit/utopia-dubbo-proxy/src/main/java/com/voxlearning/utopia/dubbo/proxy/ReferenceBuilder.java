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

package com.voxlearning.utopia.dubbo.proxy;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.remote.dubbo.config.RuntimeApplicationConfig;
import com.voxlearning.alps.remote.dubbo.config.RuntimeRegistryConfig;
import com.voxlearning.com.alibaba.dubbo.config.ReferenceConfig;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@ThreadSafe
public class ReferenceBuilder {

    private static final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private static final Map<ID, ReferenceConfig<GenericService>> buffer = new HashMap<>();

    public static ReferenceConfig<GenericService> build(String group,
                                                        String generic,
                                                        String service,
                                                        String version) {

        ID id = new ID(group, generic, service, version);
        ReferenceConfig<GenericService> buffered = locker.withinReadLock(() -> buffer.get(id));
        if (buffered != null) {
            return buffered;
        }
        return locker.withinWriteLock(() -> {
            ReferenceConfig<GenericService> rc = buffer.get(id);
            if (rc != null) {
                return rc;
            }

            rc = new ReferenceConfig<>();
            rc.setApplication(RuntimeApplicationConfig.load());
            rc.setRegistry(RuntimeRegistryConfig.load());
            rc.setInterface(service);
            rc.setGroup(group);
            rc.setVersion(version);
            rc.setGeneric(generic);

            buffer.put(id, rc);

            return rc;
        });
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ID {
        private String group;
        private String generic;
        private String service;
        private String version;
    }
}
