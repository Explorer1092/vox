package com.voxlearning.utopia.enanalyze.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 * 作文分数排名服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface ArticleRankService {

    /**
     * 根据作文分数获取全局排行
     *
     * @return
     */
    MapMessage getRank(String openId);

//    /**
//     * 获取组排名
//     *
//     * @param openId
//     * @return
//     */
//    MapMessage getGroupRank(String openId);
}
