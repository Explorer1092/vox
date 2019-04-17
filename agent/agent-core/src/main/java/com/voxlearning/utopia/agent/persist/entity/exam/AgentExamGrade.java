package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 大考封装
 * @author chunlin.yu
 * @create 2018-04-18 20:54
 **/

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_EXAM_GRADE")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180418")
public class AgentExamGrade extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 8091089135825581151L;

    @UtopiaSqlColumn
    private Integer grade;

    @UtopiaSqlColumn
    private Long agentExamSchoolId;


}
