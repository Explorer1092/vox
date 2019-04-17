package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.vendor.api.XiaoUOrderInfoService;
import com.voxlearning.utopia.service.vendor.api.mapper.XiaoUOrderInfo;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/9
 */
@Named
@Service(interfaceClass = XiaoUOrderInfoService.class)
@ExposeService(interfaceClass = XiaoUOrderInfoService.class)
@SuppressWarnings("unchecked")
public class XiaoUOrderInfoServiceImpl implements XiaoUOrderInfoService {

    private static final String XIAO_U_ORDER_INFO_LIST_CACHE_KEY = "XIAO_U_ORDER_INFO_LIST";


    @Override
    public List<XiaoUOrderInfo> getXiaoUOrderInfoList() {
        CacheObject<Object> objectCacheObject = VendorCache.getVendorPersistenceCache().get(generateCacheKey());
        return (List<XiaoUOrderInfo>) objectCacheObject.getValue();
    }


    public void addXiaoUOrderInfo(Long userId, String orderProductType) {
        CacheObject<Object> objectCacheObject = VendorCache.getVendorPersistenceCache().get(generateCacheKey());
        List<XiaoUOrderInfo> uOrderInfos = (List<XiaoUOrderInfo>) objectCacheObject.getValue();
        if (CollectionUtils.isEmpty(uOrderInfos) || uOrderInfos.size() < 10) {
            XiaoUOrderInfo xiaoUOrderInfo = new XiaoUOrderInfo();
            xiaoUOrderInfo.setUserId(userId);
            xiaoUOrderInfo.setOrderProductServiceType(orderProductType);
            if (CollectionUtils.isEmpty(uOrderInfos)) {
                uOrderInfos = new ArrayList<>();
            }
            uOrderInfos.add(0, xiaoUOrderInfo);
            VendorCache.getVendorPersistenceCache().set(generateCacheKey(), 10 * 86400, uOrderInfos);
        }
    }


    private String generateCacheKey() {
        return CacheKeyGenerator.generateCacheKey(XiaoUOrderInfoServiceImpl.class, XIAO_U_ORDER_INFO_LIST_CACHE_KEY);
    }
}
