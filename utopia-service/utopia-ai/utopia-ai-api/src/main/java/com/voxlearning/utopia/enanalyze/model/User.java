package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息
 *
 * @author xiaolei.li
 * @version 2018/7/19
 */
@Data
public class User implements Serializable {
    /**
     * openid
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像url
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private String gender;

    /**
     * 城市
     */
    private String city;

    /**
     * 省份
     */
    private String province;

    /**
     * 国家
     */
    private String country;

}
