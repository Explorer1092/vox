package com.voxlearning.utopia.service.newexam.impl.dao;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.utopia.service.newexam.api.entity.JournalNewExamProcessResult;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by tanguohong on 2016/3/24.
 */
@Named
public class JournalNewExamProcessResultDao extends DynamicMongoDao<JournalNewExamProcessResult, String> {


    @Override
    protected String calculateDatabase(String template, JournalNewExamProcessResult entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, JournalNewExamProcessResult entity) {
        RangeableId rangeableId = RangeableId.parse(entity.getId());
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.D).toString());
    }

    @Override
    protected void calculateCacheDimensions(JournalNewExamProcessResult source, Collection<String> dimensions) {

    }
}
