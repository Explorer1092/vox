package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.CrmWorkRecordLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.CrmWorkRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrmWorkRecordLoaderImpl
 *
 * @author song.wang
 * @date 2017/12/4
 */
@Named
@Service(interfaceClass = CrmWorkRecordLoader.class)
@ExposeService(interfaceClass = CrmWorkRecordLoader.class)
public class CrmWorkRecordLoaderImpl extends SpringContainerSupport implements CrmWorkRecordLoader {

    @Inject
    CrmWorkRecordDao crmWorkRecordDao;

    @Override
    public CrmWorkRecord load(String id) {
        return crmWorkRecordDao.load(id);
    }

    @Override
    public Map<String, CrmWorkRecord> loads(Collection<String> ids){
        return crmWorkRecordDao.loads(ids);
    }

    @Override
    public List<CrmWorkRecord> findByWorker(Long workerId) {
        return crmWorkRecordDao.findByWorker(workerId);
    }

    @Override
    public Map<Long, List<CrmWorkRecord>> findByWorkers(Collection<Long> workerIds) {
        if(CollectionUtils.isEmpty(workerIds)){
            return new HashMap<>();
        }
        return crmWorkRecordDao.findByWorkers(workerIds);
    }

    @Override
    public List<CrmWorkRecord> findBySchool(Long schoolId) {
        return crmWorkRecordDao.findBySchool(schoolId);
    }

    @Override
    public Map<Long, List<CrmWorkRecord>> findBySchools(Collection<Long> schoolIds) {
        if(CollectionUtils.isEmpty(schoolIds)){
            return new HashMap<>();
        }
        return crmWorkRecordDao.findBySchools(schoolIds);
    }

    @Override
    public List<CrmWorkRecord> listByTaskDetailId(String taskDetailId) {
        return crmWorkRecordDao.listByTaskDetailId(taskDetailId);
    }

    @Override
    public long countByTaskDetailIdAndWorkerId(String taskDetailId, Long workerId) {
        return crmWorkRecordDao.countByTaskDetailIdAndWorkerId(taskDetailId, workerId);
    }

    @Override
    public List<CrmWorkRecord> listByWorkersAndTime(Collection<Long> workerIds, Date startTime, Date endTime) {
        if(CollectionUtils.isEmpty(workerIds) || startTime == null || endTime == null){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.listByWorkersAndTime(workerIds, startTime, endTime);
    }

    @Override
    public List<CrmWorkRecord> listByWorkerAndType(Long workerId, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        if(workerId == null || recordType == null){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.listByWorkerAndType(workerId, recordType, startDate, endDate);
    }

    @Override
    public List<CrmWorkRecord> listByWorkersAndType(Collection<Long> workerIds, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        if(CollectionUtils.isEmpty(workerIds) || recordType == null){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.listByWorkersAndType(workerIds, recordType, startDate, endDate);
    }

    @Override
    public List<CrmWorkRecord> listByStartDateAndEndDateAndWorkerId(Date startDate, Date endDate, Long workerId) {
        if(workerId == null){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.listByStartDateAndEndDateAndWorkerId(startDate, endDate, workerId);
    }

    @Override
    public List<CrmWorkRecord> findAllByWorker(Long workerId, CrmWorkRecordType recordType) {
        if(workerId == null || recordType == null){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.findAllByWorker(workerId, recordType);
    }

    @Override
    public List<CrmWorkRecord> getVisitRecordsByIntoRecordId(String intoSchoolRecordId) {
        if(StringUtils.isBlank(intoSchoolRecordId)){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.getByIntoRecordId(intoSchoolRecordId).stream().filter(p -> Objects.equals(p.getWorkType(),CrmWorkRecordType.VISIT)).collect(Collectors.toList());
    }

    @Override
    public List<CrmWorkRecord> getJoinMeetingRecordsByIntoRecordId(String recordId) {
        if(StringUtils.isBlank(recordId)){
            return Collections.emptyList();
        }
        return crmWorkRecordDao.getByIntoRecordId(recordId).stream().filter(p -> Objects.equals(p.getWorkType(),CrmWorkRecordType.JOIN_MEETING)).collect(Collectors.toList());
    }

    @Override
    public Page<CrmWorkRecord> findPageByDateAndRegion(CrmWorkRecordType workType, Date startTime, Date endTime, Integer provinceCode, Integer cityCode, Integer countyCode, Pageable pageable) {
        if (workType == null) {
            return null;
        }
        startTime = startTime == null ? null : DateUtils.truncate(startTime, Calendar.DATE);
        endTime = endTime == null ? null : DateUtils.ceiling(endTime, Calendar.DATE);
        return crmWorkRecordDao.findPageByDateAndRegion(workType, startTime, endTime, provinceCode, cityCode, countyCode, pageable);
    }

    @Override
    public List<CrmWorkRecord> findByTeacherId(Long teacherId) {
        return crmWorkRecordDao.findByTeacherId(teacherId);
    }

    @Override
    public List<CrmWorkRecord> findByResearcherId(Long researcherId, CrmWorkRecordType workType) {
        return crmWorkRecordDao.findByResearcherId(researcherId,workType);
    }
}
