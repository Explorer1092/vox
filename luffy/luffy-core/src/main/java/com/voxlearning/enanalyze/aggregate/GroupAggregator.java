package com.voxlearning.enanalyze.aggregate;

import com.voxlearning.enanalyze.view.GroupView;

import java.util.List;

/**
 * 群聚合服务接口
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface GroupAggregator {

    /**
     * 列出某个用户关联的所有微信群
     *
     * @param openId openid
     * @return 群列表
     */
    List<GroupView> list(String openId);

    /**
     * 删除某个用户与群的关系
     *
     * @param openId      用户id
     * @param openGroupId 群id
     */
    void remove(String openId, String openGroupId);
}
