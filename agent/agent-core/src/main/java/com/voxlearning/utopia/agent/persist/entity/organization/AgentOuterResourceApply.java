/**
 * Author:   xianlong.zhang
 * Date:     2018/12/13 16:30
 * Description: 上层资源扩展属性
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.organization;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_outer_resource_apply")
public class AgentOuterResourceApply implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private Long resourceId;          //资源 AgentOuterResource id  或 AgentResearchers id
    private Long applyUserId;         //申请人id
    private String applyUserName;     //申请人姓名


    private Long managerId;          //处理人id
    private String managerName;      //处理人姓名

    private Integer result;          //处理结果 0 待处理 1 已通过  2 已驳回
    private String opinions;         //处理意见

    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    private Date auditTime;       //审核时间

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
//                newCacheKey("resourceId",this.resourceId)
        };
    }
}
