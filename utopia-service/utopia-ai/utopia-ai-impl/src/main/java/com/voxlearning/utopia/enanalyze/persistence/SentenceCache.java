package com.voxlearning.utopia.enanalyze.persistence;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 好句子缓存接口
 *
 * @author xiaolei.li
 * @version 2018/7/22
 */
public interface SentenceCache {

    /**
     * 更新
     *
     * @param sentence 句子
     */
    void update(Sentence sentence);

    /**
     * 根据openid获取好句子
     *
     * @param openId openid
     * @return
     */
    Sentence get(String openId);

    /**
     * 根据openid集合，找到对所有人的好句子
     *
     * @param openIds openid列表
     * @return Sentence列表
     */
    List<Sentence> list(List<String> openIds);

    /**
     * 找到给定的openIds所有人的好句子
     *
     * @param openIds 多个用户
     * @return 映射（openId, Sentence)
     */
    Map<String, Sentence> queryMap(List<String> openIds);

    /**
     * 句子
     */
    @Data
    class Sentence implements Serializable {
        private String articleId;
        private String openId;
        private String text;
        private float score;
    }
}
