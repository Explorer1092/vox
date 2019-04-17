package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.06.14")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface VendorResgContentService {

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<VersionedVendorResgContentList> loadVersionedVendorResgContentList(long version);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<VendorResgContent>> loadAllVendorResgContentsFromDB();

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void reloadVendorResgContentBuffer();
}
