package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.vendor.api.ParentBabyEagleChinaCultureService;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;

import javax.inject.Named;
import java.util.*;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/16
 */
@Named
@Service(interfaceClass = ParentBabyEagleChinaCultureService.class)
@ExposeService(interfaceClass = ParentBabyEagleChinaCultureService.class)
public class ParentBabyEagleChinaCultureServiceImpl implements ParentBabyEagleChinaCultureService {

    private static final String SHARE_RECORD_CACHE_KEY = "SHARE_RECORD_CACHE_KEY_";

    @Override
    public Map<Long, List<String>> getShareRecordsByStudentIds(Collection<Long> studentIds) {
        Map<String, Long> cacheMap = new HashMap<>();
        studentIds.forEach(e -> cacheMap.put(generateCacheKey(e), e));
        Map<String, CacheObject<List<String>>> cacheObjectMap = VendorCache.getVendorPersistenceCache().gets(cacheMap.keySet());
        Map<Long, List<String>> returnMap = new HashMap<>();
        cacheMap.forEach((k, v) -> {
            if (cacheObjectMap.get(k) != null && CollectionUtils.isNotEmpty(cacheObjectMap.get(k).getValue())) {
                returnMap.put(v, cacheObjectMap.get(k).getValue());
            }
        });
        return returnMap;
    }

    @Override
    public void insertShareRecordByStudentIds(Long studentId, String courseId) {
        CacheObject<List<String>> cacheObject = VendorCache.getVendorPersistenceCache().get(generateCacheKey(studentId));
        List<String> shareRecords = cacheObject.getValue();
        if (CollectionUtils.isEmpty(shareRecords)) {
            shareRecords = new ArrayList<>();
        }
        shareRecords.add(courseId);
        VendorCache.getVendorPersistenceCache().set(generateCacheKey(studentId), 0, shareRecords);
    }


    private String generateCacheKey(Long studentId) {
        String cacheKey = SHARE_RECORD_CACHE_KEY + studentId;
        return CacheKeyGenerator.generateCacheKey(ParentBabyEagleChinaCultureServiceImpl.class, cacheKey);
    }
}
