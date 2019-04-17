package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 *
 * @author song.wang
 * @date 2018/1/11
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentUserOperationType {

    SHOW_TEACHER("显示隐藏的老师"),

    CONTRACT_BASE_INFO("合同基本信息"),

    CONTRACT_PAYBACK_INFO("回款信息"),

    ORGANIZATION_INFO("机构信息");

    private final String desc;
}
