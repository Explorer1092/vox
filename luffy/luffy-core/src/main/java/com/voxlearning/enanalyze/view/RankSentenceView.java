package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.model.SentenceRankResult;
import lombok.Data;

import java.io.Serializable;

/**
 * 排行视图
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class RankSentenceView implements Serializable {
    private String openGroupId;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private String sentence;
    private long rank;
    private long likes;
    private boolean likeStatus;

    public static class Builder {
        public static RankSentenceView build(SentenceRankResult data) {
            RankSentenceView view = new RankSentenceView();
            view.setOpenGroupId(data.getOpenGroupId());
            view.setOpenId(data.getOpenId());
            view.setNickName(data.getNickName());
            view.setAvatarUrl(data.getAvatarUrl());
            view.setSentence(data.getSentence());
            view.setRank(data.getRank());
            view.setLikes(data.getLikes());
            view.setLikeStatus(data.isLikeStatus());
            return view;
        }
    }

}
