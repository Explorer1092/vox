package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;

import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Named
@CacheBean(type = VacationHomeworkPackage.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class VacationHomeworkPackageDao extends StaticMongoShardPersistence<VacationHomeworkPackage, String> {

    @Override
    protected void calculateCacheDimensions(VacationHomeworkPackage document, Collection<String> dimensions) {
        dimensions.add(VacationHomeworkPackage.ck_id(document.getId()));
        dimensions.add(VacationHomeworkPackage.ck_clazzGroupId(document.getClazzGroupId()));
        dimensions.add(VacationHomeworkPackage.ck_teacherId(document.getTeacherId()));
    }

    @CacheMethod
    public Map<Long, List<VacationHomeworkPackage.Location>> loadVacationHomeworkPackageByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        query.field().includes("_id", "teacherId", "clazzGroupId", "createAt", "plannedDays", "bookId",
                "startTime", "endTime", "actionId", "subject");

        Map<Long, List<VacationHomeworkPackage.Location>> ret = query(query).stream()
                .map(VacationHomeworkPackage::toLocation)
                .collect(Collectors.groupingBy(VacationHomeworkPackage.Location::getClazzGroupId));
        return groupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    /**
     * 假期作业list历史(包含被删除的)
     * 仅供CRM使用！！！
     * @param groupIds
     * @return
     */
    public Map<Long, List<VacationHomeworkPackage.Location>> loadAllVacationHomeworkPackageByClazzGroupIds(Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds);
        Query query = Query.query(criteria);
        query.field().includes("_id", "teacherId", "clazzGroupId", "createAt", "plannedDays", "bookId",
                "startTime", "endTime", "actionId", "subject", "disabled");

        Map<Long, List<VacationHomeworkPackage.Location>> ret = query(query).stream()
                .map(VacationHomeworkPackage::toLocation)
                .collect(Collectors.groupingBy(VacationHomeworkPackage.Location::getClazzGroupId));
        return groupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    /**
     * 删除操作
     *
     * @param packageId 作业包id
     * @return Boolean
     */
    public Boolean updateDisabledTrue(String packageId) {
        if (StringUtils.isBlank(packageId)) {
            return null;
        }

        VacationHomeworkPackage vacationHomeworkPackage = load(packageId);
        if (vacationHomeworkPackage == null) {
            return Boolean.FALSE;
        }
        vacationHomeworkPackage.setDisabled(true);
        return upsert(vacationHomeworkPackage) != null;
    }

    /**
     * 恢复假期作业
     * @param packageId
     * @return
     */
    public Boolean resumeVacationHomework(String packageId) {
        if (StringUtils.isBlank(packageId)) {
            return null;
        }

        VacationHomeworkPackage vacationHomeworkPackage = load(packageId);
        if (vacationHomeworkPackage == null) {
            return Boolean.FALSE;
        }
        vacationHomeworkPackage.setDisabled(false);
        return upsert(vacationHomeworkPackage) != null;
    }
}
