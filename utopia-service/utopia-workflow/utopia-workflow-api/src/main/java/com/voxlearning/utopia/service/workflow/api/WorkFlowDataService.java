package com.voxlearning.utopia.service.workflow.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author fugui.chang
 * @since 2016/11/15
 */
@ServiceVersion(version = "20170426")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WorkFlowDataService extends IPingable {

   MapMessage clearWorkFlowRecordProcess(Long wordRecordId);

   /**
    * 设置工作流状态， 审核人员重置成新状态下可以审核的人员 <br/>
    * 可用于申请被驳回，在原有申请基础上修改后， 再次提交，重置原有工作流状态 （无需新建申请及工作流记录， 并且这样可以保留住原来所有的历史审核记录）
    * @param wordRecordId 工作流ID
    * @param status 状态
    * @param processUsers 可处理人员
     * @return MapMessage
     */
   MapMessage updateRecordStatus(Long wordRecordId, String status, List<WorkFlowProcessUser> processUsers);

   MapMessage addWorkFlowRecord(WorkFlowRecord workFlowRecord, WorkFlowProcessUser... processUsers);

}
