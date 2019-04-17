package com.voxlearning.utopia.service.workflow.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
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
@TruncateDatabaseTable(databaseEntities = WorkFlowRecord.class)
public class TestWorkFlowRecordPersistence {

    @Inject
    WorkFlowRecordPersistence workFlowRecordPersistence;

    @Test
    public void testLoadByCreatorAccount() {
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setId(1001L);
        workFlowRecord.setCreatorAccount("100001");
        workFlowRecord.setCreatorName("creatorName");
        workFlowRecord.setLatestProcessorName("latestProcessorName");
        workFlowRecord.setSourceApp("adimn");
        workFlowRecord.setStatus("status");
        workFlowRecord.setTaskContent("taskContent");
        workFlowRecord.setTaskName("tashName");
        workFlowRecord.setTaskDetailUrl("taskDetailUrl");
        workFlowRecordPersistence.insert(workFlowRecord);

        workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setId(1002L);
        workFlowRecord.setCreatorAccount("100001");
        workFlowRecord.setCreatorName("creatorName");
        workFlowRecord.setLatestProcessorName("latestProcessorName");
        workFlowRecord.setSourceApp("adimn");
        workFlowRecord.setStatus("status");
        workFlowRecord.setTaskContent("taskContent");
        workFlowRecord.setTaskName("tashName");
        workFlowRecord.setTaskDetailUrl("taskDetailUrl");
        workFlowRecordPersistence.insert(workFlowRecord);

        workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setId(1003L);
        workFlowRecord.setCreatorAccount("100002");
        workFlowRecord.setCreatorName("creatorName");
        workFlowRecord.setLatestProcessorName("latestProcessorName");
        workFlowRecord.setSourceApp("adimn");
        workFlowRecord.setStatus("status");
        workFlowRecord.setTaskContent("taskContent");
        workFlowRecord.setTaskName("tashName");
        workFlowRecord.setTaskDetailUrl("taskDetailUrl");
        workFlowRecordPersistence.insert(workFlowRecord);

        List<WorkFlowRecord> workFlowRecordList = workFlowRecordPersistence.loadByCreatorAccount("adimn","100001");
        Assert.assertEquals(2,workFlowRecordList.size());

        workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setId(1004L);
        workFlowRecord.setCreatorAccount("100001");
        workFlowRecord.setCreatorName("creatorName");
        workFlowRecord.setLatestProcessorName("latestProcessorName");
        workFlowRecord.setSourceApp("adimn");
        workFlowRecord.setStatus("status");
        workFlowRecord.setTaskContent("taskContent");
        workFlowRecord.setTaskName("tashName");
        workFlowRecord.setTaskDetailUrl("taskDetailUrl");
        workFlowRecordPersistence.insert(workFlowRecord);
        workFlowRecordList = workFlowRecordPersistence.loadByCreatorAccount("adimn","100001");
        Assert.assertEquals(3,workFlowRecordList.size());

        workFlowRecord = workFlowRecordPersistence.load(1004L);
        workFlowRecord.setTaskContent("updateTaskContent");
        workFlowRecordPersistence.upsert(workFlowRecord);
        workFlowRecordList = workFlowRecordPersistence.loadByCreatorAccount("adimn","100001");
        Assert.assertEquals(3,workFlowRecordList.size());

    }
    @Test
    public void testLoadByWorkFlowType() {
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setId(1001L);
        workFlowRecord.setCreatorAccount("100001");
        workFlowRecord.setCreatorName("creatorName");
        workFlowRecord.setLatestProcessorName("latestProcessorName");
        workFlowRecord.setSourceApp("adimn");
        workFlowRecord.setStatus("status");
        workFlowRecord.setTaskContent("taskContent");
        workFlowRecord.setTaskName("tashName");
        workFlowRecord.setTaskDetailUrl("taskDetailUrl");
        workFlowRecord.setWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL);
        workFlowRecordPersistence.insert(workFlowRecord);

//        List<WorkFlowRecord> workFlowRecordList = workFlowRecordPersistence.loadByWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL);
//        Assert.assertEquals(1,workFlowRecordList.size());
    }

}
