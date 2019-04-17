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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.wechat.api.entities.WechatRedPackHistory;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by xiaopeng.yang on 2015/6/5.
 */
@Named
public class WechatRedPackHistoryPersistence extends AlpsStaticJdbcDao<WechatRedPackHistory, Long> {

    @Override
    protected void calculateCacheDimensions(WechatRedPackHistory document, Collection<String> dimensions) {
    }
}
