package com.voxlearning.utopia.agent.persist.entity.permission;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.view.permission.SystemOperationView;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统操作权限配置（配置用户对某个功能的操作权限）
 *
 * @author song.wang
 * @date 2018/6/11
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_OPERATION")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180616")
public class SystemOperation extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private static final long serialVersionUID = 6776984239599796044L;

    private String module;                         // 模块
    private String subModule;                      // 子模块
    private String operationCode;                  // 操作权限码  有该权限码的角色可以访问相应controller中有 OperationCode 注解的url
    private String operationName;                  // 功能操作
    private String comment;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey("ALL")
        };
    }

    public SystemOperationView toViewData(){
        SystemOperationView view = new SystemOperationView();
        view.setId(this.id);
        view.setModule(this.module);
        view.setSubModule(this.subModule);
        view.setOperationCode(this.operationCode);
        view.setOperationName(this.operationName);
        view.setComment(this.comment);
        return view;
    }
}
