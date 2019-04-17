package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentCityLevel;

import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * AgentCityLevelDao
 *
 * @author song.wang
 * @date 2018/2/8
 */
@Named
@CacheBean(type = AgentCityLevel.class)
public class AgentCityLevelDao extends StaticCacheDimensionDocumentMongoDao<AgentCityLevel, Integer> {

}
