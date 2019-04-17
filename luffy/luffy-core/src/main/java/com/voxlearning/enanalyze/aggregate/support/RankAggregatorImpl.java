package com.voxlearning.enanalyze.aggregate.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.MessageFetcher;
import com.voxlearning.enanalyze.aggregate.RankAggregator;
import com.voxlearning.enanalyze.view.RankSentenceLikeRequest;
import com.voxlearning.enanalyze.view.RankSentenceLikeView;
import com.voxlearning.enanalyze.view.RankSentencePageView;
import com.voxlearning.enanalyze.view.RankSentenceView;
import com.voxlearning.utopia.enanalyze.api.SentenceLikeService;
import com.voxlearning.utopia.enanalyze.api.SentenceRankService;
import com.voxlearning.utopia.enanalyze.model.PageInfo;
import com.voxlearning.utopia.enanalyze.model.SentenceRankResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 排名聚合服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Service
public class RankAggregatorImpl implements RankAggregator {

    @ImportService(interfaceClass = SentenceRankService.class)
    @ServiceVersion(version = "20180701")
    private SentenceRankService rankSentenceService;

    @ImportService(interfaceClass = SentenceLikeService.class)
    @ServiceVersion(version = "20180701")
    private SentenceLikeService sentenceLikeService;

    @Override
    public RankSentenceView getRank(String openGroupId, String openId) {
        MapMessage msg = rankSentenceService.getRank(openGroupId, openId);
        SentenceRankResult rankResult = MessageFetcher.get(msg, SentenceRankResult.class);
        return RankSentenceView.Builder.build(rankResult);
    }

    @Override
    public RankSentencePageView queryGroupRank(String openGroupId, String fromOpenId, PageInfo pageInfo) {
        // 请求服务
        MapMessage message = rankSentenceService.getRanks(openGroupId, fromOpenId);
        ArrayList<SentenceRankResult> list = MessageFetcher.get(message, ArrayList.class);
        // 组装结果
        return RankSentencePageView.Builder.build(list, pageInfo);
    }

    @Override
    public RankSentenceLikeView like(RankSentenceLikeRequest request) {
        SentenceLikeService.Params params = new SentenceLikeService.Params();
        params.setOpenGroupId(request.getOpenGroupId());
        params.setFromOpenId(request.getFromOpenId());
        params.setToOpenId(request.getToOpenId());
        MapMessage message = sentenceLikeService.like(params);
        SentenceLikeService.Result result = MessageFetcher.get(message, SentenceLikeService.Result.class);
        return RankSentenceLikeView.Builder.build(result);
    }
}
