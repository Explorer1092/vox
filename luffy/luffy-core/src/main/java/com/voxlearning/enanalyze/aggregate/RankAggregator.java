package com.voxlearning.enanalyze.aggregate;

import com.voxlearning.enanalyze.view.RankSentenceLikeRequest;
import com.voxlearning.enanalyze.view.RankSentenceLikeView;
import com.voxlearning.enanalyze.view.RankSentencePageView;
import com.voxlearning.enanalyze.view.RankSentenceView;
import com.voxlearning.utopia.enanalyze.model.PageInfo;

/**
 * 排行聚合服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface RankAggregator {

    /**
     * 获取某个人排行数据
     *
     * @param openGroupId 群id
     * @param openId      openid
     * @return 个人数据
     */
    RankSentenceView getRank(String openGroupId, String openId);

    /**
     * 获取某个群所有人的排行数据
     *
     * @param openGroupId 群id
     * @param fromOpenId  当前用户openId
     * @param pageInfo    分页数据
     * @return 一页的排行数据
     */
    RankSentencePageView queryGroupRank(String openGroupId, String fromOpenId, PageInfo pageInfo);

    /**
     * 点赞或者取消点赞
     *
     * @param request 点赞请求
     * @return 点赞结果
     */
    RankSentenceLikeView like(RankSentenceLikeRequest request);
}
