package com.voxlearning.utopia.service.vendor.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.vendor.api.VendorAppsResgRefService;
import lombok.Getter;

public class VendorAppsResgRefServiceClient {

    @Getter
    @ImportService(interfaceClass = VendorAppsResgRefService.class)
    private VendorAppsResgRefService vendorAppsResgRefService;
}
