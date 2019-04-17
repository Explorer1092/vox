package com.voxlearning.utopia.enanalyze.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * 群服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface GroupService {

    /**
     * 获取群列表
     *
     * @return 群列表
     */
    MapMessage list(String openId);

    /**
     * 删除某个用户与群的关系
     *
     * @param openId      用户id
     * @param openGroupId 群id
     * @return 结果
     */
    MapMessage remove(String openId, String openGroupId);
}
