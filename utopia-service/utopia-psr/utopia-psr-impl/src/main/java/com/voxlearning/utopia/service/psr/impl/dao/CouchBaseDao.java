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

package com.voxlearning.utopia.service.psr.impl.dao;

import com.voxlearning.alps.annotation.common.ObjectCacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.impl.support.PsrCacheSystem;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/5/13
 * Time: 19:51
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
@Named
public class CouchBaseDao extends CouchbaseFormat {

    @Inject
    private PsrCacheSystem psrCacheSystem;

    public boolean addCouchbaseData(String strKey, String strValue) {
        return addCouchbaseData(strKey, strValue, 0);
    }

    public boolean addCouchbaseData(String strKey, String strValue, int expirationInSeconds) {
        if (StringUtils.isEmpty(strKey) || StringUtils.isEmpty(strValue)) {
            return false;
        }
        String formatKey = formatCouchbaseKey(strKey);
        return Boolean.TRUE.equals(psrCacheSystem.CBS.psr.add(ObjectCacheKeyGenerator.generate(formatKey), expirationInSeconds, strValue));
    }

    public boolean setCouchbaseData(String strKey, String strValue) {
        return setCouchbaseData(strKey, strValue, 0);
    }

    public boolean setCouchbaseData(String strKey, String strValue, int expirationInSeconds) {
        if (StringUtils.isEmpty(strKey) || StringUtils.isEmpty(strValue)) {
            return false;
        }
        String formatKey = formatCouchbaseKey(strKey);
        return Boolean.TRUE.equals(psrCacheSystem.CBS.psr.set(ObjectCacheKeyGenerator.generate(formatKey), expirationInSeconds, strValue));
    }

    public String getCouchbaseDataByKey(String strKey) {
        if (StringUtils.isEmpty(strKey)) return null;
        String formatKey = formatCouchbaseKey(strKey);
        return psrCacheSystem.CBS.psr.load(ObjectCacheKeyGenerator.generate(formatKey));
    }


    private static String formatCouchbaseKey(String strKey) {
        if (StringUtils.isEmpty(strKey)) return null;
        if (strKey.contains(" ")) strKey = strKey.replace(" ", "_");
        return strKey;
    }

}
