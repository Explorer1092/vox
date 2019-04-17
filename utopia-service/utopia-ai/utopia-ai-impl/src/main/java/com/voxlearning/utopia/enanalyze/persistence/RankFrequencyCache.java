package com.voxlearning.utopia.enanalyze.persistence;

/**
 * 排行服务 - 批改频率
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
public interface RankFrequencyCache {

    /**
     * 更新
     *
     * @param openId    用户id
     * @param frequency 频率
     */
    void update(String openId, double frequency);

    /**
     * 获取排名
     *
     * @param openId 用户id
     * @return 排名
     */
    Long get(String openId);

    /**
     * 获取总数
     *
     * @return 总数
     */
    Long count();
}
