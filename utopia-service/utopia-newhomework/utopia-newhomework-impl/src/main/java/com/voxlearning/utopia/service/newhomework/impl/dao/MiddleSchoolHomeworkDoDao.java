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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomeworkDo;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class MiddleSchoolHomeworkDoDao extends AlpsDynamicMongoDao<MiddleSchoolHomeworkDo, String> {

    @Override
    protected void calculateCacheDimensions(MiddleSchoolHomeworkDo source, Collection<String> dimensions) {
        dimensions.add(MiddleSchoolHomeworkDo.ck_id(source.getId()));
    }

    @Override
    protected String calculateDatabase(String template, MiddleSchoolHomeworkDo entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, MiddleSchoolHomeworkDo entity) {
        MiddleSchoolHomeworkDo.ID id = entity.parseID();
        return StringUtils.formatMessage(template, id.getDay());
    }

    public Long getFinishedStudentCount(MiddleSchoolHomework homework) {
        Criteria criteria = Criteria.where("homework_id").is(new ObjectId(homework.getId())).and("status").is(5);

        String day = DayRange.newInstance(homework.getCreateTime().getTime()).toString();

        MiddleSchoolHomeworkDo.ID mock = new MiddleSchoolHomeworkDo.ID();
        mock.setDay(day);
        mock.setSubject(Subject.ENGLISH);
        mock.setHid(homework.getId());
        mock.setUserId("0");

        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mock.toString());
        return executeCount(createMongoConnection(mongoNamespace), Query.query(criteria));
    }
}
