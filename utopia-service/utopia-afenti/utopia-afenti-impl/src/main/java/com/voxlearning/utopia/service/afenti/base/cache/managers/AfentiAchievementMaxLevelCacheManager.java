package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author peng.zhang.a
 * @since 17-3-21
 */
public class AfentiAchievementMaxLevelCacheManager extends PojoCacheObject<AfentiAchievementMaxLevelCacheManager.GenerateKey, Set<Long>> {


    public AfentiAchievementMaxLevelCacheManager(UtopiaCache cache) {
        super(cache);
    }


    /**
     * 批量增加数据
     */
    public boolean addRecord(StudentDetail student, Subject subject, AchievementType achievementType, Map<Integer, Set<Long>> userIds) {
        int clazzLevel = student.getClazz().getClazzLevel().getLevel();
        Long schoolId = student.getClazz().getSchoolId();
        String jie = student.getClazz().getJie();
        Long clazzId = student.getClazzId();

        for (Integer level = 1; level <= achievementType.levelNum; level++) {
            set(new GenerateKey(schoolId, clazzId, subject, clazzLevel, level, achievementType, jie), userIds.getOrDefault(level, Collections.emptySet()));
        }
        return true;

    }

    public boolean addRecord(StudentDetail student, Subject subject, Integer level, AchievementType achievementType) {
        int clazzLevel = student.getClazz().getClazzLevel().getLevel();
        Long schoolId = student.getClazz().getSchoolId();
        String jie = student.getClazz().getJie();
        Long clazzId = student.getClazzId();

        String cacheKey = cacheKey(new GenerateKey(schoolId, clazzId, subject, clazzLevel, level, achievementType, jie));

        CacheObject<Set<Long>> cacheObject = cache.get(cacheKey);
        if (cacheObject == null) return false;

        if (cacheObject.getValue() == null) {
            Set<Long> mid = new HashSet<>();
            mid.add(student.getId());
            getCache().add(cacheKey, expirationInSeconds(), mid);
        } else {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(student.getId());
                return currentValue;
            });
        }
        return true;
    }

    /**
     * 是否已经标记计算
     */
    public boolean isRecord(StudentDetail student, Subject subject) {
        int clazzLevel = student.getClazz().getClazzLevel().getLevel();
        Long schoolId = student.getClazz().getSchoolId();
        String jie = student.getClazz().getJie();
        Long clazzId = student.getClazzId();
        Integer level = 1;
        String key = cacheKey(new GenerateKey(schoolId, clazzId, subject, clazzLevel, level, null, jie));
        key = "recordFlag:" + key;
        return getCache().add(key, expirationInSeconds(), true);
    }

    public boolean removeRecord(StudentDetail student, Subject subject, Integer level, AchievementType achievementType) {
        int clazzLevel = student.getClazz().getClazzLevel().getLevel();
        Long schoolId = student.getClazz().getSchoolId();
        String jie = student.getClazz().getJie();
        Long clazzId = student.getClazzId();

        String cacheKey = cacheKey(new GenerateKey(schoolId, clazzId, subject, clazzLevel, level, achievementType, jie));
        CacheObject<Set<Long>> cacheObject = cache.get(cacheKey);
        if (cacheObject == null) return false;
        if (cacheObject.getValue() == null) {
            return false;
        } else {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.remove(student.getId());
                return currentValue;
            });
        }
        return true;
    }

    //缓存一年
    public int expirationInSeconds() {
        return (int) (DateUtils.DAY_TIME_LENGTH_IN_MILLIS / 1000) * 366;

    }

    public Set<Long> fetchRecordByLevel(Long schoolId, Long clazzId, Subject subject, String jie,
                                        int clazzLevel, int level, AchievementType type) {

        Set<Long> mid = load(new GenerateKey(schoolId, clazzId, subject, clazzLevel, level, type, jie));
        return mid == null ? Collections.emptySet() : mid;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"schoolId", "clazzId", "subject", "clazzLevel", "level", "type", "jie"})
    class GenerateKey {
        private Long schoolId;
        private Long clazzId;
        private Subject subject;
        private Integer clazzLevel;
        private Integer level;
        private AchievementType type;
        private String jie;

        @Override
        public String toString() {
            return "SID=" + schoolId + ",CID=" + clazzId
                    + ",ST=" + subject + ",CL=" + clazzLevel
                    + ",L" + level + ",T=" + type + ",J=" + jie;
        }
    }

}
