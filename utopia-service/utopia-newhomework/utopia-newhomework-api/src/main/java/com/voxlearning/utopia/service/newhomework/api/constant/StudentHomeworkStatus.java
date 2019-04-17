package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Hailong Yang on 2015/10/27.
 */
@RequiredArgsConstructor
public enum StudentHomeworkStatus {
    NEW(0, "待完成", "NEW"),
    @Deprecated
    TIMEOUT(1, "超时未完成", "TIMEOUT"),
    FINISH(2, "已完成", "FINISH"),
    CHECK_UNFINISH(3, "未完成", "CHECK_UNFINISH"),
    CHECK_FINISH(4, "已检查", "CHECK_FINISH"),
    UN_START(5,"待完成","NEW"),
    ;

    @Getter
    private final int type;
    @Getter private final String title;
    @Getter private final String status;
}
