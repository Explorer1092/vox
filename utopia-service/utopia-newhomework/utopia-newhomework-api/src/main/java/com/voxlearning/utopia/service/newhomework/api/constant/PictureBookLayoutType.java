package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 阅读绘本的结构
 *
 * @author xuesong.zhang
 * @since 2016-07-18
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PictureBookLayoutType {
    pt("左图右文"),
    tp("右图左文"),
    ptpt("左右图文混排"),
    wp("上图下文"),
    tt("左文右文");

    @Getter private final String desc;
}
