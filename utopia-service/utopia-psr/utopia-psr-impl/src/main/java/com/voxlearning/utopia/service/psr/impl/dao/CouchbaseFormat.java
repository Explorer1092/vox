/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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


import com.voxlearning.alps.lang.util.SpringContainerSupport;

abstract public class CouchbaseFormat extends SpringContainerSupport {

    abstract public boolean addCouchbaseData(String strKey, String strValue);

    abstract public boolean setCouchbaseData(String strKey, String strValue);

    abstract public String getCouchbaseDataByKey(String strKey);

}
