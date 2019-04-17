package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.vendor.api.DPLivecastActivityService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-11-29 下午5:48
 **/
@Slf4j
@Named
@ExposeService(interfaceClass = DPLivecastActivityService.class)
public class DPLiveCastActivityServiceImpl implements DPLivecastActivityService {


    protected final Logger logger = LoggerFactory.getLogger(getClass());
    

    @Override
    public MapMessage studentAddLotteryChance(Long studentId, String subject) {
        return MapMessage.successMessage();
    }
}
