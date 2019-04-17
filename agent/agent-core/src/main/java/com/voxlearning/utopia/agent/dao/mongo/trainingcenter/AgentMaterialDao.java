package com.voxlearning.utopia.agent.dao.mongo.trainingcenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentMaterial;

import javax.inject.Named;

@Named
@CacheBean(type = AgentMaterial.class)
public class AgentMaterialDao extends StaticCacheDimensionDocumentMongoDao<AgentMaterial, String> {

}
