package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.vendor.api.VendorUserLoader;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUserRef;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorUsageStatPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorUserRefPersistence;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named
@Service(interfaceClass = VendorUserLoader.class)
@ExposeService(interfaceClass = VendorUserLoader.class)
public class VendorUserLoaderImpl implements VendorUserLoader {

    @Inject
    private VendorUserRefPersistence vendorUserRefPersistence;
    @Inject
    private VendorUsageStatPersistence vendorUsageStatPersistence;

    @Override
    public Long queryEffectiveUser(String appKey, String date) {

        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(date)) {
            return 0L;
        }

        return vendorUsageStatPersistence.loadEffectiveUser(appKey, date);
    }

    @Override
    public List<VendorUserRef> countEffectiveUser(String appkey, Long minUserId, int limit) {

        if (StringUtils.isEmpty(appkey) || limit > 1000) {
            return Collections.emptyList();
        }

        return vendorUserRefPersistence.loadVendorUserRefList(appkey,minUserId,limit);
    }

}
