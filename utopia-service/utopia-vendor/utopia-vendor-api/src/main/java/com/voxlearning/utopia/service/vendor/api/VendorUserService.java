package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@ServiceVersion(version = "20190201")
public interface VendorUserService {

    void saveVendorUsageStat(String appKey, String yearMonth, Long totalNum);

    void saveVendorUserRef(String appKey, Long childId, List<String> productIdList);

    boolean deleteVendorUserRef(Long childId);
}
