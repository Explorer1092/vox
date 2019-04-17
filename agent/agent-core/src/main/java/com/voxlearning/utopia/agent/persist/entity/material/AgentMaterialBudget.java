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
 * 市场物料预算
 *
 * @author chunlin.yu
 * @create 2018-02-06 11:26
 **/

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_material_budget")
public class AgentMaterialBudget implements CacheDimensionDocument {
    private static final long serialVersionUID = 5305673162427729309L;

    @DocumentId
    private String id;

    /**
     * 预算
     */
    private Double budget;

    /**
     * 余额
     */
    private Double balance;

    /**
     * 预算类型，1：城市预算，2：物料预算
     */
    private Integer budgetType;

    //-----------城市预算部分
    /**
     * 分区ID
     */
    private Long groupId;

    private String groupName;

    /**
     * 城市编码
     */
    private Integer regionCode;

    private String regionName;

    /**
     * 费用月份
     */
    private Integer month;

    //-----------城市预算部分结束


    //-----------物料费用部分
    private Long userId;
    /**
     * 学期
     */
    private String semester;
    //------------物料费用部分结束


    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey(id)};
    }
}
