/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.utopia.service.business.api.entity.RSRegionUnitWeakPointStat;
import com.voxlearning.utopia.service.business.impl.support.AbstractRSRegionStatDao;
import com.voxlearning.utopia.service.business.impl.support.mode1.RSRegionUnitWeakPointStatDao1;
import com.voxlearning.utopia.service.business.impl.support.mode2.RSRegionUnitWeakPointStatDao2;

import javax.inject.Named;

/**
 * Created by Changyuan on 2015/1/15.
 */
@Named
public class RSRegionUnitWeakPointStatDao extends AbstractRSRegionStatDao<RSRegionUnitWeakPointStat,
        RSRegionUnitWeakPointStatDao1, RSRegionUnitWeakPointStatDao2> {
}
