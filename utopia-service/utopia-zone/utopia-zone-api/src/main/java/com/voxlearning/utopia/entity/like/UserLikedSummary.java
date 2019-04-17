package com.voxlearning.utopia.entity.like;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentExpiration;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRevision;
import com.voxlearning.alps.annotation.dao.aerospike.DocumentNamespace;
import com.voxlearning.alps.annotation.dao.aerospike.DocumentSet;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 *
 * Created by alex on 2017/12/26.
 */
@Getter
@Setter
@DocumentConnection(configName = "utopiaex")
@DocumentNamespace(namespace = "vox")
@DocumentSet(setName = "UserLikedSummary")
public class UserLikedSummary implements Serializable {

    private static final long serialVersionUID = 7260494331860951419L;

    @DocumentId private String id;               // ComplexID(YYYYMM_userId)
    private Set<Long> likers;                    // 点赞人列表
    private Map<String, Integer> dailyCount;     // 每天被赞数汇总
    private Set<String> recordIds;               // 被赞记录ID
    @DocumentRevision
    private Integer revision;
    @DocumentExpiration
    private Date expiration;

    public static UserLikedSummary newInstance(Long userId, Date actionTime) {

        String key = generateId(userId, actionTime);

        UserLikedSummary instance = new UserLikedSummary();
        instance.setId(key);
        instance.setLikers(new HashSet<>());
        instance.setDailyCount(new HashMap<>());
        instance.setRecordIds(new HashSet<>());

        instance.setExpiration(MonthRange.current().next().getEndDate());

        return instance;
    }

    public void liked(Long liker, String recordId) {

        likers.add(liker);

        recordIds.add(recordId);

        String dailyKey = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        int count = 1;
        if (dailyCount.containsKey(dailyKey)) {
            count = dailyCount.get(dailyKey) + 1;
        }
        dailyCount.put(dailyKey, count);
    }

    public static String generateId(Long userId, Date actionTime) {
        if (userId == null || userId <= 0L || actionTime == null) {
            throw new IllegalArgumentException("user id and action time can not be null");
        }

        return StringUtils.join("ULS_", userId, "_", DateUtils.dateToString(actionTime, "yyyyMM"));
    }

}