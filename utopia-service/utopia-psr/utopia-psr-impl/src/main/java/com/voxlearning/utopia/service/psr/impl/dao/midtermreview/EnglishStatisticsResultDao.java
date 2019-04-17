package com.voxlearning.utopia.service.psr.impl.dao.midtermreview;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.midtermreview.EnglishStatisticsResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Created by Administrator on 2016/10/10.
 */
@Named
@UtopiaCacheSupport(EnglishStatisticsResult.class)
public class EnglishStatisticsResultDao extends AlpsStaticMongoDao<EnglishStatisticsResult,String> {

    @Override
    protected void calculateCacheDimensions(EnglishStatisticsResult source, Collection<String> dimensions){
    }

    public List<EnglishStatisticsResult> getEnglishStatisticsResultByGroupAndBook(String bookId, Integer groupId) {
        if (StringUtils.isEmpty(bookId) || groupId == null || groupId <= 0)
            return Collections.emptyList();

        Criteria criteria = Criteria.where("book_id").is(bookId).and("group_id").is(groupId);
        return query(Query.query(criteria));
    }

}
