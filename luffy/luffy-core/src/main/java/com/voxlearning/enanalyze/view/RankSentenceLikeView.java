package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.api.SentenceLikeService;
import lombok.Data;

import java.io.Serializable;

/**
 * 点赞结果视图
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class RankSentenceLikeView implements Serializable {

    /**
     * 所属群id
     */
    private String openGroupId;

    /**
     * 谁点的赞
     */
    private String fromOpenId;

    /**
     * 给谁点赞
     */
    private String toOpenId;

    /**
     * 当前点赞状态,true:被赞状态;false:取消点赞状态
     */
    private boolean likeStatus;

    /**
     * 当前累计点赞次数
     */
    private long likes;


    public static class Builder {
        public static RankSentenceLikeView build(SentenceLikeService.Result data) {
            RankSentenceLikeView view = new RankSentenceLikeView();
            view.setOpenGroupId(data.getOpenGroupId());
            view.setFromOpenId(data.getFromOpenId());
            view.setToOpenId(data.getToOpenId());
            view.setLikeStatus(data.isLikeStatus());
            view.setLikes(data.getLikes());
            return view;
        }
    }
}
