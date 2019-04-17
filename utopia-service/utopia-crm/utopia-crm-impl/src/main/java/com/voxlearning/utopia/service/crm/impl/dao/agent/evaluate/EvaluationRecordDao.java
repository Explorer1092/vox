package com.voxlearning.utopia.service.crm.impl.dao.agent.evaluate;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;

import javax.inject.Named;

/**
 *
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@CacheBean(type = EvaluationRecord.class)
public class EvaluationRecordDao extends StaticCacheDimensionDocumentMongoDao<EvaluationRecord, String> {

}
