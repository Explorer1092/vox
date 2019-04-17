package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsContestant;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsUser;

import java.util.concurrent.TimeUnit;

/**
 * 华杯赛 Loader
 * Created by haitian.gan on 2017/2/15.
 */
@ServiceVersion(version = "20170321")
@ServiceTimeout(timeout = 30,unit = TimeUnit.SECONDS)
@ServiceRetries
public interface HbsLoader {

    HbsUser loadUser(Long userId);

    HbsUser loadUserByName(String userName);

    // 不走缓存的实现，因为华杯赛维护成绩功能没办法清缓存
    HbsContestant getContestant(Long studentId);

    HbsContestant loadContestant(Long studentId);

    HbsContestant findStudentByIdCardNo(String idCardNo);

    HbsContestant findStudentByPhoneNumber(String phoneNubmer);
}
