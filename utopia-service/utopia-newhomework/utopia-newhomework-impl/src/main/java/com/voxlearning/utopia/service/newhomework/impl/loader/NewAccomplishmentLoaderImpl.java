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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.newhomework.api.NewAccomplishmentLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewAccomplishmentDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@Named
@Service(interfaceClass = NewAccomplishmentLoader.class)
@ExposeService(interfaceClass = NewAccomplishmentLoader.class)
public class NewAccomplishmentLoaderImpl implements NewAccomplishmentLoader {

    @Inject private NewAccomplishmentDao newAccomplishmentDao;

    @Override
    public NewAccomplishment __loadNewAccomplishment(String id) {
        return newAccomplishmentDao.load(id);
    }

    @Override
    public Map<String, NewAccomplishment> loadNewAccomplishments(Collection<String> ids) {
        return newAccomplishmentDao.loads(ids);
    }
}
