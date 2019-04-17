package com.voxlearning.utopia.enanalyze.persistence;


import com.voxlearning.utopia.enanalyze.entity.ArticleEntity;

import java.util.List;

/**
 * 作文持久层
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface ArticleDao {

    /**
     * 新增
     *
     * @param entity 实体
     */
    void insert(ArticleEntity entity);

    /**
     * 更新
     *
     * @param entity 实体
     */
    void update(ArticleEntity entity);

    /**
     * 通过openid获取所有记录
     *
     * @param openId openid
     * @return 记录列表
     */
    List<ArticleEntity> findByOpenId(String openId);

    /**
     * 通过主键查询
     *
     * @param id 主键
     * @return 记录
     */
    ArticleEntity findById(String id);

    /**
     * 获取最后一次修改记录
     *
     * @param openId openid
     * @return 最后一次修改记录
     */
    ArticleEntity findLast(String openId);

    /**
     * 失效
     *
     * @param id 记录
     */
    void disable(String id);

    /**
     * 获取总批改记录次数
     *
     * @param openId 用户id
     * @return
     */
    Long count(String openId);

//    /**
//     * 获取作文得分最高分的作文
//     *
//     * @param openId openid
//     * @return 最高分的作文，可能为多个相同分数的最高分
//     */
//    List<ArticleEntity> findHighestScoreByOpenId(String openId);
}
