package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordTeacherLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * WorkRecordTeacherLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordTeacherLoaderClient implements WorkRecordTeacherLoader {

    @ImportService(interfaceClass = WorkRecordTeacherLoader.class)
    private WorkRecordTeacherLoader remoteReference;

    @Override
    public WorkRecordTeacher load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String,WorkRecordTeacher> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }

    @Override
    public List<WorkRecordTeacher> loadByTeacherId(Long teacherId) {
        return remoteReference.loadByTeacherId(teacherId);
    }

}
