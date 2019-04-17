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
public enum PictureBookSubPage {
    firstHalfPage("1", "前半页"),
    afterHalfPage("2", "后半页");

    @Getter private final String index; // 对应页码
    @Getter private final String desc;  // 页码描述
}
