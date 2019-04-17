package com.voxlearning.utopia.agent.persist.entity.partner;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * @description: 合作机构联系人关系表
 * @author: kaibo.he
 * @create: 2019-04-01 21:23
 **/
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_PARTNER_LINKMAN")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20190401")
public class AgentPartnerLinkMan extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private Long partnerId;
    private Long linkManId;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("pid",this.partnerId),
        };
    }
}
