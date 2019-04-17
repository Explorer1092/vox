package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 试卷录入状态
 * Created by zang.tao on 2017/4/17.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum  TestPaperEntryStatus {
    PENDINGENTRY(1,"待录入"),
    DRAFT(2, "录入中"),  //报名考试 未发布; 统一考试 录入中
    ONLINE(3,"已发布"),
    OFFLINE(4, "下线");
    @Getter
    private final Integer type;
    @Getter
    private final String desc;
}
