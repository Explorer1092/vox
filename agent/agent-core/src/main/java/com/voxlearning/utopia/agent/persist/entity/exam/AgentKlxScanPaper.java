package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 快乐学扫描的试卷，需要大数据灌入数据
 * @author chunlin.yu
 * @create 2018-03-15 17:24
 **/

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_KLX_SCAN_PAPER")
@UtopiaCacheExpiration
@UtopiaCacheRevision("201803114")
public class AgentKlxScanPaper  extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -8855120861016832423L;

    @UtopiaSqlColumn
    private Long schoolId;

    @UtopiaSqlColumn
    private String paperId;

    @UtopiaSqlColumn
    private Integer paperGrade;

    @UtopiaSqlColumn
    private String paperSubject;

    @UtopiaSqlColumn
    private String paperTitle;

    @UtopiaSqlColumn
    private Long paperCreatorId;

    @UtopiaSqlColumn
    private String paperCreatorName;

    @UtopiaSqlColumn
    private Date lastScanTime;
}
