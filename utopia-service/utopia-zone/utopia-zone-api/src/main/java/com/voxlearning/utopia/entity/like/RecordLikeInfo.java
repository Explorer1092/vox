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
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
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
@DocumentSet(setName = "RecordLikeInfo")
public class RecordLikeInfo implements Serializable {

    private static final long serialVersionUID = 7210494331860952429L;

    @DocumentId private String id;               // ComplexID(YYYYMM_userId)
    private Integer likedCount;                  // 被赞数量
    private Set<String> likerNames;              // 点赞人姓名列表
    private Map<Long, Date> likeTime;            // 最近一次的点赞时间
    @DocumentRevision
    private Integer revision;
    @DocumentExpiration
    private Date expiration;

    public static RecordLikeInfo newInstance(UserLikeType likeType, String recordId) {

        String key = generateId(likeType, recordId);

        RecordLikeInfo instance = new RecordLikeInfo();
        instance.setId(key);
        instance.setLikedCount(0);
        instance.setLikerNames(new HashSet<>());
        instance.setLikeTime(new HashMap<>());

        int duration = 7;
        if (likeType.getDuration() != null) {
            duration = likeType.getDuration();
        }

        instance.setExpiration(DateUtils.addDays(new Date(), duration));

        return instance;
    }

    public void liked(Long likerId, String likerName, Date actionTime) {
        likedCount++;
        likerNames.add(likerName);
        likeTime.put(likerId, actionTime);
    }

    public boolean hasLiked(Long userId) {
        return likeTime != null && likeTime.containsKey(userId);
    }

    public static String generateId(UserLikeType likeType, String recordId) {
        if (likeType == null || StringUtils.isBlank(recordId)) {
            throw new IllegalArgumentException("record id and like type can not be null");
        }

        return StringUtils.join("CSL_", likeType.name(), "_", recordId);
    }

    public static String generateAttendanceId(Date date, Long clazzId, Long userId) {
        if (date == null || clazzId == null || userId == null) {
            throw new IllegalArgumentException("date , clazz id and user id can not be null");
        }

        String curMonth = DateUtils.dateToString(date, "yyyyMM");
        return StringUtils.join(curMonth, "_", clazzId, "_", userId);
    }

    public static String generateAchievementId(Long userId, String achievementType, Integer level) {
        if (userId == null || StringUtils.isBlank(achievementType) || level == null) {
            throw new IllegalArgumentException("user id , achievement type and level can not be null");
        }

        return StringUtils.join(userId, "_", achievementType, "_", level);
    }

}