package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordTeacherService;

import java.util.Collection;
import java.util.List;

/**
 * WorkRecordTeacherServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordTeacherServiceClient implements WorkRecordTeacherService {

    @ImportService(interfaceClass = WorkRecordTeacherService.class)
    private WorkRecordTeacherService remoteReference;

    @Override
    public String insert(WorkRecordTeacher workRecordTeacher){
        return remoteReference.insert(workRecordTeacher);
    }

    @Override
    public List<String> inserts(Collection<WorkRecordTeacher> workRecordTeacherList){
        return remoteReference.inserts(workRecordTeacherList);
    }

    @Override
    public WorkRecordTeacher update(WorkRecordTeacher workRecordTeacher) {
        return remoteReference.update(workRecordTeacher);
    }
}
