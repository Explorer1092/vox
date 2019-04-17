package com.voxlearning.utopia.service.newhomework.impl.dao;


import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author guoqiang.li
 * @version 0.1
 * @since 2016/3/1
 */
@Named
@UtopiaCacheSupport(TotalAssignmentRecord.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class TotalAssignmentRecordDao extends StaticMongoDao<TotalAssignmentRecord, String> {
    @Override
    protected void calculateCacheDimensions(TotalAssignmentRecord source, Collection<String> dimensions) {
        dimensions.add(TotalAssignmentRecord.ck_id(source.getId()));
    }

    public void updateTotalAssignmentRecord(Subject subject,
                                            Integer clazzGroupSize,
                                            Map<String, Integer> questionMap,
                                            Set<String> packageSet,
                                            Set<String> paperSet) {
        if (MapUtils.isNotEmpty(questionMap)) {
            questionMap.forEach((id, times) -> update(subject, HomeworkContentType.QUESTION, id, times * clazzGroupSize));
        }
        if (CollectionUtils.isNotEmpty(packageSet)) {
            packageSet.forEach(id -> update(subject, HomeworkContentType.PACKAGE, id, clazzGroupSize));
        }
        if (CollectionUtils.isNotEmpty(paperSet)) {
            paperSet.forEach(id -> update(subject, HomeworkContentType.PAPER, id, clazzGroupSize));
        }
    }

    private void update(Subject subject, HomeworkContentType homeworkContentType, String contentId, Integer incTimes) {
        String id = "{}-{}-{}";
        id = StringUtils.formatMessage(id, subject, homeworkContentType, contentId);
        Update update = updateBuilder.build();
        update.set("contentType", homeworkContentType);
        update.set("subject", subject);
        update.inc("assignTimes", incTimes);
        if (!update.toBsonDocument().isEmpty()) {
            TotalAssignmentRecord inst = __upsert_OTF(id, update);
            if (inst != null) {
                String key = TotalAssignmentRecord.ck_id(id);
                getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                        currentValue -> inst);
            }
        }
    }
}
