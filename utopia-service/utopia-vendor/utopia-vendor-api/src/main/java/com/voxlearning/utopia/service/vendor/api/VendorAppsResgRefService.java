package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.06.15")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface VendorAppsResgRefService {

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<VendorAppsResgRef>> loadAllVendorAppsResgRefsFromBuffer();

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<VendorAppsResgRef>> findVendorAppsResgRefsByAppKeyFromBuffer(String appKey);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<VendorAppsResgRef>> loadAllVendorAppsResgRefsFromDB();

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void reloadVendorAppsResgRefBuffer();
}
