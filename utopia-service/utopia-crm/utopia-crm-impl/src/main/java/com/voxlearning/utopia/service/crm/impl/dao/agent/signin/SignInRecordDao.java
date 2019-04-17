package com.voxlearning.utopia.service.crm.impl.dao.agent.signin;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;

import javax.inject.Named;

/**
 * SignInRecordDao
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@CacheBean(type = SignInRecord.class)
public class SignInRecordDao extends StaticCacheDimensionDocumentMongoDao<SignInRecord, String> {
}
