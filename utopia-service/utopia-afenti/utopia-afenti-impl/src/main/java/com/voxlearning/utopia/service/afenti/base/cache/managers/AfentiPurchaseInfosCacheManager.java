package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiInfoAimedType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.api.data.ClassmatePurchaseInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-11-30
 * 同班同学购买信息
 */
@UtopiaCacheRevision(value = "20161201")
public class AfentiPurchaseInfosCacheManager extends PojoCacheObject<AfentiPurchaseInfosCacheManager.GenerateKey, List<ClassmatePurchaseInfo>> {


    private final Integer MAX_LIST_SIZE = 15;
    private final Integer DAY_RANGE = 60;

    public AfentiPurchaseInfosCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addRecord(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate) {
        if (studentDetail == null || studentDetail.getClazz() == null || studentDetail.getClazz().getSchoolId() == 0) {
            return;
        }
        ClassmatePurchaseInfo classmatePurchaseInfo = new ClassmatePurchaseInfo();
        classmatePurchaseInfo.setUserId(studentDetail.getId());
        classmatePurchaseInfo.setRealName(studentDetail.fetchRealname());
        classmatePurchaseInfo.setPurchaseType(purchaseType);
        classmatePurchaseInfo.setImgUrl(studentDetail.fetchImageUrl());
        classmatePurchaseInfo.setCreateDate(createDate);

        addRecord(new GenerateKey(studentDetail.getClazzId(), AfentiInfoAimedType.CLAZZ), classmatePurchaseInfo);
        addRecord(new GenerateKey(studentDetail.getClazz().getSchoolId(), AfentiInfoAimedType.SCHOOL), classmatePurchaseInfo);
    }

    public List<Map<String, Object>> getRecords(StudentDetail studentDetail) {
        List<ClassmatePurchaseInfo> clazzRecords = getClazzRecords(studentDetail);
        Set<Long> classRecordUserIds = clazzRecords.stream()
                .filter(p -> p.getCreateDate() != null)
                .filter(p -> DateUtils.dayDiff(new Date(), p.getCreateDate()) <= DAY_RANGE)
                .filter(p -> !Objects.equals(p.getUserId(), studentDetail.getId()))
                .map(ClassmatePurchaseInfo::getUserId).collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (ClassmatePurchaseInfo classmatePurchaseInfo : clazzRecords) {
            Map<String, Object> mid = new HashMap<>();
            mid.put("imgUrl", classmatePurchaseInfo.getImgUrl());
            mid.put("userId", classmatePurchaseInfo.getUserId());
            mid.put("userName", classmatePurchaseInfo.getRealName());
            mid.put("purchaseType", classmatePurchaseInfo.getPurchaseType());
            mid.put("aimedType", AfentiInfoAimedType.CLAZZ);
            result.add(mid);
        }
        //不够数量从学校开通列表中补齐
        if (CollectionUtils.isEmpty(clazzRecords) || clazzRecords.size() < MAX_LIST_SIZE) {
            int maxSize = MAX_LIST_SIZE - clazzRecords.size();
            getSchoolRecords(studentDetail).stream()
                    .filter(p -> !classRecordUserIds.contains(p.getUserId()))
                    .filter(p -> p.getCreateDate() != null)
                    .filter(p -> DateUtils.dayDiff(new Date(), p.getCreateDate()) <= DAY_RANGE)
                    .filter(p -> !Objects.equals(p.getUserId(), studentDetail.getId()))
                    .limit(maxSize)
                    .collect(Collectors.toList())
                    .forEach(classmatePurchaseInfo -> {
                                Map<String, Object> mid = new HashMap<>();
                                mid.put("imgUrl", classmatePurchaseInfo.getImgUrl());
                                mid.put("userId", classmatePurchaseInfo.getUserId());
                                mid.put("userName", classmatePurchaseInfo.getRealName());
                                mid.put("purchaseType", classmatePurchaseInfo.getPurchaseType());
                                mid.put("aimedType", AfentiInfoAimedType.SCHOOL);
                                result.add(mid);
                            }
                    );
        }
        return result;
    }

    private List<ClassmatePurchaseInfo> getClazzRecords(StudentDetail studentDetail) {
        if (studentDetail == null || studentDetail.getClazz() == null || studentDetail.getClazz().getSchoolId() == 0) {
            return Collections.emptyList();
        }
        List<ClassmatePurchaseInfo> ret = load(new GenerateKey(studentDetail.getClazzId(), AfentiInfoAimedType.CLAZZ));
        if (ret == null) {
            return Collections.emptyList();
        }
        return ret;
    }

    private List<ClassmatePurchaseInfo> getSchoolRecords(StudentDetail studentDetail) {
        if (studentDetail == null || studentDetail.getClazz() == null || studentDetail.getClazz().getSchoolId() == 0) {
            return Collections.emptyList();
        }
        List<ClassmatePurchaseInfo> ret = load(new GenerateKey(studentDetail.getClazz().getSchoolId(), AfentiInfoAimedType.SCHOOL));
        if (ret == null) {
            return Collections.emptyList();
        }
        return ret;
    }

    private void addRecord(GenerateKey generateKey, ClassmatePurchaseInfo classmatePurchaseInfo) {
        String cacheKey = cacheKey(generateKey);
        CacheObject<List<ClassmatePurchaseInfo>> cacheObject = getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() == null) {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singletonList(classmatePurchaseInfo));
        } else if (cacheObject != null) {
            //丢失数据不影响，所以并发情况下可以直接set
            List<ClassmatePurchaseInfo> currentValue = cacheObject.getValue();
            ClassmatePurchaseInfo mid = currentValue.stream()
                    .filter(p -> Objects.equals(p.getUserId(), classmatePurchaseInfo.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (mid != null) {
                currentValue.remove(mid);
            }
            if (currentValue.size() >= MAX_LIST_SIZE) {
                currentValue.remove(currentValue.size() - 1);
            }
            currentValue.add(0, classmatePurchaseInfo);
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
