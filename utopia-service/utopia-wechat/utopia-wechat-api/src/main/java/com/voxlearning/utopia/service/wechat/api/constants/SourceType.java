package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Hailong Yang on 2015/10/10.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SourceType {
    APP_JZT(1,"手机家长通"),
    WECHAT(2,"微信");

    private final int value;
    private final String description;

}