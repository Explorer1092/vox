package com.voxlearning.utopia.enanalyze.mq.consumer;

import com.voxlearning.utopia.enanalyze.entity.ArticleEntity;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.mq.ConsumerLabel;
import com.voxlearning.utopia.enanalyze.mq.MessageConsumer;
import com.voxlearning.utopia.enanalyze.mq.Topic;
import com.voxlearning.utopia.enanalyze.persistence.ArticleDao;
import com.voxlearning.utopia.enanalyze.persistence.RankFrequencyCache;
import com.voxlearning.utopia.enanalyze.persistence.SentenceCache;
import com.voxlearning.utopia.enanalyze.persistence.UserDao;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 作文删除消息消费者
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
@ConsumerLabel(topic = Topic.ARTICLE_DELETE)
public class ArticleDeleteConsumer implements MessageConsumer {

    @Resource
    UserDao userDao;

    @Resource
    ArticleDao articleDao;

    @Resource
    SentenceCache sentenceCache;

    @Resource
    RankFrequencyCache rankFrequencyCache;

    @Override
    public void handle(String messageBody) {

        // 获取当前作文记录
        final String articleId = messageBody;
        ArticleEntity article = articleDao.findById(articleId);
        if (null != article) {
            String openId = article.getOpenId();
            UserEntity user = userDao.findByOpenId(openId);
            Date beginDate = user.getCreateDate();
            int days = Double.valueOf(
                    // 向上取正，也就是，1.1 = 2
                    Math.ceil(
                            // 当前时间 - 用户创建时间 的毫秒数
                            Double.valueOf(new Date().getTime() - beginDate.getTime())
                                    // 一天的毫秒数
                                    / Double.valueOf(1000L * 60 * 60 * 24)))
                    .intValue();
            Long totalCount = articleDao.count(openId);
            BigDecimal frequency = new BigDecimal(totalCount).divide(new BigDecimal(days), 2, BigDecimal.ROUND_DOWN);
            rankFrequencyCache.update(openId, frequency.doubleValue());
        }
    }
}
