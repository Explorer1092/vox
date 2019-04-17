package com.voxlearning.utopia.service.parent.homework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业与学生关系表
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-09
 */
@Named
@CacheBean(type = HomeworkUserRef.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
@Slf4j
public class HomeworkUserRefDao extends AlpsStaticMongoDao<HomeworkUserRef, String> {

    //缓存、分页最大数
    private static final int MAX = 300;
    public static final int CACHE_SECONDS = 86400;

    /**
     * 缓存维度
     *
     * @param document
     * @param dimensions
     */
    @Override
    protected void calculateCacheDimensions(HomeworkUserRef document, Collection<String> dimensions) {
        dimensions.add(document.ckUserId());
        dimensions.add("parenthw.hurs_l_" + document.ckUserId());
    }

    /**
     * 查询当天用户与作业关系
     *
     * @param userId 用户id
     * @return 用户与作业关系列表
     */
    @CacheMethod
    public List<HomeworkUserRef> loadHomeworkUserRef(@CacheParameter("userId") Long userId){
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("createTime").gte(DayRange.current().getStartDate());
        return query(Query.query(criteria));
    }

    /**
     * 下翻页查询指定用户、时间的作业id
     *
     * @param userId 用户id
     * @param start 开始条数
     * @param size 每页大小
     * @param startTime 开始时间
     * @return 用户与作业关系列表
     */
    public List<String> loadHomeworkIdsDown(Long userId, Integer start, Integer size, Date startTime){

        boolean cached = DateUtils.isSameDay(startTime, new Date()) && start + size <= MAX;
        //from cache
        String cacheKey = "parenthw.hurs_l_" + userId;
        if(cached){
            List<String> chids = getCache().load(cacheKey);
            if(!CollectionUtils.isEmpty(chids) && chids.size() >= start + size){
                LoggerUtils.info("~loadHomeworkIdsDown",userId, start, size, chids.size());
                return new ArrayList<>(chids.subList(start, start + size));
            }
        }

        //from DB
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("createTime").lte(startTime);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        List<String> result = query(Query.query(criteria).with(sort).skip(start).limit(size)).stream().map(HomeworkUserRef::getHomeworkId).collect(Collectors.toList());
        LoggerUtils.info("+loadHomeworkIdsDown", userId, start, size);

        //set cache
        if(cached){
            if(!ObjectUtils.anyBlank(result)){
                getCache().set(cacheKey, CACHE_SECONDS, result);
            }
        }
        result = result.subList(start, Math.min(start + size, result.size()));

        return result;
    }

    /**
     * 查询用户最近的用户与作业关系
     *
     * @param userId 用户id
     * @return 用户与作业关系列表
     */
    public HomeworkUserRef last(Long userId){
        Criteria criteria = Criteria.where("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        List<HomeworkUserRef> result = query(Query.query(criteria).with(sort).limit(1));
        return ObjectUtils.get(()->result.get(0));
    }

    /**
     * 根据学生id和时间查询布置过的题
     * @param userId 学生id
     * @param time 时间
     * @return 作业学生关系
     */
    public Collection<HomeworkUserRef> lastTime(Long userId, Date time){
        Criteria criteria = Criteria.where("userId").is(userId).and("createTime").gte(time);
        return query(Query.query(criteria));
    }

}
