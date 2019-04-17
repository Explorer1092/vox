package com.voxlearning.utopia.enanalyze.facade.support;

import com.google.common.collect.Maps;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.facade.SentencePersistenceFacade;
import com.voxlearning.utopia.enanalyze.persistence.SentenceCache;
import com.voxlearning.utopia.enanalyze.persistence.SentenceLikeCache;
import com.voxlearning.utopia.enanalyze.persistence.SentenceRankCache;
import com.voxlearning.utopia.enanalyze.persistence.UserGroupDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 句子持久化门面实现类
 *
 * @author xiaolei.li
 * @version 2018/7/22
 */
@Service
public class SentencePersistenceFacadeImpl implements SentencePersistenceFacade {

    @Resource
    SentenceRankCache sentenceRankCache;

    @Resource
    SentenceCache sentenceCache;

    @Resource
    SentenceLikeCache sentenceLikeCache;

    @Resource
    UserGroupDao userGroupDao;

    @Override
    public void update(Sentence sentence) {
        // 更新句子排行榜
        SentenceRankCache.Element el = new SentenceRankCache.Element();
        el.setOpenGroupIds(sentence.getOpenGroupIds());
        el.setOpenId(sentence.getOpenId());
        el.setScore(sentence.getSentenceScore());
        sentenceRankCache.update(el);
        // 更新句子
        SentenceCache.Sentence _e = new SentenceCache.Sentence();
        _e.setArticleId(sentence.getArticleId());
        _e.setOpenId(sentence.getOpenId());
        _e.setText(sentence.getSentence());
        _e.setScore(sentence.getSentenceScore());
        sentenceCache.update(_e);
    }

    @Override
    public Result get(String openGroupId, String openId) {
        // 获取句子
        SentenceCache.Sentence s = sentenceCache.get(openId);
        // 获取排行
        SentenceRankCache.Rank rank = sentenceRankCache.getRank(openGroupId, openId);
        // 获取点赞
        long likes = sentenceLikeCache.getLikes(openGroupId, openId);
        // 组装结果
        Result result = new Result();
        result.setOpenGroupId(openGroupId);
        result.setOpenId(s.getOpenId());
        result.setArticleId(s.getArticleId());
        result.setSentence(s.getText());
        result.setSentenceScore(s.getScore());
        result.setGroupRank(rank.getRank());
        result.setGroupLikes(likes);
        return result;
    }

    @Override
    public List<Result> list(String openGroupId, String fromOpenId) {
        // 获取当前群下的所有成员
        List<UserGroupEntity> users = userGroupDao.findByGroupId(openGroupId);
        // 获取当前群下所有人的句子排行
        List<SentenceRankCache.Rank> ranks = sentenceRankCache.getRanks(openGroupId);
        // 获取这些人的好句子
        List<String> openIds = users.stream().map(UserGroupEntity::getOpenId)
                .collect(Collectors.toList());
        List<SentenceCache.Sentence> sentences = sentenceCache.list(openIds);
        // 获取这些人的点赞数
        Map<String, Long> likeMap = sentenceLikeCache.queryLikes(openGroupId, openIds);
        // 构建句子映射，格式:(openId => Sentence)
        Map<String, SentenceCache.Sentence> sentenceMap = Maps.newHashMap();
        sentences.stream().forEach(i -> sentenceMap.put(i.getOpenId(), i));
        // 组装
        return ranks.stream()
                .sorted(Comparator.comparing(SentenceRankCache.Rank::getRank).reversed())
                .map(rank -> {
                    final String _openId = rank.getOpenId();
                    Result result = new Result();
                    result.setOpenId(_openId);
                    result.setOpenGroupId(rank.getOpenGroupId());
                    result.setGroupRank(rank.getRank());
                    if (sentenceMap.containsKey(_openId)) {
                        SentenceCache.Sentence sentence = sentenceMap.get(_openId);
                        result.setSentenceScore(sentence.getScore());
                        result.setSentence(sentence.getText());
                        result.setArticleId(sentence.getArticleId());
                    }
                    if (likeMap.containsKey(_openId))
                        result.setGroupLikes(likeMap.get(_openId));
                    return result;
                }).collect(Collectors.toList());
    }
}
