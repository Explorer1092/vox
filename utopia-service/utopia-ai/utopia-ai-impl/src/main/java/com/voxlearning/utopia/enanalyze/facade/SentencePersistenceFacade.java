package com.voxlearning.utopia.enanalyze.facade;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 关于句子持久化的facade
 * <br>
 * 好句子排行榜在redis中存在两块，通过一个门面模式，把复杂的底层缓存封装起来，提供一个简单的服务
 * <ul>
 * <li>通过sorted set实现的排行榜</li>
 * <li>通过hash实现的存储</li>
 *
 * </ul>
 *
 * @author xiaolei.li
 * @version 2018/7/22
 */
public interface SentencePersistenceFacade {

    /**
     * 更新缓存
     *
     * @param sentence 句子
     */
    void update(Sentence sentence);

    /**
     * 获取句子
     *
     * @param openGroupId 群id
     * @param openId      openId
     * @return 句子的信息，包括排名、得分、文本、点赞数
     */
    Result get(String openGroupId, String openId);


    /**
     * 获取某个群所有人的排行
     *
     * @param openGroupId 群id
     * @param fromOpenId      关联的openId，对于每一个记录需要得出是否处于点赞状态，此openId就是用来判断的
     * @return 结果
     */
    List<Result> list(String openGroupId, String fromOpenId);

    /**
     * 句子
     */
    @Data
    class Sentence implements Serializable {

        /**
         * 群id - 多个
         */
        private String[] openGroupIds;

        /**
         * openid
         */
        private String openId;

        /**
         * 所属作文id
         */
        private String articleId;

        /**
         * 句子得分
         */
        private float sentenceScore;

        /**
         * 句子
         */
        private String sentence;
    }

    /**
     * 句子
     */
    @Data
    class Result implements Serializable {

        /**
         * 群id
         */
        private String openGroupId;

        /**
         * 被点赞人的openId
         */
        private String openId;

        /**
         * 所属作文id
         */
        private String articleId;

        /**
         * 句子
         */
        private String sentence;

        /**
         * 得分
         */
        private float sentenceScore;

        /**
         * 排名
         */
        private long groupRank;

        /**
         * 点赞数
         */
        private long groupLikes;

        /**
         * 是否点赞
         */
        private boolean likeStatus;
    }
}
