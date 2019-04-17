package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.agent.constants.AgentLargeExamContractType;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 大考合同
 *
 * @author chunlin.yu
 * @create 2018-03-13 10:57
 **/

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_EXAM_CONTRACT")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180824")
public class AgentExamContract extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 5218264639110296055L;

    @UtopiaSqlColumn
    private Long schoolId;

    @UtopiaSqlColumn
    private AgentLargeExamContractType contractType;

    /**
     * 合同金额
     */
    @UtopiaSqlColumn
    private Integer contractAmount;

    /**
     * 服务开始时间
     */
    @UtopiaSqlColumn
    private Date beginDate;

    /**
     * 服务结束时间
     */
    @UtopiaSqlColumn
    private Date endDate;

    /**
     * 签约人ID
     */
    @UtopiaSqlColumn
    private Long contractorId;

    /**
     * 签约日期
     */
    @UtopiaSqlColumn
    private Date contractDate;

    @UtopiaSqlColumn
    private Integer hardwareCost;   //硬件成本

    @UtopiaSqlColumn
    private Integer machinesNum;    //机器数量

    @UtopiaSqlColumn
    private String machinesType;    //机器型号

    @UtopiaSqlColumn
    private String remark;          //备注

    @UtopiaSqlColumn
    private Integer thirdPartyProductCost;//第三方产品成本

    @UtopiaSqlColumn
    private String serviceRange;    //服务范围
}
