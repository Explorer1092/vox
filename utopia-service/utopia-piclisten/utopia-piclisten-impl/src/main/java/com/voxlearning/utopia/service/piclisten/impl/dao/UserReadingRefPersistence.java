package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.vendor.api.entity.UserReadingRef;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2016/12/21
 */
@Named
@CacheBean(type = UserReadingRef.class)
public class UserReadingRefPersistence extends StaticCacheDimensionDocumentJdbcDao<UserReadingRef, Long>{
    @CacheMethod
    public Map<Long, List<UserReadingRef>> getUserReadingRefsByUserIds(@CacheParameter(value = "UID", multiple = true) Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("USER_ID").in(userIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(UserReadingRef::getUserId));
    }

    @CacheMethod
    public List<UserReadingRef> getUserReadingRefsByUserId(@CacheParameter(value = "UID") Long userId) {
        if (userId == 0L) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria)).stream().collect(Collectors.toList());
    }

    public MapMessage deleteReadingRefsByUserIdAndReadingIds(Long userId, Collection<String> pictureBookIds) {
        if (userId == 0 || CollectionUtils.isEmpty(pictureBookIds)) {
            return MapMessage.errorMessage("用户id或者绘本id出错");
        }
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("PICTURE_BOOK_ID").in(pictureBookIds);
        long deleteCount = executeDelete(criteria, getTableName());
        if (deleteCount > 0) {
            UserReadingRef mock = new UserReadingRef();
            mock.setUserId(userId);
            List<String> cacheKeys = Arrays.asList(mock.generateCacheDimensions());
            getCache().delete(cacheKeys);
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("删除失败");
        }
    }

    public MapMessage upsertUserReadingRef(UserReadingRef userReadingRef) {
        String sql = "INSERT INTO VOX_USER_READING_REF(PICTURE_BOOK_ID, USER_ID, SELF_STUDY_TYPE, CREATE_DATETIME, " +
                "UPDATE_DATETIME,FINISH_STATUS,READ_SECONDS) " +
                "VALUES(:pictureBookId,:userId,:selfStudyType,NOW(),NOW(),:finishStatus,:readSeconds) " +
                "ON DUPLICATE KEY UPDATE UPDATE_DATETIME = NOW()";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("pictureBookId", userReadingRef.getPictureBookId())
                .addValue("userId", userReadingRef.getUserId())
                .addValue("selfStudyType", userReadingRef.getSelfStudyType().name())
                .addValue("finishStatus", userReadingRef.getFinishStatus())
                .addValue("readSeconds", userReadingRef.getReadSeconds());
        if(Objects.equals(userReadingRef.getFinishStatus(),1)){
            sql =  "INSERT INTO VOX_USER_READING_REF(PICTURE_BOOK_ID, USER_ID, SELF_STUDY_TYPE, CREATE_DATETIME, " +
                    "UPDATE_DATETIME,READ_FINISH_TIME,FINISH_STATUS,READ_SECONDS) " +
                    "VALUES(:pictureBookId,:userId,:selfStudyType,NOW(),NOW(),:readFinishTime,:finishStatus,:readSeconds) " +
                    "ON DUPLICATE KEY UPDATE UPDATE_DATETIME = NOW(),READ_FINISH_TIME = NOW()," +
                    "FINISH_STATUS=1,READ_SECONDS=:readSeconds";
            parameterSource.addValue("readFinishTime",userReadingRef.getReadFinishTime());
        }

       long rows = getNamedParameterJdbcTemplate().update(sql, parameterSource);
       if (rows > 0) {
           List<String> cacheKeys = Arrays.asList(userReadingRef.generateCacheDimensions());
           getCache().delete(cacheKeys);
           return MapMessage.successMessage();
       } else {
           return MapMessage.errorMessage("更新数据失败");
       }
    }
}

