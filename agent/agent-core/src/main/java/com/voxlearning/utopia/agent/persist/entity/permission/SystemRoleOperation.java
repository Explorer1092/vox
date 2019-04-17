package com.voxlearning.utopia.agent.persist.entity.permission;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * SystemRoleOperation
 *
 * @author song.wang
 * @date 2018/6/11
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ROLE_OPERATION")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180611")
public class SystemRoleOperation extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private static final long serialVersionUID = -1156838178946399180L;

    private Integer roleId;                     // 角色ID
    private Long operationId;                 // 功能操作ID


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("rid", this.roleId),
                newCacheKey("oid", this.operationId),
                newCacheKey("ALL")
        };
    }
}
