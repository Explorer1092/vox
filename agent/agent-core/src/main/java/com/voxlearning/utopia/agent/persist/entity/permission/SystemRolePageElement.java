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
 * SystemRolePageElement
 *
 * @author song.wang
 * @date 2018/5/16
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ROLE_PAGE_ELEMENT")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180516")
public class SystemRolePageElement extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = -3927194259181008097L;

    private Integer roleId;                     // 角色ID
    private Long pageElementId;                 // 页面元素ID

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("rid", roleId),
                newCacheKey("peId", pageElementId),
                newCacheKey("ALL")
        };
    }
}
