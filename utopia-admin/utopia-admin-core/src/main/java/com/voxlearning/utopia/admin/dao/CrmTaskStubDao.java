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

package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmTaskStub;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/10/21
 */
@Named
public class CrmTaskStubDao extends StaticMongoDao<CrmTaskStub, String> {

    @Override
    protected void calculateCacheDimensions(CrmTaskStub source, Collection<String> dimensions) {
    }

    public List<CrmTaskStub> findByTaskId(String taskId) {
        Filter filter = filterBuilder.where("taskId").is(taskId);
        Sort sort = new Sort(Sort.Direction.DESC, "actionTime");
        return find(filter, sort);
    }

    private List<CrmTaskStub> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }
}
