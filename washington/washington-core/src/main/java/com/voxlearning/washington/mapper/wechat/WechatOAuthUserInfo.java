package com.voxlearning.washington.mapper.wechat;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xin.xin
 * @since 8/6/18
 **/
@Getter
@Setter
public class WechatOAuthUserInfo implements Serializable {
    private static final long serialVersionUID = 5556411570408007844L;

    private String nickName;
    private String headImgUrl;
    private String sex;
    private String province;
    private String city;
    private String openId;
}
