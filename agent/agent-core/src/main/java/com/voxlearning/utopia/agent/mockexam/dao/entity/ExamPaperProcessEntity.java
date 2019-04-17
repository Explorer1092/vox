package com.voxlearning.utopia.agent.mockexam.dao.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * 试卷流程状态
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Data
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_MEXAM_PAPER_PROCESS")
public class ExamPaperProcessEntity implements Serializable {

    /**
     * 主键
     */
    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC)
    private Long id;

    /**
     * 试卷流程id
     */
    @UtopiaSqlColumn(name = "PROCESS_ID")
    private String processId;

    /**
     * 计划id
     */
    @UtopiaSqlColumn(name = "PLAN_ID")
    private Long planId;

    /**
     * 流程状态
     */
    @UtopiaSqlColumn(name = "STATUS")
    private String status;

    /**
     * 拒绝理由
     */
    @UtopiaSqlColumn(name = "REJECT_REASON")
    private String rejectReason;

    /**
     * 创建时间
     */
    @UtopiaSqlColumn(name = "CREATE_DATETIME")
    @DocumentCreateTimestamp
    private Date createDatetime;

    /**
     * 最后一次修改时间
     */
    @UtopiaSqlColumn(name = "UPDATE_DATETIME")
    @DocumentUpdateTimestamp
    private Date updateDatetime;

    /**
     * 逻辑删除
     */
    @UtopiaSqlColumn(name = "DISABLE")
    private String disable;
}
