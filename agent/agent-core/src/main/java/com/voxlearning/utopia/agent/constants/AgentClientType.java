package com.voxlearning.utopia.agent.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by yaguang.wang
 * on 2017/6/8.
 */
@AllArgsConstructor
@Getter
public enum AgentClientType {
    IOS("901", "IOS客户端版本"),
    ANDROID("900", "安卓版本");
    private String productId;
    private String des;
}
