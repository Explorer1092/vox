package com.voxlearning.utopia.enanalyze.persistence;

import com.voxlearning.utopia.enanalyze.entity.GroupEntity;

import java.util.List;

/**
 * 群持久层
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface GroupDao {

    GroupEntity findByOpenGroupId(String openGroupId);

    void insert(GroupEntity groupEntity);

    List<GroupEntity> findByOpenId(String openId);
}
