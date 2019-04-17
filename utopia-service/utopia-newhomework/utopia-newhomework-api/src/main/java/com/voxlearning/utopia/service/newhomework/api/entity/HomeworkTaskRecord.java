package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * @author guoqiang.li
 * @since 2017/4/19
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "homework_task_record")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20170419")
public class HomeworkTaskRecord implements Serializable {
    private static final long serialVersionUID = 702620919864727965L;

    @DocumentId
    private String id;
    private Long teacherId;
    private String taskPeriod;
    private Integer taskId;
    private Integer integralCount;
    private HomeworkTaskType taskType;
    private HomeworkTaskStatus taskStatus;

    /**
     * 日常作业：key -> day + "|" + groupId
     * 周末作业: key -> groupId
     */
    private LinkedHashMap<String, Boolean> details;           // 详情

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(HomeworkTaskRecord.class, id);
    }

    public static String generateId(Long teacherId, Integer taskId, HomeworkTaskType taskType, String taskPeriod) {
        return StringUtils.formatMessage("{}-{}-{}-{}", teacherId, taskId, taskType, taskPeriod);
    }

    public Map<Integer, List<Long>> findDailyDetails() {
        if (HomeworkTaskType.DAILY_HOMEWORK != taskType || MapUtils.isEmpty(details)) {
            return Collections.emptyMap();
        }
        Map<Integer, List<Long>> dailyDetails = new LinkedHashMap<>();
        details.keySet().forEach(dayGroup -> {
            String[] strArray = StringUtils.split(dayGroup, "|");
            if (strArray.length == 2) {
                int day = SafeConverter.toInt(strArray[0]);
                long groupId = SafeConverter.toLong(strArray[1]);
                dailyDetails.computeIfAbsent(day, k -> new ArrayList<>()).add(groupId);
            }
        });
        return dailyDetails;
    }

    public Map<Long, Boolean> findWeekendDetails() {
        if (HomeworkTaskType.WEEKEND_HOMEWORK != taskType || MapUtils.isEmpty(details)) {
            return Collections.emptyMap();
        }
        Map<Long, Boolean> weekendDetails = new LinkedHashMap<>();
        details.forEach((group, finished) -> weekendDetails.put(SafeConverter.toLong(group), finished));
        return weekendDetails;
    }

    public Map<Long, Boolean> findActivityDetails() {
        if (HomeworkTaskType.ACTIVITY_HOMEWORK != taskType || MapUtils.isEmpty(details)) {
            return Collections.emptyMap();
        }
        Map<Long, Boolean> activityDetails = new LinkedHashMap<>();
        details.forEach((group, finished) -> activityDetails.put(SafeConverter.toLong(group), finished));
        return activityDetails;
    }
}
