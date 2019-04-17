package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiInfoAimedType;
import com.voxlearning.utopia.service.afenti.api.data.ClassmateRewardInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-11-30
 * 阿分体做题提交页面 显示同班同校奖励信息
 * 需要过滤掉自己的信息
 */
public class AfentiRewardInfosCacheManager extends PojoCacheObject<AfentiRewardInfosCacheManager.GenerateKey, List<ClassmateRewardInfo>> {

    final Integer MAX_LIST_SIZE = 30;

    public AfentiRewardInfosCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addRecord(StudentDetail studentDetail, Integer integral) {
        if (studentDetail == null || studentDetail.getClazz() == null || studentDetail.getClazz().getSchoolId() == 0) {
            return;
        }
        ClassmateRewardInfo classmateRewardInfo = new ClassmateRewardInfo();
        classmateRewardInfo.setUserId(studentDetail.getId());
        classmateRewardInfo.setRealName(studentDetail.fetchRealname());
        classmateRewardInfo.setIntegralNum(integral);
        classmateRewardInfo.setImgUrl(studentDetail.fetchImageUrl());

        addRecord(new GenerateKey(studentDetail.getClazzId(), AfentiInfoAimedType.CLAZZ), classmateRewardInfo);
        addRecord(new GenerateKey(studentDetail.getClazz().getSchoolId(), AfentiInfoAimedType.SCHOOL), classmateRewardInfo);
    }

    private List<ClassmateRewardInfo> loadInfos(Long id, AfentiInfoAimedType afentiInfoAimedType) {
        List<ClassmateRewardInfo> ret = load(new GenerateKey(id, afentiInfoAimedType));
        if (ret == null) {
            return Collections.emptyList();
        }
        return ret;
    }

    public List<Map<String, Object>> getRecords(StudentDetail studentDetail) {
        if (studentDetail == null || studentDetail.getClazz() == null || studentDetail.getClazz().getSchoolId() == 0) {
            return Collections.emptyList();
        }

        List<ClassmateRewardInfo> clazzRecords = loadInfos(studentDetail.getClazzId(), AfentiInfoAimedType.CLAZZ)
                .stream()
                .filter(p -> !Objects.equals(p.getUserId(), studentDetail.getId()))
                .collect(Collectors.toList());

        Set<Long> classRecordUserIds = clazzRecords.stream().map(ClassmateRewardInfo::getUserId).collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (ClassmateRewardInfo classmateRewardInfo : clazzRecords) {
            Map<String, Object> mid = new HashMap<>();
            mid.put("imgUrl", classmateRewardInfo.getImgUrl());
            mid.put("userId", classmateRewardInfo.getUserId());
            mid.put("userName", classmateRewardInfo.getRealName());
            mid.put("integralNum", classmateRewardInfo.getIntegralNum());
            mid.put("aimedType", AfentiInfoAimedType.CLAZZ);
            result.add(mid);
        }
        if (CollectionUtils.isEmpty(clazzRecords) || clazzRecords.size() < MAX_LIST_SIZE) {
            int maxSize = MAX_LIST_SIZE - clazzRecords.size();
            loadInfos(studentDetail.getClazz().getSchoolId(), AfentiInfoAimedType.SCHOOL)
                    .stream()
                    .filter(p -> !classRecordUserIds.contains(p.getUserId()))
                    .filter(p -> !Objects.equals(p.getUserId(), studentDetail.getId()))
                    .limit(maxSize)
                    .collect(Collectors.toList())
                    .forEach(rewardInfo -> {
                        Map<String, Object> mid = new HashMap<>();
                        mid.put("imgUrl", rewardInfo.getImgUrl());
                        mid.put("userId", rewardInfo.getUserId());
                        mid.put("userName", rewardInfo.getRealName());
                        mid.put("integralNum", rewardInfo.getIntegralNum());
                        mid.put("aimedType", AfentiInfoAimedType.SCHOOL);
                        result.add(mid);
                    });

        }
        return result;
    }

    private void addRecord(GenerateKey generateKey, ClassmateRewardInfo classmateRewardInfo) {
        String cacheKey = cacheKey(generateKey);
        CacheObject<List<ClassmateRewardInfo>> cacheObject = getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() == null) {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singletonList(classmateRewardInfo));
        } else if (cacheObject != null) {
            //丢失数据不影响，所以并发情况下可以直接set
            List<ClassmateRewardInfo> currentValue = cacheObject.getValue();
            ClassmateRewardInfo mid = currentValue.stream()
                    .filter(p -> Objects.equals(p.getUserId(), classmateRewardInfo.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (mid != null) {
                currentValue.remove(mid);
            }
            if (currentValue.size() >= MAX_LIST_SIZE) {
                currentValue.remove(currentValue.size() - 1);
            }
            currentValue.add(0, classmateRewardInfo);
            getCache().set(cacheKey, expirationInSeconds(), currentValue);
        }
    }

    @Override
    public int expirationInSeconds() {
        return 0; // 不过期
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"id", "type"})
    class GenerateKey {
        private Long id;
        private AfentiInfoAimedType type;

        @Override
        public String toString() {
            return "ID=" + id + ",AIAT=" + type;
        }
    }
}
