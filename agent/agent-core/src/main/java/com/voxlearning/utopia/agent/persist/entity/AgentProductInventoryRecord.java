package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * AgentProductInventoryRecord
 *
 * @author song.wang
 * @date 2016/11/18
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_product_inventory_record")
public class AgentProductInventoryRecord implements Serializable {

    @DocumentId
    private String id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Integer preQuantity; // 商品库存变更前的数量
    private Integer afterQuantity;// 商品库存变更前后数量
    private Integer quantityChange;// 商品库存变更数量
    private String comment; // 备注
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_pid(Long productId) {
        return CacheKeyGenerator.generateCacheKey(AgentProductInventoryRecord.class, "pid", productId);
    }

}
