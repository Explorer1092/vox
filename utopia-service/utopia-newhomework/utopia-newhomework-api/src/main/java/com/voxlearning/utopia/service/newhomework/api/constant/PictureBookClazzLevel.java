package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author guoqiang.li
 * @since 2016/7/15
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PictureBookClazzLevel {
    FIRST_GRADE("一年级", 1),
    SECOND_GRADE("二年级", 2),
    THIRD_GRADE("三年级", 3),
    FOURTH_GRADE("四年级", 4),
    FIFTH_GRADE("五年级", 5),
    SIXTH_GRADE("六年级", 6);


    @Getter private final String showName;
    @Getter private final Integer clazzLevel;

    public static PictureBookClazzLevel of(String name) {
        try {
            return PictureBookClazzLevel.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
