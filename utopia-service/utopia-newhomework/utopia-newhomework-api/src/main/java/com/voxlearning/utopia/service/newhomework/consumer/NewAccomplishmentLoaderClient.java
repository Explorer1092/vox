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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.api.NewAccomplishmentLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class NewAccomplishmentLoaderClient implements NewAccomplishmentLoader {

    @ImportService(interfaceClass = NewAccomplishmentLoader.class)
    private NewAccomplishmentLoader remoteReference;

    @Override
    public NewAccomplishment __loadNewAccomplishment(String id) {
        return remoteReference.__loadNewAccomplishment(id);
    }

    @Override
    public Map<String, NewAccomplishment> loadNewAccomplishments(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadNewAccomplishments(ids);
    }
}
