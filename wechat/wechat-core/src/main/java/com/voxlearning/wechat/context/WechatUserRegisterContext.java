package com.voxlearning.wechat.context;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.support.WechatUserInfo;
import lombok.Data;

@Data
public class WechatUserRegisterContext {

    //in
    private String openId;
    private WechatType wechatType;

    //
    private WechatUserInfo info;
    private Long userId = null;

}
