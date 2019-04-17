package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmProductFeedbackRecord;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
@Named
@CacheBean(type = CrmProductFeedbackRecord.class)
public class CrmProductFeedbackRecordDao extends AlpsStaticMongoDao<CrmProductFeedbackRecord, String> {
    @Override
    protected void calculateCacheDimensions(CrmProductFeedbackRecord document, Collection<String> dimensions) {

    }
}
