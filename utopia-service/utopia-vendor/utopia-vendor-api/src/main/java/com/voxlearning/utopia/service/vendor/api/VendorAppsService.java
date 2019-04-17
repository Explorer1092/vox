package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.06.15")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface VendorAppsService {

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<VersionedVendorAppsList> loadVersionedVendorAppsList(long version);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<VendorApps>> loadAllVendorAppsFromDB();

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void reloadVendorAppsBuffer();
}
