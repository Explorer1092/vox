package com.voxlearning.utopia.service.vendor.api;

/**
 * 小U售卖页，购买信息展示用。
 *
 * @Author: wei.jiang
 * @Date: Created on 2018/3/9
 */

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.vendor.api.mapper.XiaoUOrderInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2018.03.09")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface XiaoUOrderInfoService {

    List<XiaoUOrderInfo> getXiaoUOrderInfoList();

}
