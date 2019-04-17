package com.voxlearning.utopia.enanalyze.persistence;


import com.voxlearning.utopia.enanalyze.entity.UserEntity;

import java.util.List;
import java.util.Map;

/**
 * 用户持久层
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface UserDao {

    /**
     * 根据openId查询
     *
     * @param openId openid
     * @return
     */
    UserEntity findByOpenId(String openId);

    /**
     * 根据openid列表查询
     *
     * @param openIds 用户列表
     * @return
     */
    Map<String, UserEntity> findByOpenIds(List<String> openIds);

    /**
     * 创建用户
     *
     * @param user
     */
    void insert(UserEntity user);

    /**
     * 更新
     *
     * @param user
     */
    void update(UserEntity user);

    /**
     * 查询总用户数
     *
     * @return
     */
    long totalCount();
}
