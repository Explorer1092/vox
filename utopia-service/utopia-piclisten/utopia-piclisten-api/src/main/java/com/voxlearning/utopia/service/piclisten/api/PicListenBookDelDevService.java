package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.piclisten.api.entity.PicListenBookUserDev;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/1/12
 * 处理点读机设备限制问题
 */
@ServiceRetries
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface PicListenBookDelDevService {
    /**
     * 人教--查询用户设备数量
     */
    List<PicListenBookUserDev.DevInfo> queryPicDevList(Long userId);

    /**
     * 人教--移除用户设备
     */
    MapMessage delPicDev(Long userId, List<String> devIdList);

}
