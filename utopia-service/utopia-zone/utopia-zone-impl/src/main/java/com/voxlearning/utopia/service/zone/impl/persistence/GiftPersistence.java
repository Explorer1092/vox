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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.zone.api.entity.Gift;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * {@link Gift} persistence implementation.
 * Legacy ASK : GIFT_VERSION
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since Sep 3, 2013
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.GiftPersistence")
public class GiftPersistence extends NoCacheStaticMySQLPersistence<Gift, Long> {

    @Inject private GiftVersion giftVersion;

    @Override
    public void insert(Gift document) {
        super.insert(document);
        giftVersion.increment();
    }
}
