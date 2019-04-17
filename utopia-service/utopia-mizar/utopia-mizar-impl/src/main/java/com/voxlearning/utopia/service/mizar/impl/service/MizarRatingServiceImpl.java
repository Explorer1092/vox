package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;
import com.voxlearning.utopia.service.mizar.api.service.MizarRatingService;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.MizarRatingDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2016/9/19.
 */
@Named
@Service(interfaceClass = MizarRatingService.class)
@ExposeService(interfaceClass = MizarRatingService.class)
public class MizarRatingServiceImpl extends SpringContainerSupport implements MizarRatingService {

    @Inject private MizarRatingDao mizarRatingDao;

    @Override
    public MapMessage updateRatingStatus(String rating, MizarRatingStatus status) {
        mizarRatingDao.updateStatus(rating, status);
        return MapMessage.successMessage();
    }
}
