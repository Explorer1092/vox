package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.GiftService;
import lombok.Getter;

public class GiftServiceClient {

    @Getter
    @ImportService(interfaceClass = GiftService.class)
    private GiftService giftService;
}
