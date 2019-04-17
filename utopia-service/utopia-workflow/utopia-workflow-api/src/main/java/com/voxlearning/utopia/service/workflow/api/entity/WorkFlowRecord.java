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
@DocumentTable(table = "VOX_WORKFLOW_RECORD")
@UtopiaCacheRevision("20161107")
@UtopiaCacheExpiration(3600)
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowRecord extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 1776423245345313365L;

    @UtopiaSqlColumn(name = "source_app") private String sourceApp;                        // 发起源， admin / agent / mizar
    @UtopiaSqlColumn(name = "creator_account") private String creatorAccount;              // 发起人账号， admin：account(string)/ agent：id(long)/mizar：id(string)
    @UtopiaSqlColumn(name = "creator_name") private String creatorName;                    // 用户姓名
    @UtopiaSqlColumn(name = "task_name") private String taskName;                          // 要处理的任务名称
    @UtopiaSqlColumn(name = "task_content") private String taskContent;                    // 要处理的任务说明
    @UtopiaSqlColumn(name = "task_detail_url") private String taskDetailUrl;               // 要处理的任务详情URL，URL是sourceApp对应的URL
    @UtopiaSqlColumn(name = "latest_processor_name") private String latestProcessorName;   // 最近处理人姓名
    @UtopiaSqlColumn(name = "status") private String status;                               // 任务状态
    @UtopiaSqlColumn(name = "work_flow_type") private WorkFlowType workFlowType;           // 工作流类型


    public static String cacheKeyFromCreatorAccount(String sourceApp, String creatorAccount) {
        return CacheKeyGenerator.generateCacheKey(WorkFlowRecord.class, new String[]{"sourceApp", "creatorAccount"}, new Object[]{sourceApp, creatorAccount});
    }
}
