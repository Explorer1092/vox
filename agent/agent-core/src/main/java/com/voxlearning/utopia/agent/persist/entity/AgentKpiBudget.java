package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 考核指标预算
 *
 * @author song.wang
 * @date 2018/2/11
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_kpi_budget")
public class AgentKpiBudget implements CacheDimensionDocument {

    public static final Integer GROUP_OR_USER_GROUP = 1;
    public static final Integer GROUP_OR_USER_USER = 2;

    @DocumentId
    private String id;

    private Integer month;                           // 月份
    private Integer groupOrUser;                     // 部门，用户   1：部门  2：个人

    private Long groupId;                            // 部门ID
    private String groupName;                        // 部门名称
    private AgentGroupRoleType groupRoleType;        // 部门角色
    private Long parentGroupId;                      // 上级部门ID

    private Long userId;                             // 用户ID  groupOrUser=2时有值
    private String userName;                         // 用户名  groupOrUser=2时有值

    private AgentKpiType kpiType;                    // 考核指标
    private Integer budget;                          // 相应指标的预算


    private Boolean confirmed;                       // 是否已确认

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
//                newCacheKey(new String[]{"gid", "month"}, new Object[]{this.groupId, this.month}),
                newCacheKey(new String[]{"month"}, new Object[]{this.month})
        };
    }
}
