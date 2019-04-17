package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.entity.ChipsActiveServiceRecord;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type = ChipsActiveServiceRecord.class)
public class ChipsActiveServiceRecordDao extends AlpsStaticMongoDao<ChipsActiveServiceRecord, String> {

    /**
     * 全部状态
     */
    private static int STATUS_ALL = 2;
    /**
     * 完成状态
     */
    private static int STATUS_FINISNED = 1;
    /**
     * 未完成状态
     */
    private static int STATUS_UNFINISHED = 0;
    /**
     * 未审核
     */
    private static int STATUS_UNEXAMINED = 3;


    @Override
    protected void calculateCacheDimensions(ChipsActiveServiceRecord record, Collection<String> collection) {
        collection.add(ChipsActiveServiceRecord.ck_serviceType_classId(record.getServiceType(), record.getClassId()));
        collection.add(ChipsActiveServiceRecord.ck_classId(record.getClassId()));
    }

    /**
     * 筛选
     *
     * @param classId
     * @param status
     * @param unitId
     * @param date
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<ChipsActiveServiceRecord> loadByClassIdFilter(ChipsActiveServiceType serviceType, Long classId, int status, String unitId, Date date,
                                                              Long userId, int pageNum, int pageSize) {
        Criteria criteria = Criteria.where("class_id").is(classId);


        if (serviceType != null) {
            criteria.and("service_type").is(serviceType.name());
        }

        if (status == STATUS_FINISNED || status == STATUS_UNFINISHED) {
            criteria.and("serviced").is(status == STATUS_FINISNED);
        } else if (status == STATUS_UNEXAMINED) {//未审核
            criteria.and("examine_status").is(false);
        }


        if (unitId != null && !"".equals(unitId)) {
            criteria.and("unit_id").is(unitId);
        }

        if (date != null) {
            criteria.and("createDate").gte(getBeginTime(date));
            criteria.and("createDate").lte(getEndTime(date));
        }

        if (userId != null && userId != -1L) {
            criteria.and("user_id").is(userId);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);

        return query(Query.query(criteria).with(sort).with(pageable));
    }

    /**
     * 筛选总数
     *
     * @param classId
     * @param status
     * @param unitId
     * @param date
     * @param userId
     * @return
     */
    public long count(ChipsActiveServiceType serviceType, Long classId, int status, String unitId, Date date, Long userId) {
        Criteria criteria = Criteria.where("class_id").is(classId);

        if (serviceType != null) {
            criteria.and("service_type").is(serviceType.name());
        }

        if (status == STATUS_FINISNED || status == STATUS_UNFINISHED) {
            criteria.and("serviced").is(status == STATUS_FINISNED);
        } else if (status == STATUS_UNEXAMINED) {//未审核
            criteria.and("examine_status").is(false);
        }

        if (unitId != null && !"".equals(unitId)) {
            criteria.and("unit_id").is(unitId);
        }

        if (date != null) {
            criteria.and("createDate").gte(getBeginTime(date));
            criteria.and("createDate").lte(getEndTime(date));
        }

        if (userId != null && userId != -1L) {
            criteria.and("user_id").is(userId);
        }
        return count(Query.query(criteria));
    }

    @CacheMethod
    public List<ChipsActiveServiceRecord> loadByClazzId(@CacheParameter(value = "CID") Long clazzId) {
        return query(Query.query(Criteria.where("class_id").is(clazzId)));
    }

    @CacheMethod
    public List<ChipsActiveServiceRecord> loadByClassId(@CacheParameter(value = "serviceType") ChipsActiveServiceType serviceType,
                                                        @CacheParameter(value = "classId") Long classId) {
        Criteria criteria = Criteria.where("class_id").is(classId);

        if (serviceType != null) {
            criteria.and("service_type").is(serviceType.name());
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createDate").and(new Sort(Sort.Direction.DESC, "service_type"));
        return query(Query.query(criteria).with(sort));
    }

    public void disabled(Long userId, long classId) {
        Criteria criteria = Criteria.where("user_id").is(userId).and("class_id").is(classId);
        long count = $remove(Query.query(criteria));
        if (count > 0) {
            cleanCache(ChipsActiveServiceType.REMIND, classId);
            cleanCache(ChipsActiveServiceType.SERVICE, classId);
        }
    }

    /**
     * 更新状态为已服务
     *
     * @param classId
     * @param unitId
     * @param userId
     */
    public void updateToSerivced(ChipsActiveServiceType serviceType, Long classId, Long userId, String unitId) {
        String id = ChipsActiveServiceRecord.genId(serviceType, classId, userId, unitId);
        Update update = new Update();
        update.set("serviced", Boolean.TRUE);
        update.set("updateDate", new Date());
        Criteria criteria = Criteria.where("_id").is(id).and("serviced").is(Boolean.FALSE);
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCache(serviceType, classId);
        }
    }

    public void cleanCache(ChipsActiveServiceType serviceType, Long classId) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsActiveServiceRecord.ck_serviceType_classId(serviceType.name(), classId));
        getCache().deletes(cacheIds);
    }

    private Date getBeginTime(Date begin) {
        return DayRange.newInstance(begin.getTime()).getStartDate();
    }

    private Date getEndTime(Date begin) {
        return DayRange.newInstance(begin.getTime()).getEndDate();
    }

    public List<ChipsActiveServiceRecord> loadByServiceTypeDate(ChipsActiveServiceType serviceType, Date beginDate) {
        Criteria criteria = Criteria.where("service_type").is(serviceType.name());
        criteria.and("createDate").gte(beginDate);
        return query(Query.query(criteria));
    }

    public void updateUserVideoId(Long userId, String unitId, String userVideoId) {
        Criteria criteria = Criteria.where("service_type").is(ChipsActiveServiceType.SERVICE).and("user_id").is(userId).and("unit_id").is(unitId);
        Update update = new Update()
                .set("user_video_id", userVideoId)
                .set("examine_status", false)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanByCriteria(criteria);
        }
    }
    public void updateExamineStatusAndVideoUrl(Long userId, String unitId, String videoUrl) {
        Criteria criteria = Criteria.where("service_type").is(ChipsActiveServiceType.SERVICE).and("user_id").is(userId).and("unit_id").is(unitId);
        Update update = new Update()
                .set("examine_status", true)
                .set("video_url", videoUrl)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanByCriteria(criteria);
        }
    }

    public void updateRemarkStatus(Long userId, String unitId, ChipsActiveServiceRecord.RemarkStatus remarkStatus) {
        Criteria criteria = Criteria.where("service_type").is(ChipsActiveServiceType.SERVICE).and("user_id").is(userId).and("unit_id").is(unitId);
        Update update = new Update()
                .set("remark_status", remarkStatus.getVal())
//                .set("examine_status", false)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanByCriteria(criteria);
        }
    }

    private void cleanByCriteria(Criteria criteria) {
        List<ChipsActiveServiceRecord> list = query(Query.query(criteria));
        Set<Long> clazzIdSet = list.stream().map(r -> r.getClassId()).collect(Collectors.toSet());
        for (Long clazzId : clazzIdSet) {
            cleanCache(ChipsActiveServiceType.SERVICE, clazzId);
        }
    }

}
