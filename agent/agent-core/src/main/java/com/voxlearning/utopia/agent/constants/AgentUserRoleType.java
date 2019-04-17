package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AgentUserRoleType
 *
 * @author song.wang
 * @date 2016/6/16
 */

public enum AgentUserRoleType {

//    CountryManager(10, "全国总监", "全国总监"),
//    RegionManager(11, "大区总监", "大区总监"),
//    CityManager(13, "市经理", "各区域负责人"),
//    CityAgent(15, "市代理", "各区域负责人"),
//    Commissioner(21, "学校专员", "各地区学校专员");
;
    @Getter
    private Integer id;
    @Getter
    private String roleName;
    @Getter
    private String desc;

    private AgentUserRoleType(Integer id, String roleName, String desc){
        this.id = id;
        this.roleName = roleName;
        this.desc = desc;
    }

}
