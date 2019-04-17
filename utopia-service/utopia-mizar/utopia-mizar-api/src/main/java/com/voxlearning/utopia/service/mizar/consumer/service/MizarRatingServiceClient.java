package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;
import com.voxlearning.utopia.service.mizar.api.service.MizarRatingService;
import lombok.Getter;

/**
 * Created by Yuehcen.Wang on 16/9/6.
 */
public class MizarRatingServiceClient {

    @Getter
    @ImportService(interfaceClass = MizarRatingService.class)
    private MizarRatingService remoteReference;

    public MapMessage updateRatingStatus(String ratingId, MizarRatingStatus status) {
        if (StringUtils.isBlank(ratingId) || status == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.updateRatingStatus(ratingId, status);
    }

}
