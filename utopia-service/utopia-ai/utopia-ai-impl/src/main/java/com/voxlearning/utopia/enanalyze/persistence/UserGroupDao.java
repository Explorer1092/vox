package com.voxlearning.utopia.enanalyze.persistence;

import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;

import java.util.List;

/**
 * 用户群关系集持久层
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface UserGroupDao {

    /**
     * 新增
     *
     * @param userGroupEntity 用户群实体
     */
    void insert(UserGroupEntity userGroupEntity);

    /**
     * 根据openid获取用户群实体
     *
     * @param openId 成员id
     * @return 结果
     */
    List<UserGroupEntity> findByOpenId(String openId);

    /**
     * 根据群id获取当前群的所有成员
     *
     * @param openGroupId 群id
     * @return 结果
     */
    List<UserGroupEntity> findByGroupId(String openGroupId);

    /**
     * 根据用户和群删除关联关系
     *
     * @param openId      用户id
     * @param openGroupId 群id
     */
    void delete(String openId, String openGroupId);
}
