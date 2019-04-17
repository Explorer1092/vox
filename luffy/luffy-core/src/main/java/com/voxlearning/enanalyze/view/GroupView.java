package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.model.GroupWithLike;
import lombok.Data;

import java.io.Serializable;

/**
 * 组视图
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class GroupView implements Serializable {

    /**
     * 群id
     */
    private String openGroupId;

    /**
     * 群名称
     */
    private String openGroupName;

    /**
     * openid
     */
    private String openId;

    /**
     * 排名
     */
    private long rank;

    /**
     * 点赞数
     */
    private long likes;

    public static class Builder {
        public static GroupView build(GroupWithLike data) {
            GroupView view = new GroupView();
            view.setOpenGroupId(data.getOpenGroupId());
            view.setOpenGroupName(data.getOpenGroupName());
            view.setOpenId(data.getOpenId());
            view.setLikes(data.getLikes());
            view.setRank(data.getRank());
            return view;
        }
    }
}
