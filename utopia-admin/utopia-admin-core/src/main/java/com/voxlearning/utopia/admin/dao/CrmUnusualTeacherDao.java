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
import com.voxlearning.utopia.admin.entity.CrmUnusualTeacher;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Yang Hailong
 * @since 2015/7/3
 */
@Named
public class CrmUnusualTeacherDao extends StaticMongoDao<CrmUnusualTeacher, String> {

    @Override
    protected void calculateCacheDimensions(CrmUnusualTeacher source, Collection<String> dimensions) {
    }

    public List<CrmUnusualTeacher> findCityUnusalTeachers(Collection<Integer> cityCodes) {
        Filter filter = filterBuilder.where("cityCode").in(cityCodes);
        Find find = Find.find(filter);
        return __find_OTF(find);
    }

    public List<CrmUnusualTeacher> findByCreateTime(long startTime, long endTime) {
        Filter filter = filterBuilder.where("createTime").gte(startTime).lte(endTime);
        return __find_OTF(Find.find(filter));
    }
}
