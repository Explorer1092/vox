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
import java.util.Map;

/**
 * 城市预算花费
 *
 * @author chunlin.yu
 * @create 2018-02-22 14:41
 **/

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_order_city_cost")
public class AgentOrderCityCost implements CacheDimensionDocument {
    private static final long serialVersionUID = 4721492937863627312L;

    /**
     * 订单ID作为主键
     */
    @DocumentId
    private Long orderId;

    private Integer regionCode;

    /**
     * 花费的预算集合，Key：预算AgentMaterialBudget的ID，Value：花费的金额
     */
    private Map<String,Double> costs;


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
