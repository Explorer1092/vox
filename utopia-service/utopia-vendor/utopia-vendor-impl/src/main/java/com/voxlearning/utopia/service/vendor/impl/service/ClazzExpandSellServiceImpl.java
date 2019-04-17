package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.user.api.StudentLoader;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.ClazzExpandSellService;
import com.voxlearning.utopia.service.vendor.api.mapper.ClazzExpandSellInfoMapper;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/14
 */
@Named
@ExposeService(interfaceClass = ClazzExpandSellService.class)
public class ClazzExpandSellServiceImpl implements ClazzExpandSellService {


    private static final List<OrderProductServiceType> expandClazzList = Arrays.asList(MathGarden, WordBuilder, ChinesePilot, EncyclopediaChallenge, AnimalLand, ScienceLand, DinosaurLand);
    private static final String CacheKeyPrefix = "CLAZZ_EXPAND_CACHE_KEY_";
    @ImportService(interfaceClass = StudentLoader.class)
    private StudentLoader studentLoader;


    @Override
    public List<ClazzExpandSellInfoMapper> getUsedStudentList(Long clazzId) {
        List<ClazzExpandSellInfoMapper> returnList = new ArrayList<>();
        for (OrderProductServiceType orderProductServiceType : getExpandClazzList()) {
            CacheObject<ClazzExpandSellInfoMapper> objectCacheObject = VendorCache.getVendorPersistenceCache().get(generateCachekey(clazzId, orderProductServiceType.name()));
            if (objectCacheObject.getValue() != null) {
                returnList.add(objectCacheObject.getValue());
            }
        }
        return returnList;
    }


    public void addUsedStudentOrderInfo(Long studentId, String orderProductServiceType) {
        StudentDetail studentDetail = studentLoader.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return;
        }
        CacheObject<ClazzExpandSellInfoMapper> objectCacheObject = VendorCache.getVendorPersistenceCache().get(generateCachekey(studentDetail.getClazzId(), orderProductServiceType));
        ClazzExpandSellInfoMapper sellInfoMapper = objectCacheObject.getValue();
        String studentName = studentDetail.fetchRealnameIfBlankId();
        if (sellInfoMapper == null) {
            sellInfoMapper = new ClazzExpandSellInfoMapper();
            List<String> studentNameList = new ArrayList<>();
            studentNameList.add(studentName);
            sellInfoMapper.setStudentName(studentNameList);
            sellInfoMapper.setOrderProductType(orderProductServiceType);
        } else {
            List<String> studentNameList = sellInfoMapper.getStudentName();
            if (studentNameList.size() < 2) {
                studentNameList.add(studentName);
            } else {
                studentNameList.remove(0);
                studentNameList.add(studentName);
            }
            sellInfoMapper.setStudentName(studentNameList);
        }
        VendorCache.getVendorPersistenceCache().set(generateCachekey(studentDetail.getClazzId(), orderProductServiceType), 10 * 86400, sellInfoMapper);
    }

    @Override
    public List<OrderProductServiceType> getExpandClazzList() {
        return expandClazzList;
    }


    private String generateCachekey(Long clazzId, String orderProductServiceType) {
        return CacheKeyGenerator.generateCacheKey(CacheKeyPrefix, new String[]{"clazzId", "orderProductServiceType"}, new Object[]{clazzId, orderProductServiceType});
    }
}
