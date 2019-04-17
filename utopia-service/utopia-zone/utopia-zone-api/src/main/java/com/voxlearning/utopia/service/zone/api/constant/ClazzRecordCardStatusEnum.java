package com.voxlearning.utopia.service.zone.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/1
 * Time: 15:55
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ClazzRecordCardStatusEnum {

    UNKNOWN("未知"),
    UNLOCK("未解锁"), // FIXME 吐槽一下, 未解锁叫UNLOCK...被误导好久...这次是改不动了...
    ING("挑战中"),
    DONE("已结束");
    @Getter
    private final String desc;

}
