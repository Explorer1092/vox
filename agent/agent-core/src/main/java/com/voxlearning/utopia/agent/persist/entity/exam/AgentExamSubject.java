package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import lombok.Getter;
import lombok.Setter;

/**
 * 大考科目
 *
 * @author chunlin.yu
 * @create 2018-03-14 19:07
 **/

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_EXAM_SUBJET")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180418")
public class AgentExamSubject extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = -251369931232069902L;

    @UtopiaSqlColumn
    private Subject subject;

    @UtopiaSqlColumn
    private Long agentExamGradeId;
}
