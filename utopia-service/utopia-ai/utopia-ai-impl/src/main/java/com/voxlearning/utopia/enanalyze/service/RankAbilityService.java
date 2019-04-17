package com.voxlearning.utopia.enanalyze.service;

import com.voxlearning.utopia.enanalyze.model.ArticleCompositeAbility;

/**
 * 排名服务 - 作文综合能力
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
public interface RankAbilityService {

    /**
     * 更新
     *
     * @param userId  用户id
     * @param ability 能力
     */
    void update(String userId, ArticleCompositeAbility ability);

    /**
     * 获取排名
     *
     * @param userId 用户id
     * @return 排名
     */
    long get(String userId);
}
