package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;

import javax.inject.Named;
import java.util.Date;

/**
 * Dao of UserPicBookHistory
 * Created by ganhaitian on 2018/1/22.
 */
@Named
@CacheBean(type = UserPicBookResult.class)
public class UserPicBookResultDao extends DynamicCacheDimensionDocumentMongoDao<UserPicBookResult,String> {

    @Override
    protected String calculateDatabase(String template, UserPicBookResult document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserPicBookResult document) {
        Date now = new Date();
        String dateMonth = DateUtils.dateToString(now,"yyyyMM");

        return StringUtils.formatMessage(template,dateMonth);
    }
}
