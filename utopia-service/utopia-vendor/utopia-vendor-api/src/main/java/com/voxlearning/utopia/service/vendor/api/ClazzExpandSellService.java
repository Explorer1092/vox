package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.api.mapper.ClazzExpandSellInfoMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/14
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzExpandSellService {

    List<ClazzExpandSellInfoMapper> getUsedStudentList(Long clazzId);


    List<OrderProductServiceType> getExpandClazzList();
}
