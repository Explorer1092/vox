package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 统考试卷来源
 * Created by tao.zang on 2017/4/17.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum  UnifiedExamTestPaperSourceType {

    NEWLYADDED(0,"新添加"),
    ANCIENT(1,"以往的");
    @Getter
    private final Integer type;
    @Getter
    private final String desc;
}
