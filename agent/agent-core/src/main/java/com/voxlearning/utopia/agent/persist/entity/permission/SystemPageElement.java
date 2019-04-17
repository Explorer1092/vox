package com.voxlearning.utopia.agent.persist.entity.permission;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.view.permission.SystemPageElementView;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统页面元素表
 *
 * @author song.wang
 * @date 2018/5/15
 */

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_PAGE_ELEMENT")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180516")
public class SystemPageElement extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = -1395931864369906147L;

    private String module;                         // 模块
    private String subModule;                      // 子模块
    private String pageName;                       // 页面名称
    private String elementCode;                    // 元素编码
    private String elementName;                    // 元素名称
    private String comment;                        // 备注

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey("ALL")
        };
    }

    public SystemPageElementView toViewData(){
        SystemPageElementView view = new SystemPageElementView();
        view.setId(this.id);
        view.setModule(this.module);
        view.setSubModule(this.subModule);
        view.setPageName(this.pageName);
        view.setElementCode(this.elementCode);
        view.setElementName(this.elementName);
        view.setComment(this.comment);
        return view;
    }
}
