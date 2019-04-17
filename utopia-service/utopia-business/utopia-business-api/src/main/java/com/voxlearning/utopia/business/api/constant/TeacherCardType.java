package com.voxlearning.utopia.business.api.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author guoqiang.li
 * @since 2017/4/18
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeacherCardType {
    AUTHENTICATION,
    HOMEWORK,
    ACTIVITY,
    NONE
}
