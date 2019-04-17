package com.voxlearning.utopia.service.workflow.impl.dao;

/**
 * Created by fugui.chang on 2016/11/7.
 */

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WorkFlowProcessHistory.class)
public class TestWorkFlowProcessHistoryPersistence {
    @Inject WorkFlowProcessHistoryPersistence workFlowProcessHistoryPersistence;

    @Test
    public void testLoadByProcessorAccount(){
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory();
        workFlowProcessHistory.setId(1002L);
        workFlowProcessHistory.setWorkFlowRecordId(2002L);
        workFlowProcessHistory.setSourceApp("admin");
        workFlowProcessHistory.setProcessNotes("processNotes");
        workFlowProcessHistory.setProcessorAccount("processorAccount0");
        workFlowProcessHistory.setResult(WorkFlowProcessResult.agree);
        workFlowProcessHistory.setProcessorName("processName");
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        workFlowProcessHistory = new WorkFlowProcessHistory();
        workFlowProcessHistory.setId(1003L);
        workFlowProcessHistory.setWorkFlowRecordId(2003L);
        workFlowProcessHistory.setSourceApp("admin");
        workFlowProcessHistory.setProcessNotes("processNotes");
        workFlowProcessHistory.setProcessorAccount("processorAccount0");
        workFlowProcessHistory.setResult(WorkFlowProcessResult.agree);
        workFlowProcessHistory.setProcessorName("processName");
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        workFlowProcessHistory = new WorkFlowProcessHistory();
        workFlowProcessHistory.setId(1004L);
        workFlowProcessHistory.setWorkFlowRecordId(2004L);
        workFlowProcessHistory.setSourceApp("admin");
        workFlowProcessHistory.setProcessNotes("processNotes");
        workFlowProcessHistory.setProcessorAccount("processorAccount1");
        workFlowProcessHistory.setResult(WorkFlowProcessResult.agree);
        workFlowProcessHistory.setProcessorName("processName");
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        List<WorkFlowProcessHistory> workFlowProcessHistoryList = workFlowProcessHistoryPersistence.loadByProcessorAccount("admin","processorAccount0");
        Assert.assertEquals(2,workFlowProcessHistoryList.size());

        workFlowProcessHistoryList = workFlowProcessHistoryPersistence.loadByProcessorAccount("admin","processorAccount1");
        Assert.assertEquals(1,workFlowProcessHistoryList.size());
    }

    @Test
    public void testLoadByWorkFlowRecordId(){
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory();
        workFlowProcessHistory.setId(1002L);
        workFlowProcessHistory.setWorkFlowRecordId(2000L);
        workFlowProcessHistory.setSourceApp("admin");
        workFlowProcessHistory.setProcessNotes("processNotes");
        workFlowProcessHistory.setProcessorAccount("processorAccount0");
        workFlowProcessHistory.setResult(WorkFlowProcessResult.agree);
        workFlowProcessHistory.setProcessorName("processName");
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        workFlowProcessHistory = new WorkFlowProcessHistory();
        workFlowProcessHistory.setId(1003L);
        workFlowProcessHistory.setWorkFlowRecordId(2000L);
        workFlowProcessHistory.setSourceApp("admin");
        workFlowProcessHistory.setProcessNotes("processNotes");
        workFlowProcessHistory.setProcessorAccount("processorAccount0");
        workFlowProcessHistory.setResult(WorkFlowProcessResult.agree);
        workFlowProcessHistory.setProcessorName("processName");
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

        List<WorkFlowProcessHistory> workFlowProcessHistoryList = workFlowProcessHistoryPersistence.loadByWorkFlowRecordId(2000L);
        Assert.assertEquals(2,workFlowProcessHistoryList.size());
    }

    @Test
    public void testLoadByWorkFlowType() {
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory();
        workFlowProcessHistory.setId(1002L);
        workFlowProcessHistory.setWorkFlowRecordId(2000L);
        workFlowProcessHistory.setSourceApp("admin");
        workFlowProcessHistory.setProcessNotes("processNotes");
        workFlowProcessHistory.setProcessorAccount("processorAccount0");
        workFlowProcessHistory.setResult(WorkFlowProcessResult.agree);
        workFlowProcessHistory.setProcessorName("processName");
        workFlowProcessHistory.setWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL);
        workFlowProcessHistoryPersistence.insert(workFlowProcessHistory);

//        Assert.assertEquals(1,workFlowProcessHistoryPersistence.loadByWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL).size());
    }
}
