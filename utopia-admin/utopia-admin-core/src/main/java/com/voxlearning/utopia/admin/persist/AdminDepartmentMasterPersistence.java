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

package com.voxlearning.utopia.admin.persist;

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.admin.persist.entity.AdminDepartmentMaster;

import javax.inject.Named;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-9
 * Time: 下午5:14
 * To change this template use File | Settings | File Templates.
 */
@Named
@Deprecated
public class AdminDepartmentMasterPersistence extends AbstractEntityPersistence<Long, AdminDepartmentMaster> {

    public List<String> getDepartmentListForWrite(String userName) {
        return withSelectFromTable("DEPARTMENT_NAME", "WHERE USER_NAME=? AND RIGHT_WRITE=1").useParamsArgs(userName).queryColumnValues();
    }

    public List<String> getDepartmentListForRead(String userName) {
        return withSelectFromTable("DEPARTMENT_NAME", "WHERE USER_NAME=? AND RIGHT_READ=1").useParamsArgs(userName).queryColumnValues();
    }

    public List<String> getDepartmentListForDelete(String userName) {
        return withSelectFromTable("DEPARTMENT_NAME", "WHERE USER_NAME=? AND RIGHT_DELETE=1").useParamsArgs(userName).queryColumnValues();
    }

    public List<AdminDepartmentMaster> getDepartmentMasters() {
        return withAllFromTable().queryAll();
    }

}
