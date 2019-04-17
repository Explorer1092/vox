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

package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;

import javax.inject.Named;

/**
 * 陈经纶中学数字化校园平台
 *
 * @author Yuechen.Wang
 * @since 2017-07-17 10:26
 */
@Named
public class CJLSsoConnector extends AbstractSsoConnector {

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        if (null == connectionInfo || StringUtils.isBlank(token)) {
            return MapMessage.errorMessage();
        }
        return MapMessage.errorMessage();
    }

}
