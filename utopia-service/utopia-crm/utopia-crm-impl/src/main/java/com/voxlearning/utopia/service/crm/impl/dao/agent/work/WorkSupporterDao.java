package com.voxlearning.utopia.service.crm.impl.dao.agent.work;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkSupporter;

import javax.inject.Named;

/**
 * WorkSupporterDao
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@CacheBean(type = WorkSupporter.class)
public class WorkSupporterDao extends StaticCacheDimensionDocumentMongoDao<WorkSupporter, String> {

}
