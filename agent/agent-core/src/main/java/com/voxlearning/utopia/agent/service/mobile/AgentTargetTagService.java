package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.constants.AgentTag;
import com.voxlearning.utopia.agent.constants.AgentTargetType;
import com.voxlearning.utopia.agent.dao.mongo.AgentTargetTagDao;
import com.voxlearning.utopia.agent.persist.entity.AgentTargetTag;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentTargetTagService
 *
 * @author song.wang
 * @date 2017/6/2
 */
@Named
public class AgentTargetTagService extends AbstractAgentService {

    @Inject
    private AgentTargetTagDao agentTargetTagDao;


    private List<AgentTag> loadTags(Long targetId, AgentTargetType targetType){
        if(targetId == null || targetId < 1 || targetType == null){
            return Collections.emptyList();
        }
        AgentTargetTag agentTargetTag = agentTargetTagDao.loadByTarget(targetId, targetType);
        if(agentTargetTag == null || CollectionUtils.isEmpty(agentTargetTag.getTags())){
            return Collections.emptyList();
        }
        return agentTargetTag.getTags();
    }

    private boolean saveTags(Long targetId, AgentTargetType targetType, List<AgentTag> tags){
        if(targetId == null || targetId < 1 || targetType == null){
            return false;
        }
        AgentTargetTag agentTargetTag = agentTargetTagDao.loadByTarget(targetId, targetType);
        if(CollectionUtils.isEmpty(tags)){// 标签列表为空，相当于清除掉所有的标签
            if(agentTargetTag != null){
                return agentTargetTagDao.remove(agentTargetTag.getId());
            }
            return true;
        }
        if(agentTargetTag == null){
            agentTargetTag = new AgentTargetTag();
            agentTargetTag.setTargetId(targetId);
            agentTargetTag.setTargetType(targetType);
        }
        agentTargetTag.setTags(tags);
        agentTargetTagDao.upsert(agentTargetTag);
        return true;
    }

    public List<AgentTag> loadTeacherTags(Long teacherId){
        return loadTags(teacherId, AgentTargetType.TEACHER);
    }

    public boolean saveTeacherTags(Long teacherId, List<AgentTag> tags){
        return saveTags(teacherId, AgentTargetType.TEACHER, tags);
    }

    public Map<Long, AgentTargetTag> loadTeacherTargetTagMap(Collection<Long> teacherIds){
        if(CollectionUtils.isEmpty(teacherIds)){
            return Collections.emptyMap();
        }
        return agentTargetTagDao.loadTargetTags(teacherIds, AgentTargetType.TEACHER);
    }

    public Map<Long, List<AgentTag>> loadTeacherAgentTag(Collection<Long> teacherIds) {
        return loadTeacherTargetTagMap(teacherIds).values().stream().collect(Collectors.toMap(AgentTargetTag::getTargetId, this::getAgentTargetOfTage));
    }

    private List<AgentTag> getAgentTargetOfTage(AgentTargetTag agentTargetTag) {
        if (agentTargetTag == null) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(agentTargetTag.getTags())) {
            return Collections.emptyList();
        }
        return agentTargetTag.getTags();
    }

}
