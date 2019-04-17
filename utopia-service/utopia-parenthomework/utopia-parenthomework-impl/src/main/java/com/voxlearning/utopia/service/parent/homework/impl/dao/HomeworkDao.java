package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = Homework.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class HomeworkDao extends AlpsDynamicMongoDao<Homework, String> {
    @Override
    protected void calculateCacheDimensions(Homework document, Collection<String> dimensions) {
    }

    @Override
    protected String calculateDatabase(String template, Homework document) {
        return StringUtils.formatMessage(template, HomeworkUtil.yyyyMM(document.getId()));
    }

    @Override
    protected String calculateCollection(String template, Homework document) {
        return template;
    }
}
