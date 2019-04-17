package com.voxlearning.utopia.service.workflow.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_WORKFLOW_PROCESS")
@UtopiaCacheRevision("20161107")
@UtopiaCacheExpiration(3600)
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowProcess extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 8728292339529858889L;

    @UtopiaSqlColumn private Long workflowRecordId;               // 工作流记录ID
    @UtopiaSqlColumn private String sourceApp;                    // 处理源， admin / agent / mizar
    @UtopiaSqlColumn private String targetUser;                   // 处理者,注意:如果一个节点有多个人处理,那么新建多条记录
    @UtopiaSqlColumn private String targetUserName;                                // 处理者姓名
    @UtopiaSqlColumn private WorkFlowType workFlowType;                  // 工作流类型

    public static String cacheKeyFromTargetUser(String sourceApp, String targetUser) {
        return CacheKeyGenerator.generateCacheKey(WorkFlowProcess.class, new String[]{"sourceApp", "targetUser"}, new Object[]{sourceApp, targetUser});
    }

    public static String cacheKeyFromWorkflowRecordId(Long workflowRecordId) {
        return CacheKeyGenerator.generateCacheKey(WorkFlowProcess.class, "workflowRecordId", workflowRecordId);
    }

}
