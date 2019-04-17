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

package com.voxlearning.utopia.service.piclisten.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.piclisten.api.ParentShareTextReadService;
import com.voxlearning.utopia.service.piclisten.impl.dao.ParentShareTextReadDao;
import com.voxlearning.utopia.service.vendor.api.entity.ParentShareTextRead;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by jiangpeng on 16/7/18.
 */
@Named
@Service(interfaceClass = ParentShareTextReadService.class)
@ExposeService(interfaceClass = ParentShareTextReadService.class)
public class ParentShareTextReadServiceImpl implements ParentShareTextReadService {


    @Inject
    private ParentShareTextReadDao parentShareTextReadDao;

    @Override
    public ParentShareTextRead loadByParentIdFileMd5(Long parentId, String md5) {
        return parentShareTextReadDao.loadByParentIdFileMd5(parentId, md5);
    }

    @Override
    public ParentShareTextRead save(ParentShareTextRead parentShareTextRead) {

        return parentShareTextReadDao.loadIfPresentElseInsertByPidMd5(parentShareTextRead);
    }
}
