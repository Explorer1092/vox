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
 * 市场物料费用
 *
 * @author deliang.che
 * @since  2018/7/17
 **/

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_material_cost")
public class AgentMaterialCost implements CacheDimensionDocument {

    private static final long serialVersionUID = 8173961227898641550L;
    @DocumentId
    private String id;
    private String schoolTerm;//学期
    private Integer materialType;//物料类型（1：部门物料预算 2：人员物料余额）
    private Long groupId;   //部门ID
    private Long userId;    //用户ID
    private Double budget;  //物料预算
    private Double balance; //物料余额
    private Double undistributedCost; //未分配费用


    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {

        return new String[]{
                newCacheKey(id)
//                newCacheKey("gid",this.groupId)
        };
    }
}
