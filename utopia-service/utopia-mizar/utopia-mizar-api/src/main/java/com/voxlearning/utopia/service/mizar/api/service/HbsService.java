package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * Created by ganhaitian on 2017/2/15.
 */
@ServiceVersion(version = "20170215")
@ServiceTimeout(timeout = 30,unit = TimeUnit.SECONDS)
@ServiceRetries
public interface HbsService {

    MapMessage updateUserPhoneNumber(Long userId,String phoneNumber);

    MapMessage updateUserPwd(Long userId,String password);
}
