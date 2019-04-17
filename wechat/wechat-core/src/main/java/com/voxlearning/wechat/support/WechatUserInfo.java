package com.voxlearning.wechat.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class WechatUserInfo implements Serializable {
    private static final long serialVersionUID = 645568647145257590L;
    private String nickname;
    private String headimgurl;
    private Integer sex;//1 男 2 女
}
