package com.voxlearning.utopia.service.piclisten.api;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "2018-09-26")
public interface PicListenCommonService {


    Boolean userIsAuthForPicListen(User user);


    Map<String, DayRange> parentBuyBookPicListenLastDayMap(Long parentId, boolean keepExpired);

    Map<String,DayRange> parentBuyWalkManLastDayMap(Long parentId,boolean keepExpired);

    DayRange parentBuyScoreLastDay(Long parentId);


    Boolean parentHasBuyScore(Long parentId);

    Map<String, PicListenBookPayInfo> userBuyBookPicListenLastDayMap(User user, boolean keepExpired);

    Long loadPicListenPurchaseCount();

}
