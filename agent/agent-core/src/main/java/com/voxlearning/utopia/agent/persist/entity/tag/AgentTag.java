package com.voxlearning.utopia.agent.persist.entity.tag;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentTagSubType;
import com.voxlearning.utopia.agent.constants.AgentTagType;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 *  标签实体
 * @author deliang.che
 * @since  2019/3/20
 */

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_TAG")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20190320")
public class AgentTag extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private static final long serialVersionUID = 9047897903083680761L;
    private AgentTagType tagType;       //类别
    private AgentTagSubType tagSubType; //子类别
    private String name;        // 名称
    private Integer coverNum;   //覆盖数量
    private Boolean isVisible;  //天玑是否可见
    private Integer sortNum;    //排序数值


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("name", this.name),
                newCacheKey("tagType", this.tagType),
                newCacheKey(new String[]{"tagType","tagSubType"},new Object[]{this.tagType,this.tagSubType})
        };
    }
}

