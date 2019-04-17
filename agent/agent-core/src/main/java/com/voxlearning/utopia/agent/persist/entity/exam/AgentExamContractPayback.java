package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * 大考合同回款信息
 *
 * @author deliang.che
 * @data 2018-05-03
 **/

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_EXAM_CONTRACT_PAYBACK")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180503")
public class AgentExamContractPayback extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = 908396740563679885L;

    @UtopiaSqlColumn
    private Integer period; //期数

    @UtopiaSqlColumn
    private Long contractId;//合同ID

    @UtopiaSqlColumn
    private Date paybackDate;//回款日期

    @UtopiaSqlColumn
    private Long operatorId;//操作人ID

    @UtopiaSqlColumn
    private Integer paybackAmount;//回款金额
}
