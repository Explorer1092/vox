package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by jiang wei on 2017/8/1.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PicOrderInfoType {

    REN_JIAO(0, "人教"),
    SHAN_DONG(1, "山东科技版"),
    SHANG_HAI(2, "沪教"),
    LIAO_NING(3, "辽师大");

    private final Integer id;
    private final String desc;
}
