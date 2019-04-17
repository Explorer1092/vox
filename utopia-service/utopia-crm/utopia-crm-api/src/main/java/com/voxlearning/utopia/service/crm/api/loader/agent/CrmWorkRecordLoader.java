package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CrmWorkRecordService
 *
 * @author song.wang
 * @date 2017/12/4
 */
@ServiceVersion(version = "2018.06.12")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface CrmWorkRecordLoader extends IPingable {

    CrmWorkRecord load(String id);

    Map<String, CrmWorkRecord> loads(Collection<String> ids);

    List<CrmWorkRecord> findByWorker(Long workerId);

    Map<Long, List<CrmWorkRecord>> findByWorkers(Collection<Long> workerIds);

    List<CrmWorkRecord> findBySchool(Long schoolId);

    Map<Long, List<CrmWorkRecord>> findBySchools(Collection<Long> schoolIds);

    List<CrmWorkRecord> listByTaskDetailId(String taskDetailId);

    long countByTaskDetailIdAndWorkerId(String taskDetailId, Long workerId);

    List<CrmWorkRecord> listByWorkersAndTime(Collection<Long> workerIds, Date startTime, Date endTime);

    List<CrmWorkRecord> listByWorkerAndType(Long workerId, CrmWorkRecordType recordType, Date startDate, Date endDate);

    List<CrmWorkRecord> listByWorkersAndType(Collection<Long> workerIds, CrmWorkRecordType recordType, Date startDate, Date endDate);

    List<CrmWorkRecord>  listByStartDateAndEndDateAndWorkerId(Date startDate,Date endDate,Long workerId);

    List<CrmWorkRecord> findAllByWorker(Long workerId, CrmWorkRecordType recordType);

    List<CrmWorkRecord> getVisitRecordsByIntoRecordId(String intoSchoolRecordId);


    List<CrmWorkRecord> getJoinMeetingRecordsByIntoRecordId(String recordId);

    Page<CrmWorkRecord> findPageByDateAndRegion(CrmWorkRecordType workType, Date startTime, Date endTime, Integer provinceCode, Integer cityCode, Integer countyCode, Pageable pageable);

    List<CrmWorkRecord> findByTeacherId(Long teacherId);

    List<CrmWorkRecord> findByResearcherId(Long researcherId,CrmWorkRecordType workType);
}
