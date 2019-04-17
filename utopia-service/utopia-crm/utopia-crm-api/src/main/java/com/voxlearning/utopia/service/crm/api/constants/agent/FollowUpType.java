package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 跟进类型
 *
 * @author song.wang
 * @date 2018/12/5
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FollowUpType {

    ONLINE("微信/QQ/短信", true),
    PHONE_CALL("打电话", true),
    SCHOOL("进校", true),
    MEETING("组会", true),
    RESOURCE_EXTENSION("资源拓维", true);

    private final String desc;
    private final boolean valid;  // 当前类型是否有效

}
