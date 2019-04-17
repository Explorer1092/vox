package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.CrmWorkRecordLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CrmWorkRecordLoaderClient
 *
 * @author song.wang
 * @date 2017/12/4
 */

public class CrmWorkRecordLoaderClient implements CrmWorkRecordLoader {

    @ImportService(interfaceClass = CrmWorkRecordLoader.class)
    private CrmWorkRecordLoader remoteReference;

    @Override
    public CrmWorkRecord load(String id) {
        return remoteReference.load(id);
    }

    @Override
    public Map<String, CrmWorkRecord> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }

    @Override
    public List<CrmWorkRecord> findByWorker(Long workerId) {
        return remoteReference.findByWorker(workerId);
    }

    @Override
    public Map<Long, List<CrmWorkRecord>> findByWorkers(Collection<Long> workerIds) {
        return remoteReference.findByWorkers(workerIds);
    }

    @Override
    public List<CrmWorkRecord> findBySchool(Long schoolId) {
        return remoteReference.findBySchool(schoolId);
    }

    @Override
    public Map<Long, List<CrmWorkRecord>> findBySchools(Collection<Long> schoolIds) {
        return remoteReference.findBySchools(schoolIds);
    }

    @Override
    public List<CrmWorkRecord> listByTaskDetailId(String taskDetailId) {
        return remoteReference.listByTaskDetailId(taskDetailId);
    }

    @Override
    public long countByTaskDetailIdAndWorkerId(String taskDetailId, Long workerId) {
        return remoteReference.countByTaskDetailIdAndWorkerId(taskDetailId, workerId);
    }

    @Override
    public List<CrmWorkRecord> listByWorkersAndTime(Collection<Long> workerIds, Date startTime, Date endTime) {
        return remoteReference.listByWorkersAndTime(workerIds, startTime, endTime);
    }

    @Override
    public List<CrmWorkRecord> listByWorkerAndType(Long workerId, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        return remoteReference.listByWorkerAndType(workerId, recordType, startDate, endDate);
    }

    @Override
    public List<CrmWorkRecord> listByWorkersAndType(Collection<Long> workerIds, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        return remoteReference.listByWorkersAndType(workerIds, recordType, startDate, endDate);
    }

    @Override
    public List<CrmWorkRecord> listByStartDateAndEndDateAndWorkerId(Date startDate, Date endDate, Long workerId) {
        return remoteReference.listByStartDateAndEndDateAndWorkerId(startDate, endDate, workerId);
    }

    @Override
    public List<CrmWorkRecord> findAllByWorker(Long workerId, CrmWorkRecordType recordType) {
        return remoteReference.findAllByWorker(workerId, recordType);
    }

    @Override
    public List<CrmWorkRecord> getVisitRecordsByIntoRecordId(String intoSchoolRecordId) {
        return remoteReference.getVisitRecordsByIntoRecordId(intoSchoolRecordId);
    }

    @Override
    public List<CrmWorkRecord> getJoinMeetingRecordsByIntoRecordId(String recordId) {
        return remoteReference.getJoinMeetingRecordsByIntoRecordId(recordId);
    }

    @Override
    public Page<CrmWorkRecord> findPageByDateAndRegion(CrmWorkRecordType workType, Date startTime, Date endTime, Integer provinceCode, Integer cityCode, Integer countyCode, Pageable pageable) {
        return remoteReference.findPageByDateAndRegion(workType, startTime, endTime, provinceCode, cityCode, countyCode, pageable);
    }

    @Override
    public List<CrmWorkRecord> findByTeacherId(Long teacherId) {
        return remoteReference.findByTeacherId(teacherId);
    }

    @Override
    public List<CrmWorkRecord> findByResearcherId(Long researcherId,CrmWorkRecordType workType) {
        return remoteReference.findByResearcherId(researcherId,workType);
    }

}
