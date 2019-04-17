package com.voxlearning.utopia.enanalyze.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 好句子排行榜持久层接口
 *
 * @author xiaolei.li
 * @version 2018/7/22
 */
public interface SentenceRankCache {

    /**
     * 更新
     *
     * @param element 元素
     */
    void update(Element element);

    /**
     * 根据openGroupId和openid获取排行
     *
     * @param openGroupId groupid
     * @param openId      openid
     * @return 结果
     */
    Rank getRank(String openGroupId, String openId);

    /**
     * 获取某个群所有人的排行
     *
     * @param openGroupId
     * @return 结果
     */
    List<Rank> getRanks(String openGroupId);


    /**
     * 好句子排行榜元素
     *
     * @author xiaolei.li
     * @version 2018/7/22
     */
    @Data
    class Element implements Serializable {

        /**
         * 群id - 多个
         */
        private String[] openGroupIds;

        /**
         * openid
         */
        private String openId;

        /**
         * 得分
         */
        private float score;
    }

    /**
     * 排名
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Rank implements Serializable {

        /**
         * 群id
         */
        private String openGroupId;

        /**
         * openid
         */
        private String openId;

        /**
         * 排行
         */
        private Long rank;
    }
}
