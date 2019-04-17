/**
 * Author:   xianlong.zhang
 * Date:     2018/12/13 16:18
 * Description: 上层资源
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.organization;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_OUTER_RESOURCE")
public class AgentOuterResource implements Serializable {
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID")
    private Long id;
    @DocumentField private String name;
    @DocumentField private Integer gender;           //性别  1 男 2 女
    @DocumentField private String phone;
    @DocumentField private String telephone;         // 座机
    @DocumentField private Long agentUserId;

    @DocumentField  private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentOuterResource.class, id);
    }
}
