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

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionLib;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author Maofeng Lu
 * @since 14-10-24 下午3:39
 */
@Named
public class SmartClazzQuestionLibDao extends StaticMongoDao<SmartClazzQuestionLib, String> {
    @Override
    protected void calculateCacheDimensions(SmartClazzQuestionLib source, Collection<String> dimensions) {
    }
}
