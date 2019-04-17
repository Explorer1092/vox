package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 大考试卷
 *
 * @author chunlin.yu
 * @create 2018-03-14 19:12
 **/
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_EXAM_PAPER")
@UtopiaCacheExpiration
@UtopiaCacheRevision("201803114")
public class AgentExamPaper extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = -5126884079999252787L;
    /**
     * 科目大考ID
     */
    @UtopiaSqlColumn
    private Long agentExamSubjectId;

    /**
     * 试卷ID
     */
    @UtopiaSqlColumn
    private String paperId;

    /**
     * 试卷名字
     */
    @UtopiaSqlColumn
    private String paperName;

    /**
     * 试卷科目
     */
    @UtopiaSqlColumn
    private Subject subject;
}
