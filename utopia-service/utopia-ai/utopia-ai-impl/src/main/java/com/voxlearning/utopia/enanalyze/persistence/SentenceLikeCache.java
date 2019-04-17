package com.voxlearning.utopia.enanalyze.persistence;

import com.voxlearning.utopia.enanalyze.api.SentenceLikeService;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.enanalyze.api.SentenceLikeService.Result;

/**
 * 好句子点赞缓存接口
 *
 * @author xiaolei.li
 * @version 2018/7/24
 */
public interface SentenceLikeCache {

    /**
     * 点赞或者取消点赞
     *
     * @param params 请求参数
     * @return 点赞结果
     */
    Result like(SentenceLikeService.Params params);

    /**
     * 点赞或者取消点赞
     *
     * @param openGroupId 群id
     * @param fromOpenId  谁点的赞
     * @param toOpenId    给谁的赞
     * @return 结果
     */
    Result like(String openGroupId, String fromOpenId, String toOpenId);

    /**
     * 获取某个人群中某个人的点赞数
     *
     * @param openGroupId 群id
     * @param openId      openId
     * @return 点赞数
     */
    long getLikes(String openGroupId, String openId);

    /**
     * 获取某个群下的某些人的点赞数
     *
     * @param openGroupId
     * @param openIds
     * @return
     */
    Map<String, Long> queryLikes(String openGroupId, List<String> openIds);

    /**
     * 获取某个人在某些群下的点赞数
     *
     * @param openGroupIds
     * @param openId
     * @return
     */
    Map<String, Long> queryLikes(List<String> openGroupIds, String openId);

    /**
     * 批量 - 获取某个群下的某些人（toOpenIds)是否被fromOpenId点过赞
     *
     * @param openGroupId 群id
     * @param fromOpenId  来自谁的点赞
     * @param toOpenIds   用户id（多个）
     * @return 结果，格式（openId => likes)
     */
    Map<String, Boolean> batchGetLikeStatus(String openGroupId, String fromOpenId, List<String> toOpenIds);

    /**
     * 清空某个人在某些群的点赞数据
     *
     * @param openGroupIds 群id - 多个
     * @param openId       openid
     */
    void purge(List<String> openGroupIds, String openId);

    @Data
    class Like implements Serializable {
        private String groupId;
        private String fromOpenId;
        private String toOpenId;
        private boolean likeStatus;
        private long likes;
    }
}
