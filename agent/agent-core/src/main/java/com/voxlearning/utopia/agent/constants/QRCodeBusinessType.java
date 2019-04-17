package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * QRCodeBusinessType
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum QRCodeBusinessType {

    LIVE_ENROLLMENT("直播招生")


    ;

    private final String desc;
}
