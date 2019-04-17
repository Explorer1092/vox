package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.model.User;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息
 *
 * @author xiaolei.li
 * @version 2018/7/19
 */
@Data
public class UserRequest implements Serializable {

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

    public static class Builder {
        public static User build(UserRequest request) {
            User user = new User();
            user.setOpenId(request.getOpenId());
            user.setNickName(request.getNickName());
            user.setGender(request.getGender());
            user.setAvatarUrl(request.getAvatarUrl());
            user.setCity(request.getCity());
            user.setCountry(request.getCountry());
            user.setProvince(request.getProvince());
            return user;
        }
    }
}
