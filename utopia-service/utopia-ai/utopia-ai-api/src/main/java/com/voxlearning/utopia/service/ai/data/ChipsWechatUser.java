package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChipsWechatUser implements Serializable {
    private static final long serialVersionUID = 0L;

    private Long wechatUserId;

    private String openId;

    private String nickName;

    private String avatar;

    private Long userId;

}
