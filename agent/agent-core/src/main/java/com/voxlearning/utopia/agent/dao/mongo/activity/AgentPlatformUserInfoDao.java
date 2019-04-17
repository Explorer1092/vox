package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.platform.AgentPlatformUserInfo;

import javax.inject.Named;

@Named
@CacheBean(type =  AgentPlatformUserInfo.class)
public class AgentPlatformUserInfoDao extends StaticCacheDimensionDocumentMongoDao<AgentPlatformUserInfo, Long> {

}
