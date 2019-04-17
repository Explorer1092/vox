package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author xin
 * @since 14-4-24 上午10:45
 */
@RequiredArgsConstructor
public enum WechatQuestionState {
    WAITING(0,"待处理");

    @Getter
    private final int type;
    @Getter
    private final String description;
}
