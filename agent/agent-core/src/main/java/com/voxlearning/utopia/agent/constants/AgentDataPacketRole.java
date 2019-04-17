package com.voxlearning.utopia.agent.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 资料包使用角色
  Created by Administrator on 2016/9/6.
 */
@Getter
@RequiredArgsConstructor
public enum AgentDataPacketRole {

    Country(10, "全国总监"),
    Region(11, "大区经理"),
    CityManager(13, "城市经理"),
    BusinessDeveloper(21, "城市专员"),
    CityAgent(15, "代理"),
    BUManager(17, "业务部经理"),
    AreaManager(19, "区域经理");

    private final Integer id;
    private final String roleName;

    public static final Map<Integer, AgentDataPacketRole> roleTypeMap;

    static {
        roleTypeMap = new HashMap<>();
        for (AgentDataPacketRole type : values()) {
            roleTypeMap.put(type.getId(), type);
        }
    }

    public static AgentDataPacketRole typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return AgentDataPacketRole.roleTypeMap.get(id);
    }
}
