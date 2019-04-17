package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 授权响应
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class UserLoginView implements Serializable {

    /**
     * 令牌
     */
    private String token;

    /**
     * openid
     */
    private String openId;

    /**
     * 群id
     */
    private String openGroupId;
}
