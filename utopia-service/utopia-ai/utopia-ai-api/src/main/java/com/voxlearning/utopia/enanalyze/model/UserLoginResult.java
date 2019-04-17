package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 授权响应
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class UserLoginResult implements Serializable {

    /**
     * 令牌
     */
    private String token;

    /**
     * 微信openid
     */
    private String openId;

    /**
     * 微信群id
     */
    private String openGroupId;
}
