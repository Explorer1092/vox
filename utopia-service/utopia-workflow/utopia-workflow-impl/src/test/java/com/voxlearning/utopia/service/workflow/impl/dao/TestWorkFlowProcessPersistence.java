package com.voxlearning.utopia.service.workflow.impl.dao;

/**
 * Created by fugui.chang on 2016/11/7.
 */

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WorkFlowProcess.class)
public class TestWorkFlowProcessPersistence {
    @Inject
    WorkFlowProcessPersistence workFlowProcessPersistence;

    @Test
    public void testLoadByTargetUser() {
        WorkFlowProcess workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(1001L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser0");
        workFlowProcess.setWorkflowRecordId(20001L);
        workFlowProcessPersistence.insert(workFlowProcess);

        workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(1002L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser0");
        workFlowProcess.setWorkflowRecordId(20002L);
        workFlowProcessPersistence.insert(workFlowProcess);

        workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(1003L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser1");
        workFlowProcess.setWorkflowRecordId(20003L);
        workFlowProcessPersistence.insert(workFlowProcess);

        List<WorkFlowProcess> workFlowProcessList = workFlowProcessPersistence.loadByTargetUser("admin","targetUser0");
        assertEquals(2, workFlowProcessList.size());
    }

    @Test
    public void testLoadByWorkflowRecordId() {
        WorkFlowProcess workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(100000L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser0");
        workFlowProcess.setWorkflowRecordId(200000L);
        workFlowProcessPersistence.insert(workFlowProcess);

        workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(100001L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser1");
        workFlowProcess.setWorkflowRecordId(200000L);
        workFlowProcessPersistence.insert(workFlowProcess);

        List<WorkFlowProcess> workFlowProcessList = workFlowProcessPersistence.loadByWorkflowRecordId(200000L);
        assertEquals(2,workFlowProcessList.size());
    }

    @Test
    public void testDisableByWorkflowRecordId(){
        WorkFlowProcess workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(100000L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser0");
        workFlowProcess.setWorkflowRecordId(200000L);
        workFlowProcessPersistence.insert(workFlowProcess);
        List<WorkFlowProcess> workFlowProcessList = workFlowProcessPersistence.loadByWorkflowRecordId(200000L);
        assertEquals(0,workFlowProcessList.size());
        workFlowProcess = workFlowProcessPersistence.load(100000L);
        assertNotNull(workFlowProcess);
    }

    @Test
    public void testDeleteByWorkflowRecordId(){
        WorkFlowProcess workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(100000L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser0");
        workFlowProcess.setWorkflowRecordId(200000L);
        workFlowProcessPersistence.insert(workFlowProcess);
        workFlowProcessPersistence.deleteByWorkflowRecordId(200000L);
        workFlowProcess = workFlowProcessPersistence.load(100000L);
        assertNull(workFlowProcess);
    }

    @Test
    public void testLoadByWorkFlowType() {
        WorkFlowProcess workFlowProcess = new WorkFlowProcess();
        workFlowProcess.setId(100000L);
        workFlowProcess.setSourceApp("admin");
        workFlowProcess.setTargetUser("targetUser0");
        workFlowProcess.setWorkflowRecordId(200000L);
        workFlowProcess.setWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL);
        workFlowProcessPersistence.insert(workFlowProcess);
//        assertEquals(1,workFlowProcessPersistence.loadByWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL).size());

    }
}
