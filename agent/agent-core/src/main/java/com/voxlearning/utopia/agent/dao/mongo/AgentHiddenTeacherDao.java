package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentHiddenTeacher;

import javax.inject.Named;

/**
 * AgentHiddenTeacherDao
 *
 * @author song.wang
 * @date 2018/1/11
 */
@Named
@CacheBean(type = AgentHiddenTeacher.class)
public class AgentHiddenTeacherDao extends StaticCacheDimensionDocumentMongoDao<AgentHiddenTeacher, Long> {
}
