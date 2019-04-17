package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.zone.api.UserGiftService;
import com.voxlearning.utopia.service.zone.impl.support.WeekReceiveGiftCountCacheManager;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExposeService(interfaceClass = UserGiftService.class)
public class UserGiftServiceImpl implements UserGiftService {

    @Inject private WeekReceiveGiftCountCacheManager weekReceiveGiftCountCacheManager;

    @Override
    public AlpsFuture<Integer> getCurrentWeekReceivedGiftCount(Long userId) {
        if (userId == null) {
            return new ValueWrapperFuture<>(0);
        }
        int count = weekReceiveGiftCountCacheManager.currentCount(userId);
        return new ValueWrapperFuture<>(count);
    }
}
