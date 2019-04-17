package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentModifyDictSchoolApplyLoader
 *
 * @author song.wang
 * @date 2016/12/28
 */
@ServiceVersion(version = "2016.12.28")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentModifyDictSchoolApplyLoader extends IPingable {

    /**
     * 根据学校Id 查找申请记录
     */
    @Idempotent
    List<AgentModifyDictSchoolApply> findBySchoolId(Long schoolId);

    /**
     * 根据工作流Id 查找申请记录
     */
    @Idempotent
    AgentModifyDictSchoolApply findByWorkflowId(Long workflowId);

    /**
     * 根据 是否更新到字典表 和 status == APPROVED 来确定申请
     */
    @Idempotent
    List<AgentModifyDictSchoolApply> findByStatusAndResolved(ApplyStatus status, Boolean resolved);

    /**
     * 根据ID 批量查询更新申请
     */
    @Idempotent
    Map<Long, AgentModifyDictSchoolApply> findByIds(List<Long> applyIds);

}
