package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 15-5-14.
 */
@RequiredArgsConstructor
public enum WechatTicketType {
    TEACHER_FACE2FACE_INVITE(1, "面对面邀请");
    @Getter
    private final int type;
    @Getter
    private final String description;
}
