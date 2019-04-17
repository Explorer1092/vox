package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/9
 */
@Named("agent.CrmSchoolClueDao")
public class CrmSchoolClueDao extends StaticMongoDao<CrmSchoolClue, String> {

    @Override
    protected void calculateCacheDimensions(CrmSchoolClue source, Collection<String> dimensions) {
    }

    public List<CrmSchoolClue> findByTime(Date createTime, Integer authenticateType, Integer status) {
        Filter filter = filterBuilder.where("createTime").gte(createTime);
        filterIs(filter, "authenticateType", authenticateType);
        filterIs(filter, "infoStatus", status);
        filterIs(filter, "disabled", false);
        return findAll(filter);
    }

    public Map<Long, List<CrmSchoolClue>> findSchoolIdIs(Collection<Long> schoolIds) {
        Filter filter = filterBuilder.where("schoolId").in(schoolIds);
        filterIs(filter, "disabled", false);
        return findAll(filter).stream().collect(Collectors.groupingBy(CrmSchoolClue::getSchoolId, Collectors.toList()));
    }

    public List<CrmSchoolClue> findSchoolIdIs(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        filterIs(filter, "disabled", false);
        Sort sort = new Sort(Sort.Direction.DESC, "updateTime");
        return findAll(filter, sort);
    }

    public List<CrmSchoolClue> findSchoolIdIncludeDisabled(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        Sort sort = new Sort(Sort.Direction.DESC, "updateTime");
        return findAll(filter, sort);
    }

    public List<CrmSchoolClue> findSchoolIdIsAuth(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId);
        filterIs(filter, "status", 2);
        filterIs(filter, "disabled", false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return findAll(filter, sort);
    }

    public List<CrmSchoolClue> findMasterSchoolIdIs(Long masterSchoolId) {
        Filter filter = filterBuilder.where("masterSchoolId").is(masterSchoolId);
        filterIs(filter, "disabled", false);
        Sort sort = new Sort(Sort.Direction.ASC, "createTime");
        return findAll(filter, sort);
    }

    public List<CrmSchoolClue> findRecorderIdIs(Long recorderId) {
        Filter filter = filterBuilder.where("recorderId").is(recorderId);
        filterIs(filter, "disabled", false);
        return findAll(filter);
    }

    public List<CrmSchoolClue> findBySchoolId(Long schoolId) {
        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("status").ne(0);
        filterIs(filter, "disabled", false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return findAll(filter, sort);
    }

    public List<CrmSchoolClue> findAuthClues(Integer authStatus, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        Filter filter = filterBuilder.build();
        filterIs(filter, "status", authStatus);
        filterIs(filter, "authenticateType", 1);
        if (StringUtils.isNoneBlank(schoolName)) {
            filterRegex(filter, "schoolName", schoolName);
        }
        if (StringUtils.isNoneBlank(provinceName)) {
            filterRegex(filter, "provinceName", provinceName);
        }
        if (StringUtils.isNoneBlank(cityName)) {
            filterRegex(filter, "cityName", cityName);
        }
        if (StringUtils.isNoneBlank(recorderName)) {
            filterRegex(filter, "recorderName", recorderName);
        }
        if (StringUtils.isNotBlank(reviewerName)) {
            filterRegex(filter, "reviewerName", reviewerName);
        }
        smartFilter(filter, "createTime", createStart, createEnd);
        return findAll(filter);
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

    public List<CrmSchoolClue> findInfoClues(Integer authStatus, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        Filter filter = filterBuilder.build();
        filterIs(filter, "authenticateType", 2);
        filterIs(filter, "infoStatus", authStatus);
        if (StringUtils.isNoneBlank(schoolName)) {
            filterRegex(filter, "schoolName", schoolName);
        }
        if (StringUtils.isNoneBlank(provinceName)) {
            filterRegex(filter, "provinceName", provinceName);
        }
        if (StringUtils.isNoneBlank(cityName)) {
            filterRegex(filter, "cityName", cityName);
        }
        if (StringUtils.isNoneBlank(recorderName)) {
            filterRegex(filter, "recorderName", recorderName);
        }
        if (StringUtils.isNotBlank(reviewerName)) {
            filterRegex(filter, "reviewerName", reviewerName);
        }
        smartFilter(filter, "createTime", createStart, createEnd);
        return findAll(filter);
    }

    public List<CrmSchoolClue> findCriticalClues(Integer infoStatus, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        Filter filter = filterBuilder.build();
        filterIs(filter, "authenticateType", 3);
        filterIs(filter, "status", infoStatus);
        if (StringUtils.isNoneBlank(schoolName)) {
            filterRegex(filter, "schoolName", schoolName);
        }
        if (StringUtils.isNoneBlank(provinceName)) {
            filterRegex(filter, "provinceName", provinceName);
        }
        if (StringUtils.isNoneBlank(cityName)) {
            filterRegex(filter, "cityName", cityName);
        }
        if (StringUtils.isNoneBlank(recorderName)) {
            filterRegex(filter, "recorderName", recorderName);
        }
        if (StringUtils.isNotBlank(reviewerName)) {
            filterRegex(filter, "reviewerName", reviewerName);
        }
        smartFilter(filter, "createTime", createStart, createEnd);
        return findAll(filter);
    }

    public List<CrmSchoolClue> findSignInClues(Integer authStatus, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {

        Filter filter = filterBuilder.build();
        filterIs(filter, "authenticateType", 4);
        filterIs(filter, "infoStatus", authStatus);
        if (StringUtils.isNoneBlank(schoolName)) {
            filterRegex(filter, "schoolName", schoolName);
        }
        if (StringUtils.isNoneBlank(provinceName)) {
            filterRegex(filter, "provinceName", provinceName);
        }
        if (StringUtils.isNoneBlank(cityName)) {
            filterRegex(filter, "cityName", cityName);
        }
        if (StringUtils.isNoneBlank(recorderName)) {
            filterRegex(filter, "recorderName", recorderName);
        }
        if (StringUtils.isNotBlank(reviewerName)) {
            filterRegex(filter, "reviewerName", reviewerName);
        }
        smartFilter(filter, "createTime", createStart, createEnd);
        return findAll(filter);
    }

    public List<CrmSchoolClue> findAllStatus(Boolean authStates, Boolean schoolIdExists, String schoolName) {
        Filter filter = filterBuilder.build();
        filterExists(filter, "status", authStates);
        filterExists(filter, "infoStatus", authStates);
        filterExists(filter, "schoolId", schoolIdExists);
        filterRegex(filter, "schoolName", schoolName);
        filterIs(filter, "disabled", false);
        return findAll(filter);
    }

    public List<CrmSchoolClue> findSchoolClueByDate(Date startTime, Date endTime) {
        Filter m = filterBuilder.where("createTime").gte(startTime).lt(endTime);
        Filter f1 = filterBuilder.where("disabled").is(false);
        Filter f2 = filterBuilder.where("disabled").exists(false);
        Filter filter = filterBuilder.build().andOperator(m, filterBuilder.build().orOperator(f1, f2));
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return findAll(filter, sort);
    }

    private void filterExists(Filter filter, String key, Boolean exists) {
        if (exists != null) {
            filter.and(key).exists(exists);
        }
    }

    private void filterIs(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).is(value);
        }
    }

    private void filterRegex(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).regex(".*" + value + ".*");
        }
    }

    private List<CrmSchoolClue> findAll(Filter filter) {
        return __find_OTF(filter.toBsonDocument());
    }

    private List<CrmSchoolClue> findAll(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }

    private Page<CrmSchoolClue> find(Filter filter, Pageable pageable) {
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public List<CrmSchoolClue> findClueListBySchoolIdAndStatus(Long schoolId, Integer authStatus) {
        Filter f0 = filterBuilder.where("schoolId").is(schoolId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        List<CrmSchoolClue> list = findAll(f0, sort);
        if (CollectionUtils.isNotEmpty(list)) {
            CrmSchoolClue clue = list.get(0);
            Integer type = clue.getAuthenticateType();
            list = list.stream().filter(p -> p.getAuthenticateType() != null && ((p.getAuthenticateType() == 1 && p.getStatus() == authStatus) || (p.getAuthenticateType() == 2 && p.getInfoStatus() == authStatus))).collect(Collectors.toList());
        }
        return list;
    }
}
