package com.voxlearning.utopia.agent.persist.entity.material;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 物料预算余额变更记录
 *
 * @author chunlin.yu
 * @create 2018-02-06 18:35
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_material_balance_change_record")
public class AgentMaterialBalanceChangeRecord implements CacheDimensionDocument {
    private static final long serialVersionUID = 8589153564669271586L;
    @DocumentId
    private String id;
    private Integer operateType;//1:人工修改，2：购买物料
    private Long operatorId;
    private String operatorName;
    private Double preCash; // 调整前的金额
    private Double afterCash;// 调整前的金额
    private Double quantity;// 调整的金额
    private String comment; // 备注
    private Integer recordType; //记录类型；1：预算调整，2：余额调整， 3:部门未分配费用调整
    //对应于AgentMaterialBudget的主键
    private String agentMaterialBudgetId;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
