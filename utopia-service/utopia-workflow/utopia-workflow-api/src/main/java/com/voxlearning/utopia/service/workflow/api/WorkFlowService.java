package com.voxlearning.utopia.service.workflow.api;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;

import java.util.concurrent.TimeUnit;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@ServiceVersion(version = "20170327")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface WorkFlowService extends IPingable {

    MapMessage agree(WorkFlowContext workFlowContext);

    MapMessage reject(WorkFlowContext workFlowContext);

    MapMessage raiseup(WorkFlowContext workFlowContext);

    // 撤销
    MapMessage revoke(WorkFlowContext workFlowContext);

    // 用于工作流结束之后生成一个操作历史
    @NoResponseWait
    void insertWorkFlowHistory(WorkFlowProcessHistory workFlowProcessHistory);

}
