package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.newhomework.api.entity.AncientPoetryStudentGlobalStar;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = AncientPoetryStudentGlobalStar.class)
public class AncientPoetryStudentGlobalStarPersistence extends StaticMySQLPersistence<AncientPoetryStudentGlobalStar, Long> {

    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getUtopiaSql("homework");
    }

    @Override
    protected void calculateCacheDimensions(AncientPoetryStudentGlobalStar document, Collection<String> dimensions) {
        dimensions.add(AncientPoetryStudentGlobalStar.ck_id(document.getId()));
    }

    public void addStarAndDuration(Long studentId, Double star, Long duration, Integer clazzLevel, Long schoolId, Integer regionId) {
        // star和duration都为空时不更新
        if ((star == null || star == 0D) && (duration == null || duration == 0L)) {
            return;
        }
        AncientPoetryStudentGlobalStar globalStar = load(studentId);
        if (globalStar == null) {
            globalStar = new AncientPoetryStudentGlobalStar();
            globalStar.setId(studentId);
            globalStar.setTotalStar(0D);
            globalStar.setTotalDuration(0L);
        }
        if (star != null && star != 0) {
            globalStar.setTotalStar(globalStar.getTotalStar() + star);
        }
        if (duration != null && duration != 0) {
            globalStar.setTotalDuration(globalStar.getTotalDuration() + duration);
        }
        if (clazzLevel != null) {
            globalStar.setClazzLevel(clazzLevel);
        }
        if (schoolId != null) {
            globalStar.setSchoolId(schoolId);
        }
        if (regionId != null) {
            globalStar.setRegionId(regionId);
        }
        upsert(globalStar);
    }

    public List<AncientPoetryStudentGlobalStar> loadBySchoolIdAndClazzLevel(Long schoolId, Integer clazzLevel, Integer limit) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId).and("CLAZZ_LEVEL").is(clazzLevel);
        Query query = new Query(criteria).resetSort();
        Sort sort = new Sort(Sort.Direction.DESC, "TOTAL_STAR").and(new Sort(Sort.Direction.ASC, "TOTAL_DURATION"));
        if (limit != null && limit != 0) {
            return query(query.with(sort).limit(limit));
        } else {
            return query(query.with(sort));
        }
    }

    public List<AncientPoetryStudentGlobalStar> loadByRegionIdAndClazzLevel(Integer regionId, Integer clazzLevel, Integer limit) {
        Criteria criteria = Criteria.where("REGION_ID").is(regionId).and("CLAZZ_LEVEL").is(clazzLevel);
        Query query = new Query(criteria).resetSort();
        Sort sort = new Sort(Sort.Direction.DESC, "TOTAL_STAR").and(new Sort(Sort.Direction.ASC, "TOTAL_DURATION"));
        if (limit != null && limit != 0) {
            return query(query.with(sort).limit(limit));
        } else {
            return query(query.with(sort));
        }
    }

    public List<Long> loadAllSchoolIds() {
        String sql = "SELECT DISTINCT SCHOOL_ID FROM VOX_ANCIENT_POETRY_STUDENT_GLOBAL_STAR";
        List<Long> schoolIds = new ArrayList<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSql.withSql(sql).queryAll((rs, rowNum) -> schoolIds.add(rs.getLong("SCHOOL_ID"))))
                .execute();
        return schoolIds;
    }

    public List<Integer> loadAllRegionIds() {
        String sql = "SELECT DISTINCT REGION_ID FROM VOX_ANCIENT_POETRY_STUDENT_GLOBAL_STAR";
        List<Integer> regionIds = new ArrayList<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSql.withSql(sql).queryAll((rs, rowNum) -> regionIds.add(rs.getInt("REGION_ID"))))
                .execute();
        return regionIds;
    }
}
