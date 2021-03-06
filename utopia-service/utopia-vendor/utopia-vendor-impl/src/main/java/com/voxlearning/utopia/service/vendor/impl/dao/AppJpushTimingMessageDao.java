package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsDateRangeMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author xinxin
 * @since 27/7/2016
 */
@Named
public class AppJpushTimingMessageDao extends AlpsDateRangeMongoDao<AppJpushTimingMessage> {
    @Override
    protected String calculateDatabase(String template, AppJpushTimingMessage document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, AppJpushTimingMessage document) {
        if (document == null || document.getId() == null) {
            return null;
        }
        String[] ids = document.getId().split("_");
        if (ids.length != 3) {
            return null;
        }

        return StringUtils.formatMessage(template, ids[1]);
    }

    @Override
    protected void calculateCacheDimensions(AppJpushTimingMessage document, Collection<String> dimensions) {

    }

    public Page<AppJpushTimingMessage> getTimingMessage(Long sendTime, Pageable pageable) {
        Criteria sendTimeCriteria = Criteria.where("sendTime").is(sendTime);

        Query query = Query.query(sendTimeCriteria)
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize());
        List<AppJpushTimingMessage> messages = query(query);

        Query countQuery = Query.query(sendTimeCriteria);
        long count = count(countQuery);

        return new PageImpl<>(messages, pageable, count);
    }

}
