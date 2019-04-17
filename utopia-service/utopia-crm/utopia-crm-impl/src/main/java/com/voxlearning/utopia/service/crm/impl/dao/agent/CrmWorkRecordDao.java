/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.calendar.DateUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/10/10
 */
@Named
@CacheBean(type = CrmWorkRecord.class)
public class CrmWorkRecordDao extends StaticMongoDao<CrmWorkRecord, String> {

    @Override
    protected void calculateCacheDimensions(CrmWorkRecord source, Collection<String> dimensions) {
        dimensions.add(CrmWorkRecord.ck_worker(source.getWorkerId()));
        dimensions.add(CrmWorkRecord.ck_school(source.getSchoolId()));
        dimensions.add(CrmWorkRecord.ck_workerId_type(source.getWorkerId(), source.getWorkType()));
        dimensions.add(CrmWorkRecord.ck_taskDetailId(source.getTaskDetailId()));
        dimensions.add(CrmWorkRecord.ck_intoSchool(source.getSchoolWorkRecordId()));
    }

    @CacheMethod
    public List<CrmWorkRecord> findByWorker(@CacheParameter("WID") Long workerId) {
        // 员工的工作记录默认只取一个月以内的
        Date dateFrom = DateUtils.calculateDateDay(new Date(), -32);
        Filter m = filterBuilder.where("workerId").is(workerId)
                .and("workTime").gte(dateFrom)
                .and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(m, sort);
    }

    @CacheMethod(type = List.class)
    public Map<Long, List<CrmWorkRecord>> findByWorkers(@CacheParameter(value = "WID", multiple = true) Collection<Long> workerIds) {
        // 员工的工作记录默认只取半年以内的
        Date dateFrom = DateUtils.calculateDateDay(new Date(), -32);
        Filter m = filterBuilder.where("workerId").in(workerIds)
                .and("workTime").gte(dateFrom)
                .and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(m, sort).stream().collect(Collectors.groupingBy(CrmWorkRecord::getWorkerId));
    }

    @CacheMethod
    public List<CrmWorkRecord> findBySchool(@CacheParameter("SID") Long schoolId) {
        // 学校的工作记录取所有的
        Filter m = filterBuilder.where("schoolId").is(schoolId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(m, sort);
    }

    @CacheMethod
    public Map<Long, List<CrmWorkRecord>> findBySchools(@CacheParameter(value = "SID", multiple = true) Collection<Long> schoolIds) {
        // 学校的工作记录取所有的
        Filter m = filterBuilder.where("schoolId").in(schoolIds)
                .and("disabled").is(false);
        return find(m).stream().collect(Collectors.groupingBy(CrmWorkRecord::getSchoolId));
    }


    // --------------------------------------------------------------------------------------------------------------
    // 以下的方法可能都被删掉
    @CacheMethod
    public List<CrmWorkRecord> listByTaskDetailId(@CacheParameter("taskDetailId") String taskDetailId) {
        Filter filter = filterBuilder.where("taskDetailId").is(taskDetailId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);
    }

    public long countByTaskDetailIdAndWorkerId(String taskDetailId, Long workerId) {
        Filter filter = filterBuilder.where("taskDetailId").is(taskDetailId).and("workerId").is(workerId);
        return count(filter);
    }

    public List<CrmWorkRecord> listByWorkersAndTime(Collection<Long> workerIds, Date startTime, Date endTime) {
        Filter m = filterBuilder.where("workerId").in(workerIds).and("workTime").gte(startTime).lt(endTime);
        List<CrmWorkRecord> workRecordList = find(m);
        if(CollectionUtils.isEmpty(workRecordList)){
            return Collections.emptyList();
        }
        workRecordList = workRecordList.stream().filter(p -> p.getDisabled() == null || !p.getDisabled()).collect(Collectors.toList());
        Collections.sort(workRecordList, (o1, o2) -> o2.getWorkTime().compareTo(o1.getWorkTime()));
        return workRecordList;
    }

    private void smartFilter(Filter filter, String key, Object foot, Object top) {
        if (foot != null && top != null) {
            filter.and(key).gte(foot).lt(top);
        } else if (foot != null) {
            filter.and(key).gte(foot);
        } else if (top != null) {
            filter.and(key).lt(top);
        }
    }


    private long count(Filter filter) {
        return __count_OTF(Find.find(filter));
    }

    private List<CrmWorkRecord> find(Filter filter) {
        return __find_OTF(Find.find(filter));
    }

    private List<CrmWorkRecord> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }

    private Page<CrmWorkRecord> find(Filter filter, Pageable pageable) {
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public List<CrmWorkRecord> listByWorkerAndType(Long workerId, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        Filter filter = filterBuilder.where("workerId").is(workerId).and("workType").is(recordType);
        smartFilter(filter, "workTime", startDate, endDate);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        List<CrmWorkRecord> recordList = find(filter, sort);
        if (CollectionUtils.isNotEmpty(recordList)) {
            recordList = recordList.stream().filter(p -> p.getDisabled() == null || !p.getDisabled()).collect(Collectors.toList());
        }
        return recordList;
    }


    public List<CrmWorkRecord> listByWorkersAndType(Collection<Long> workerIds, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        Filter filter = filterBuilder.where("workerId").in(workerIds).and("workType").is(recordType);
        smartFilter(filter, "workTime", startDate, endDate);
//        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
//        List<CrmWorkRecord> recordList = find(filter, sort);
        List<CrmWorkRecord> recordList = find(filter);
        if (CollectionUtils.isNotEmpty(recordList)) {
            recordList = recordList.stream().filter(p -> p.getDisabled() == null || !p.getDisabled()).collect(Collectors.toList());
            Collections.sort(recordList, (o1, o2) -> o2.getWorkTime().compareTo(o1.getWorkTime()));
        }
        return recordList;
    }

    /**
     * 根据查询的月份开始结束日期 及 工作记录所属人员id标识 查询工作记录统计
     * @return List<CrmWorkRecord>
     * 采用新的查询方式 并不能使用原因是并没有集成新的接口
     * 按时间倒序排列
     */
    public List<CrmWorkRecord>  listByStartDateAndEndDateAndWorkerId(Date startDate,Date endDate,Long workerId){
        Filter filter = filterBuilder.where("workerId").is(workerId);
        smartFilter(filter, "workTime", startDate, endDate);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(filter, sort);
    }

    /**
     * @param workerId
     * @param recordType
     * @return
     */
    @CacheMethod
    public List<CrmWorkRecord> findAllByWorker(@CacheParameter("workerId") Long workerId, @CacheParameter("type")CrmWorkRecordType recordType) {
        Filter m = filterBuilder.where("workerId").is(workerId)
                .and("workType").is(recordType);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(m, sort);
    }

    /**
     * 根据schoolWorkRecordId查找记录
     * @param recordId
     * @return
     */
    @CacheMethod
    public List<CrmWorkRecord> getByIntoRecordId(@CacheParameter("ISRID") String recordId){
        Filter m = filterBuilder.where("schoolWorkRecordId").is(recordId)
                .and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(m, sort);
    }

    public Page<CrmWorkRecord> findPageByDateAndRegion(CrmWorkRecordType workType, Date startTime, Date endTime, Integer provinceCode, Integer cityCode, Integer countyCode, Pageable pageable) {
        Filter filter = filterBuilder.where("disabled").ne(true);
        filterIs(filter, "workType", workType);
        smartFilter(filter, "workTime", startTime, endTime);
        filterIs(filter, "provinceCode", provinceCode);
        filterIs(filter, "cityCode", cityCode);
        filterIs(filter, "countyCode", countyCode);
        return find(filter, pageable);
    }

    private void filterIs(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).is(value);
        }
    }

    public List<CrmWorkRecord> findByTeacherId(Long teacherId){
        Filter filter = filterBuilder.where("visitTeacherList.teacherId").is(teacherId).and("disabled").ne(true);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(filter, sort);
    }

    public List<CrmWorkRecord> findByResearcherId(Long researcherId,CrmWorkRecordType recordType){
        Filter filter = filterBuilder.where("visitedResearcherList.researcherId").is(researcherId).and("disabled").is(false).and("workType").is(recordType);
        Sort sort = new Sort(Sort.Direction.DESC, "workTime");
        return find(filter, sort);
    }
}
