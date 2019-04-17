package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author chongfeng.qi
 * @data 2018
 */
@Named
@CacheBean(type = ActivityConfig.class, useValueWrapper = true)
public class ActivityConfigDao extends AlpsStaticMongoDao<ActivityConfig, String> {

    @Inject
    private ActivityConfigVersionDao activityConfigVersion;

    @Override
    protected void calculateCacheDimensions(ActivityConfig document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public void agree(String id, String auditor, Set<Subject> subjects) {
        ActivityConfig activityConfig = load(id);
        if (activityConfig == null) {
            return;
        }
        activityConfig.setStatus(2);
        activityConfig.setAuditor(auditor);
        activityConfig.setAuditorTime(new Date());
        activityConfig.setSubjects(subjects);
        upsert(activityConfig);
        activityConfigVersion.increment();
    }

    public void reject(String id, String auditor) {
        ActivityConfig activityConfig = load(id);
        if (activityConfig == null) {
            return;
        }
        activityConfig.setStatus(3);
        activityConfig.setAuditor(auditor);
        activityConfig.setUpdateTime(new Date());
        activityConfig.setAuditorTime(new Date());
        upsert(activityConfig);
    }

    public void disableActivity(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("disabled", true).set("updateTime", new Date());
        executeUpdateOne(createMongoConnection(), criteria, update);

        ActivityConfig load = load(id);
        getCache().deletes(Arrays.asList(load.generateCacheDimensions()));
        activityConfigVersion.increment();
    }

    @CacheMethod
    public Map<Long, List<ActivityConfig>> loadByApplicant(@CacheParameter(value = "AID", multiple = true) Collection<Long> applicants, @CacheParameter("AR") Integer applicantRole) {
        Criteria criteria = Criteria.where("applicant").in(applicants).and("disabled").is(false);
        if (applicantRole != null) {
            criteria.and("applicantRole").is(applicantRole);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return query(new Query(criteria).with(sort).limit(500)).stream().collect(groupingBy(ActivityConfig::getApplicant));
    }

    /**
     *
     * @param status
     * @param type
     * @param clazzLevel
     * @param name
     * @param applicantRole
     * @param page
     * @param pageSize
     * @return
     */
    public Page<ActivityConfig> load(Integer status, String type, Integer clazzLevel, String name, Integer applicantRole, int page, int pageSize) {
        Criteria criteria = Criteria.where("disabled").is(false);
        if (status != null && status != 0) {
            criteria.and("status").is(status);
        }
        ActivityTypeEnum typeEnum;
        if (StringUtils.isNoneBlank(type) && (typeEnum = ActivityTypeEnum.getType(type)) != null) {
            criteria.and("type").is(typeEnum.name());
        }
        if (clazzLevel != null && clazzLevel != 0) {
            criteria.and("clazzLevels").all(Collections.singletonList(clazzLevel));
        }
        if (StringUtils.isNoneBlank(name)) {
            criteria.and("title").is(name);
        }
        if (applicantRole != null) {
            criteria.and("applicantRole").is(applicantRole);
        }
        int realPage = page <= 0 ? 0 : page;
        Query query = new Query(criteria);
        long count = count(query);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return new PageImpl<>(query(query.skip(realPage * pageSize).limit(pageSize).with(sort)), new PageRequest(realPage, pageSize), count);
    }

    public List<ActivityConfig> loadAgreeStartingNoNotice() {
        Criteria criteria = Criteria.where("noticeStatus").is(false)
                .and("startTime").lte(DateUtils.addMinutes(new Date(), 5))
                .and("endTime").gte(new Date())
                .and("disabled").is(false)
                .and("status").is(2);
        Sort sort = new Sort(Sort.Direction.ASC, "startTime");
        return query(new Query(criteria).with(sort).limit(100));
    }

    public void editNoticeStatus(String id, Boolean noticeStatus) {
        ActivityConfig activityConfig = load(id);
        if (activityConfig == null) {
            return;
        }
        activityConfig.setNoticeStatus(noticeStatus);
        activityConfig.setUpdateTime(new Date());
        activityConfig.setAuditorTime(new Date());
        upsert(activityConfig);
    }

    /**
     * 给生成报告的 job 用
     */
    public List<ActivityConfig> loadAllActivityCofig() {
        Date today = DateUtils.getTodayStart();
        Date yesterday = DateUtils.addDays(today, -1); // 比如15号结束,16号凌晨依然需要生成报告,所以 endTime >= 昨天
        Criteria criteria = Criteria.where("disabled").is(false).and("status").is(2)
                .and("applicantRole").in(Arrays.asList(ActivityConfig.ROLE_AGENT, ActivityConfig.ROLE_TEACHER))
                .and("startTime").lt(today).and("endTime").gte(yesterday);
        return query(new Query(criteria));
    }

    /**
     * 进行中的活动 (包括一周内结束的)
     * 只放置时长布置的
     */
    public List<ActivityConfig> loadLatelyPassedActivityConfigBufferData() {
        Date today = DateUtils.getTodayStart();
        Date beforeWeek = DateUtils.addDays(today, -7);
        Criteria criteria = Criteria.where("disabled").is(false).and("status").is(2)
                .and("applicantRole").in(Arrays.asList(ActivityConfig.ROLE_AGENT))
                .and("startTime").lte(today).and("endTime").gte(beforeWeek);
        Sort sort = new Sort(Sort.Direction.DESC, "startTime");
        return query(new Query(criteria).with(sort));
    }

    public void updateEmail(String id, String email) {
        ActivityConfig activityConfig = load(id);
        if (activityConfig == null) {
            return;
        }
        activityConfig.setEmail(email);
        activityConfig.setUpdateTime(new Date());
        upsert(activityConfig);
    }

    public List<ActivityConfig> loadActivityConfigListByTypeAndDate(String activityType, Date startDate) {
        Date lastDate = DateUtils.getLastDayOfMonth(startDate);
        Criteria criteria = Criteria.where("disabled").is(false).and("status").is(2)
                .and("startTime").gte(startDate).and("startTime").lte(lastDate);
        if(StringUtils.isNotBlank(activityType)){
            criteria.and("type").is(activityType);
        }
        return query(new Query(criteria));
    }

    public void updateDisabledStatus(String activityId, Boolean disabled) {
        ActivityConfig load = load(activityId);
        load.setDisabled(disabled);
        upsert(load);
        activityConfigVersion.increment();
    }

    public void updateEndTime(String activityId, String yyyyMMdd) {
        try {
            String dateString = yyyyMMdd + " 23:59:59";
            Date date = DateUtils.parseDate(dateString, "yyyyMMdd HH:mm:ss");
            ActivityConfig load = load(activityId);
            load.setEndTime(date);
            upsert(load);
            activityConfigVersion.increment();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public List<ActivityConfig> loadAllActivityConfigIncludeIsEnd() {
        Criteria criteria = Criteria.where("disabled").is(false).and("status").is(2);
        return query(new Query(criteria));
    }

    @CacheMethod
    public Map<Long, List<ActivityConfig>> loadClassesActivity(@CacheParameter(value = "CID", multiple = true) Collection<Long> clazzIds) {
        Criteria criteria = Criteria.where("clazzIds").in(clazzIds)
                .and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "startTime");
        List<ActivityConfig> configs = query(new Query(criteria).with(sort).limit(500));
        Map<Long, List<ActivityConfig>> map = new HashMap<>();
        configs.forEach(c ->
            c.getClazzIds().forEach(cid -> {
                List<ActivityConfig> activityConfigs = map.get(cid);
                if (activityConfigs == null) {
                    activityConfigs = new ArrayList<>();
                }
                activityConfigs.add(c);
                map.put(cid, activityConfigs);
            })
        );
        return map;
    }
}
