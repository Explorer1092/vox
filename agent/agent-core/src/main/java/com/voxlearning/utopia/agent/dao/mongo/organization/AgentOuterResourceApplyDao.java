package com.voxlearning.utopia.agent.dao.mongo.organization;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceApply;

import javax.inject.Named;


@Named
@CacheBean(type = AgentOuterResourceApply.class)
public class AgentOuterResourceApplyDao extends StaticCacheDimensionDocumentMongoDao<AgentOuterResourceApply, String> {

}