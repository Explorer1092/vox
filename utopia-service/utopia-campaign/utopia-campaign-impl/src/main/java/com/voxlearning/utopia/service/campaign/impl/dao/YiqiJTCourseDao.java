package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;
import com.voxlearning.utopia.service.campaign.api.mapper.YiqiJTCourseMapper;
import com.voxlearning.utopia.service.campaign.impl.internal.filter.CourseQueryFilter;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.campaign.api.TeacherActivityService.YQJT_ALL_RAW_CACHE_KEY;

@Named
public class YiqiJTCourseDao extends AlpsStaticJdbcDao<YiqiJTCourse,Long>{

    /**
     * 修改缓存维度时要小心下面两个方法
     * @see com.voxlearning.utopia.service.campaign.impl.dao.YiqiJTCourseDao#incrReadCount(java.lang.Long, java.lang.Long)
     * @see com.voxlearning.utopia.service.campaign.impl.dao.YiqiJTCourseDao#incrCollectCount(java.lang.Long, java.lang.Long)
     */
    @Override
    protected void calculateCacheDimensions(YiqiJTCourse document, Collection<String> dimensions) {
        dimensions.add(YiqiJTCourse.ck_all());
    }

    @Inject
    private CampaignCacheSystem campaignCacheSystem;

    @CacheMethod(key = "ALL")
    public List<YiqiJTCourse> loadAll() {
        return query();
    }

    public int updateAttendNum(long courseId) {

        Criteria criteria = Criteria.where("ID").is(courseId);

        Update update = new Update();
        update.inc("ATTEND_NUM", 1);

        int affectRows = (int)$update(update,criteria);
        if(affectRows > 0){
            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(null, keys);
            getCache().delete(keys);

            YiqiJTCourse load = load(courseId);
            campaignCacheSystem.CBS.flushable.<List<YiqiJTCourseMapper>>createCacheValueModifier()
                    .key(YQJT_ALL_RAW_CACHE_KEY)
                    .expiration(0)
                    .modifier(rawList -> {
                        for (YiqiJTCourseMapper item : rawList) {
                            if (Objects.equals(item.getId(), courseId)) {
                                item.setAttendNum(load.getAttendNum());
                            }
                        }
                        return rawList;
                    })
                    .execute();
        }

        return affectRows;
    }

    public int updateCourseTopNum(long courseId, int topNum) {
        Criteria criteria = Criteria.where("ID").is(courseId);

        Update update = new Update();
        update.inc("TOP_NUM", topNum);

        int affectRows = (int)$update(update,criteria);
        if(affectRows > 0){
            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(null, keys);
            getCache().delete(keys);
        }

        return affectRows;
    }

    public int updateCourseStatus(long courseId, int status) {
        Criteria criteria = Criteria.where("ID").is(courseId);

        Update update = new Update();
        update.set("STATUS", status);

        int affectRows = (int)$update(update,criteria);
        if(affectRows > 0){
            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(null, keys);
            getCache().delete(keys);
        }

        return affectRows;
    }

    public List<YiqiJTCourse> select17JTCourseList(CourseQueryFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }

        Criteria criteria = null;
        boolean isWhere = true;
        if (StringUtils.isNotBlank(filter.getTitle())) {
            criteria = Criteria.where("TITLE").is(filter.getTitle());
            isWhere = false;
        }

        if (filter.isGradeNotEmpty()) {
            if (isWhere) {
                criteria = Criteria.where("GRADE").in(filter.getGrade());
            } else {
                criteria.and("GRADE").in(filter.getGrade());
            }
        }

        if (filter.isSubjectNotEmpty()) {
            if (isWhere) {
                criteria = Criteria.where("SUBJECT").in(filter.getSubject());
            } else {
                criteria.and("SUBJECT").in(filter.getSubject());
            }
        }

        if (criteria == null) {
            return null;
        }

        return query(Query.query(criteria));
    }

    public void incrReadCount(Long id, Long incrCount) {
        Criteria criteria = Criteria.where("ID").is(id);

        Update update = new Update();
        update.inc("READ_COUNT", incrCount);

        int affectRows = (int) $update(update, criteria);
        if (affectRows > 0) {
            updateCache(id);
        }
    }

    public void incrCollectCount(Long id, Long incrCount) {
        Criteria criteria = Criteria.where("ID").is(id);

        Update update = new Update();
        update.inc("COLLECT_COUNT", incrCount);

        int affectRows = (int) $update(update, criteria);
        if (affectRows > 0) {
            updateCache(id);
        }
    }

    /**
     * TODO 之前遗留问题走的全量 cache, 量上来后要改掉
     */
    private void updateCache(Long id) {
        String cacheKey = YiqiJTCourse.ck_all();
        YiqiJTCourse course = $load(id);
        campaignCacheSystem.CBS.flushable.<List<YiqiJTCourse>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(yiqiJTCourses -> {
                    for (YiqiJTCourse item : yiqiJTCourses) {
                        if (Objects.equals(item.getId(), id)) {
                            item.setReadCount(course.getReadCount());
                            item.setCollectCount(course.getCollectCount());
                        }
                    }
                    return yiqiJTCourses;
                })
                .execute();

        campaignCacheSystem.CBS.flushable.<List<YiqiJTCourseMapper>>createCacheValueModifier()
                .key(YQJT_ALL_RAW_CACHE_KEY)
                .expiration(0)
                .modifier(rawList -> {
                    for (YiqiJTCourseMapper item : rawList) {
                        if (Objects.equals(item.getId(), id)) {
                            item.setReadCount(course.getReadCount());
                            item.setCollectCount(course.getCollectCount());
                        }
                    }
                    return rawList;
                })
                .execute();
    }

}
