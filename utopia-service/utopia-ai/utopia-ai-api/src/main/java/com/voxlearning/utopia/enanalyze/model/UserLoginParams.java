package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 授权请求
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class UserLoginParams implements Serializable {

    /**
     * 微信code
     */
    private String code;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像url
     */
    private String avatarUrl;

    /**
     * 加密数据
     */
    private String encryptedData;

    /**
     * 加密算法的初始向量
     */
    private String iv;
}
