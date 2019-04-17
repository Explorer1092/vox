package com.voxlearning.utopia.enanalyze.mq.consumer;

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.enanalyze.assemble.AINLPClient;
import com.voxlearning.utopia.enanalyze.entity.ArticleEntity;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.facade.SentencePersistenceFacade;
import com.voxlearning.utopia.enanalyze.mq.ConsumerLabel;
import com.voxlearning.utopia.enanalyze.mq.MessageConsumer;
import com.voxlearning.utopia.enanalyze.mq.Topic;
import com.voxlearning.utopia.enanalyze.persistence.ArticleDao;
import com.voxlearning.utopia.enanalyze.persistence.RankFrequencyCache;
import com.voxlearning.utopia.enanalyze.persistence.UserDao;
import com.voxlearning.utopia.enanalyze.persistence.UserGroupDao;
import com.voxlearning.utopia.enanalyze.service.RankAbilityService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消费者 - 新增作业批改记录
 *
 * @author xiaolei.li
 * @version 2018/7/29
 */
@Slf4j
@ConsumerLabel(topic = Topic.ARTICLE_CREATE)
public class ArticleCreateConsumer implements MessageConsumer {

    @Resource
    ArticleDao articleDao;

    @Resource
    UserDao userDao;

    @Resource
    UserGroupDao userGroupDao;

    @Resource
    RankAbilityService rankAbilityService;

    @Resource
    RankFrequencyCache rankFrequencyCache;

    @Resource
    SentencePersistenceFacade sentenceCacheFacade;

    @Override
    public void handle(String messageBody) {
        final String articleId = messageBody;
        ArticleEntity article = articleDao.findById(articleId);
        AINLPClient.Result nlpResult = article.getNlpResult();
        try {
            // 更新好句子排名
            updateSentenceRank(article);
            // 更新能力排名
            updateAbilityRank(article);
            // 更新批改频次排名
            updateFrequencyRank(article);
        } catch (Exception e) {
            // 有任何异常，更新排行榜服务降级，不做异常反馈
            log.error("更新排行榜出错,作文id = {}", messageBody, e);
        }
    }

    /**
     * 更新好句子排行以及好句子
     *
     * @param article 作文批改记录
     */
    void updateSentenceRank(ArticleEntity article) {
        AINLPClient.Result nlpResult = article.getNlpResult();
        if (true
                // 结果不为空
                && null != nlpResult
                // 得分不为空
                && null != nlpResult.getEssayRating()
                // 得分 >= 60
                && 60 <= nlpResult.getEssayRating().getOverall_score()
                // 好句子不为空
                && ArrayUtils.isNotEmpty(nlpResult.getEssayRating().getGood_sents())) {

            Optional<AINLPClient.Result.EssayRating.GoodSent> sent = Arrays.stream(nlpResult.getEssayRating().getGood_sents())
                    .filter(i -> StringUtils.isNotBlank(i.getSentence()))
                    .sorted(Comparator.comparing(AINLPClient.Result.EssayRating.GoodSent::getScore).reversed())
                    .findFirst();
            // 查找用户当前所属所有群
            List<String> userGroups = userGroupDao.findByOpenId(article.getOpenId())
                    .stream()
                    .map(UserGroupEntity::getOpenGroupId)
                    .collect(Collectors.toList());
            // 持久化
            sent.ifPresent(i -> {
                SentencePersistenceFacade.Sentence sentence = new SentencePersistenceFacade.Sentence();
                sentence.setArticleId(article.getId());
                sentence.setOpenGroupIds(userGroups.toArray(new String[]{}));
                sentence.setOpenId(article.getOpenId());
                sentence.setSentenceScore(i.getScore());
                sentence.setSentence(i.getSentence());
                sentenceCacheFacade.update(sentence);
            });
        }
    }

    /**
     * 更新综合能力排名
     *
     * @param article
     */
    void updateAbilityRank(ArticleEntity article) {
        if (null != article.getCompositeAbility()
                && 0 <= article.getCompositeAbility().getScore()) {
            rankAbilityService.update(article.getOpenId(), article.getCompositeAbility());
        }
    }

    /**
     * 更新作文频次排行
     *
     * @param article
     */
    void updateFrequencyRank(ArticleEntity article) {
        UserEntity user = userDao.findByOpenId(article.getOpenId());
        Date beginDate = user.getCreateDate();
        int days = Double.valueOf(
                // 向上取正，也就是，1.1 = 2
                Math.ceil(
                        // 当前时间 - 用户创建时间 的毫秒数
                        Double.valueOf(new Date().getTime() - beginDate.getTime())
                                // 一天的毫秒数
                                / Double.valueOf(1000L * 60 * 60 * 24)))
                .intValue();
        Long totalCount = articleDao.count(article.getOpenId());
        BigDecimal frequency = new BigDecimal(totalCount).divide(new BigDecimal(days), 2, BigDecimal.ROUND_DOWN);
        rankFrequencyCache.update(article.getOpenId(), frequency.doubleValue());
    }
}
