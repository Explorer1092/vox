package com.voxlearning.utopia.agent.dao.mongo.material;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.material.AgentOrderCityCost;

import javax.inject.Named;

/**
 * @author chunlin.yu
 * @create 2018-02-22 14:50
 **/
@Named
@CacheBean(type = AgentOrderCityCost.class)
public class AgentOrderCityCostDao extends StaticCacheDimensionDocumentMongoDao<AgentOrderCityCost, Long> {

}
