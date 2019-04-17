package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.vendor.api.VendorUserService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUsageStat;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUserRef;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorUsageStatPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorUserRefPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@Service(interfaceClass = VendorUserService.class)
@ExposeService(interfaceClass = VendorUserService.class)
public class VendorUserServiceImpl implements VendorUserService {

    @Inject
    private VendorUserRefPersistence vendorUserRefPersistence;
    @Inject
    private VendorUsageStatPersistence vendorUsageStatPersistence;

    @Override
    public void saveVendorUsageStat(String appKey, String yearMonth, Long totalNum) {
        VendorUsageStat usageStat = new VendorUsageStat();
        usageStat.setAppKey(appKey);
        usageStat.setYearMonth(yearMonth);
        usageStat.setTotalNum(totalNum);
        vendorUsageStatPersistence.$insert(usageStat);
    }

    @Override
    public void saveVendorUserRef(String appKey, Long childId, List<String> productIdList) {

        List<VendorUserRef> list = new ArrayList<>();

        productIdList.stream().forEach(productId -> {
            VendorUserRef vendorUserRef = new VendorUserRef();
            vendorUserRef.setAppKey(appKey);
            vendorUserRef.setUserId(childId);
            vendorUserRef.setProductId(productId);
            list.add(vendorUserRef);
        });

        vendorUserRefPersistence.$inserts(list);
    }

    @Override
    public boolean deleteVendorUserRef(Long childId) {

        return vendorUserRefPersistence.disableByChildId(childId);

    }
}
