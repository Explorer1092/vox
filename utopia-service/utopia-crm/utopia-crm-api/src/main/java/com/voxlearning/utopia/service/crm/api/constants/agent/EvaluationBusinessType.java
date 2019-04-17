package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 *
 *
 * @author song.wang
 * @date 2018/12/14
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EvaluationBusinessType {

    SCHOOL("进校"),
    MEETING("组会"),
    RESOURCE_EXTENSION("资源拓维");
    private final String desc;
}
