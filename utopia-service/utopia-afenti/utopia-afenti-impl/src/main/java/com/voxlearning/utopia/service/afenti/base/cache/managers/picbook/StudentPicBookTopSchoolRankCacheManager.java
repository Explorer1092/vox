package com.voxlearning.utopia.service.afenti.base.cache.managers.picbook;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import com.voxlearning.utopia.service.afenti.api.data.PicBookRankInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2018/4/8
 */
@UtopiaCachePrefix(prefix = "PICBOOK_STUDENT_SCHOOL_TOP10_RANK")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 14)
public class StudentPicBookTopSchoolRankCacheManager extends PojoCacheObject<StudentPicBookTopSchoolRankCacheManager.GeneratorKey, Map<Long, PicBookRankInfo>> {

    public StudentPicBookTopSchoolRankCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public List<PicBookRankInfo> fetchStudentRankList(Long schoolId, PicBookRankCategory rankType, Integer weekRange) {
        if (rankType == null || schoolId == null || weekRange == null || schoolId == 0)
            return Collections.emptyList();
        try {
            Map<Long, PicBookRankInfo> maps = load(new StudentPicBookTopSchoolRankCacheManager.GeneratorKey(rankType, weekRange, schoolId));
            if (maps == null)
                return Collections.emptyList();
            return new ArrayList<>(maps.values());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Integer getStudentRank(Long schoolId, PicBookRankCategory rankType, Long studentId, Integer weekRange) {
        if (rankType == null || studentId == null || schoolId == null || schoolId == 0 || weekRange == null || weekRange == 0)
            return 0;
        try {
            Map<Long, PicBookRankInfo> maps = load(new StudentPicBookTopSchoolRankCacheManager.GeneratorKey(rankType, weekRange, schoolId));
            if (MapUtils.isEmpty(maps) || !maps.containsKey(studentId))
                return 0;

            List<PicBookRankInfo> rankList = new ArrayList<>();
            if (rankType == PicBookRankCategory.READ) {
                rankList = maps.values().stream()
                        .filter(r -> r.getReadCount() != null && r.getReadCount() != 0)
                        .sorted(Comparator.comparing(PicBookRankInfo::getReadCount).reversed())
                        .collect(Collectors.toList());
            } else if (rankType == PicBookRankCategory.WORD) {
                rankList = maps.values().stream()
                        .filter(r -> r.getWordCount() != null && r.getWordCount() != 0)
                        .sorted(Comparator.comparing(PicBookRankInfo::getWordCount).reversed())
                        .collect(Collectors.toList());
            }
            // 将名次加入学生列表
            int rankIndex = 0;
            int rankCount = 0;
            long tempScoreTotal = -1;
            for (PicBookRankInfo info : rankList) {
                rankCount++;
                Integer value = rankType == PicBookRankCategory.READ ? info.getReadCount() : info.getWordCount();
                if (tempScoreTotal != value) {
                    // 总数不同时名次增加，反之名次并列
                    tempScoreTotal = value;
                    rankIndex = rankCount;
                }
                if (info.getStudentId().equals(studentId)) break;
            }
            return rankIndex;
        } catch (Exception e) {
            return 0;
        }
    }

    public void updateStudentToRank(Long schoolId, PicBookRankCategory rankType, PicBookRankInfo rankInfo, Integer weekRange) {
        if (rankType == null || rankInfo == null || rankInfo.getStudentId() == null || schoolId == null || schoolId == 0
                || weekRange == null || weekRange == 0)
            return;

        String key = cacheKey(new StudentPicBookTopSchoolRankCacheManager.GeneratorKey(rankType, weekRange, schoolId));
        CacheObject<Map<Long, PicBookRankInfo>> cacheObject = getCache().get(key);
        if (null != cacheObject.getValue()) {
            cache.cas(key, expirationInSeconds(), cacheObject, 3, currentValue -> {
                currentValue = new HashMap<>(currentValue);
                // 如果前20名存在则直接替换
                if (currentValue.containsKey(rankInfo.getStudentId())) {
                    currentValue.put(rankInfo.getStudentId(), rankInfo);
                } else {
                    // 如果不存在，当超过20长度时,删除元素为新元素腾出空间(防止有并列产生,保存到150人)
                    List<PicBookRankInfo> rankList = new ArrayList<>(currentValue.values());
                    if (rankList.size() >= 20) {
                        if (rankType == PicBookRankCategory.READ) {
                            rankList = rankList.stream()
                                    .filter(r -> r.getReadCount() != null)
                                    .sorted((r1, r2) -> (
                                            r2.getReadCount()).compareTo(r1.getReadCount()))
                                    .collect(Collectors.toList());
                        } else if (rankType == PicBookRankCategory.WORD) {
                            rankList = rankList.stream()
                                    .filter(r -> r.getWordCount() != null)
                                    .sorted((r1, r2) -> (
                                            r2.getWordCount()).compareTo(r1.getWordCount()))
                                    .collect(Collectors.toList());
                        }

                        while (rankList.size() >= 20)
                            rankList.remove(rankList.size() - 1);
                    }
                    rankList.add(rankInfo);

                    currentValue = rankList.stream().collect(Collectors.toMap(PicBookRankInfo::getStudentId, t -> t));
                }
                return currentValue;
            });
        } else {
            Map<Long, PicBookRankInfo> newStudentInfoMap = new HashMap<>();
            newStudentInfoMap.put(rankInfo.getStudentId(), rankInfo);
            cache.add(key, expirationInSeconds(), newStudentInfoMap);
        }
    }

    public void updateAllRank(Long schoolId, PicBookRankCategory rankType, List<PicBookRankInfo> rankInfoList, Integer weekRange) {
        if (rankType == null || CollectionUtils.isEmpty(rankInfoList) || schoolId == null || schoolId == 0
                || weekRange == null || weekRange == 0)
            return;

        String key = cacheKey(new StudentPicBookTopSchoolRankCacheManager.GeneratorKey(rankType, weekRange, schoolId));
        Map<Long, PicBookRankInfo> rankMap = rankInfoList.stream().collect(Collectors.toMap(PicBookRankInfo::getStudentId, t -> t));
        cache.set(key, expirationInSeconds(), rankMap);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"rankType", "weekRange", "schoolId"})
    public class GeneratorKey {
        private PicBookRankCategory rankType;
        private Integer weekRange;
        private Long schoolId;

        @Override
        public String toString() {
            return "RT=" + rankType + ":WR=" + weekRange + ":S=" + schoolId;
        }
    }
}
