package com.voxlearning.utopia.enanalyze.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * 排名服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface SentenceRankService {

    /**
     * 获取某个群中某个人的排名
     *
     * @param openGroupId 群
     * @param openId      openid
     * @return
     */
    MapMessage getRank(String openGroupId, String openId);

    /**
     * 获取某个群所有人的排名
     *
     * @param openGroupId 群id
     * @param openId      openid
     * @return 结果
     */
    MapMessage getRanks(String openGroupId, String openId);

}
