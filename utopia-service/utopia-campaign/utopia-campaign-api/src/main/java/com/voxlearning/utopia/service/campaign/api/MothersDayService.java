package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.02.20")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MothersDayService {

    @Async
    AlpsFuture<MapMessage> getMothersDayCard(User student, Boolean dataIncluded);

    @Async
    AlpsFuture<MapMessage> giveMothersDayCardAsGift(User student, String image, String voice);

    @Async
    AlpsFuture<MapMessage> shareMothersDayCard(Long studentId);

    @Async
    AlpsFuture<Boolean> updateMothersDayCardSended(Long studentId);
}
