package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AgentModifyDictSchoolApplyService
 *
 * @author song.wang
 * @date 2016/12/28
 */
@ServiceVersion(version = "2016.12.28")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentModifyDictSchoolApplyService extends IPingable {

    /**
     * 添加调整字典表申请
     * @param agentModifyDictSchoolApply apply
     * @return long
     */
    Long addApply(AgentModifyDictSchoolApply agentModifyDictSchoolApply);

    /**
     * 更新申请状态
     * @param id id
     * @param status status
     * @return boolean
     */
    Boolean updateStatus(Long id, ApplyStatus status);

    /**
     * 设置工作流ID
     * @param id id
     * @param workflowId workflowId
     * @return boolean
     */
    Boolean updateWorkflowId(Long id, Long workflowId);

    /**
     * 设置申请已经被处理
     * @param applyIds applyIds
     * @return List<Long>
     */
    List<Long> updateApplyResolvedByIds(Collection<Long> applyIds);

    /**
     * 设置申请已经被处理
     * @param applyId applyId
     * @return boolean
     */
    Boolean updateApplyResolved(Long applyId);
}
