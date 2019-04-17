package com.voxlearning.utopia.service.workflow.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
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
@DocumentTable(table = "VOX_WORKFLOW_PROCESS_HISTORY")
@UtopiaCacheRevision("20161107")
@UtopiaCacheExpiration(3600)
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowProcessHistory extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -8041125795029808417L;

    @UtopiaSqlColumn(name = "workflow_record_id") private Long workFlowRecordId;               // 工作流记录ID
    @UtopiaSqlColumn private String sourceApp;                    // 处理源， admin / agent / mizar
    @UtopiaSqlColumn private String processorAccount;             // 处理者账号
    @UtopiaSqlColumn private String processorName;                // 处理者姓名
    @UtopiaSqlColumn private WorkFlowProcessResult result;                       // 处理结果  agree: 同意   reject: 拒绝 ， raiseup
    @UtopiaSqlColumn private String processNotes;                 // 处理结果备注
    @UtopiaSqlColumn private WorkFlowType workFlowType;                  // 工作流类型

    public static String cacheKeyFromProcessorAccount(String sourceApp, String processorAccount) {
        return CacheKeyGenerator.generateCacheKey(WorkFlowProcessHistory.class, new String[]{"sourceApp", "processorAccount"}, new Object[]{sourceApp, processorAccount});
    }

    public static String cacheKeyFromWorkFlowRecordId(Long workFlowRecordId) {
        return CacheKeyGenerator.generateCacheKey(WorkFlowProcessHistory.class, "workFlowRecordId", workFlowRecordId);
    }
}
