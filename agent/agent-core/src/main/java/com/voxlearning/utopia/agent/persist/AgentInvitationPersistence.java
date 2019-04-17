package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.AgentInvitation;

import javax.inject.Named;
import java.util.Collection;

/**
 * 邀请函Persistence
 *
 * @author deliang.che
 * @date 2018-04-13
 */
@Named
public class AgentInvitationPersistence extends AlpsStaticJdbcDao<AgentInvitation, Long> {
    @Override
    protected void calculateCacheDimensions(AgentInvitation source, Collection<String> dimensions) {

    }

}
