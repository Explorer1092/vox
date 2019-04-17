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

package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.business.api.UserAdvertisementInfoService;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.impl.dao.StudentAdvertisementInfoDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 用户广告信息服务实现
 *
 * @author Wenlong Meng
 * @version 1.0.0
 */
@Named
@ExposeService(interfaceClass = UserAdvertisementInfoService.class)
public class UserAdvertisementInfoServiceImpl implements UserAdvertisementInfoService {

    @Inject
    private StudentAdvertisementInfoDao studentAdvertisementInfoDao;;

    /**
     * 新增用户广告信息
     *
     * @param sai
     * @return
     */
    @Override
    public MapMessage insert(StudentAdvertisementInfo sai) {
        if(sai == null){
            return MapMessage.errorMessage().setInfo("studentAdvertisementInfo is null");
        }
        //check参数
        if(ObjectUtils.anyBlank(sai.getSlotId(),
                sai.getUserId(),
                sai.getShowEndTime(),
                sai.getShowStartTime())){
            return MapMessage.errorMessage().setInfo("参数 soltId,userId,showStartTime,showEndTime 不能为空");
        }
        studentAdvertisementInfoDao.insert(sai);
        return MapMessage.successMessage();
    }

}
