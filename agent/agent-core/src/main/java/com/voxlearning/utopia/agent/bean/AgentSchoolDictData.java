package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用于字典表信息展示
 * Created by yaguang.wang on 2016/6/29.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentSchoolDictData implements ExportAble {

    private static final long serialVersionUID = 5328303947633709781L;

    private Long id;
    @Deprecated
    private Integer regionCode;
    @Deprecated
    private String regionName;
    private Long schoolId;
    private String schoolName;
    private String schoolLevel;

    @Deprecated
    private Integer level;

    private Boolean calPerformance; // 参与业绩计算

    private String schoolDifficulty;        // 任务难度：枚举类型，目前仅包含S，可为空 AgentDictSchoolDifficultyType

    private String schoolPopularity;        // 学校等级：枚举类型，目前仅包含A，可为空 AgentSchoolPopularityType

    /**
     * 渗透情况   @see AgentSchoolPermeabilityType
     */
    private String agentSchoolPermeabilityType;

    private GroupWithParent groupWithParent;

    private String cityManagerName;                     // 市经理名称
    private String businessDeveloperName;               // 专员名称
    private String businessDeveloperNumber;             // 专员工号

    // ------ 一下字段只有
    @Deprecated
    private SchoolDepartmentInfo schoolDepartmentInfo;

    private Integer cityCode;
    private String cityName;
    private String countyName;
    private Integer countyCode;

    @Deprecated
    private Integer provinceCode;
    @Deprecated
    private String provinceName;
    @Deprecated
    private Long groupId; // 所属部门的Id
    @Deprecated
    private String groupName; //所属部门名字


    private Integer schoolSize;

    @Deprecated
    private SchoolLevel schoolLevelEnum;

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(this.getCityName());
        result.add(this.getCountyName());
        result.add(this.getCountyCode());
        result.add(this.getSchoolId());
        result.add(this.getSchoolName());
        result.add(this.getSchoolLevel());
        result.add(this.getSchoolPopularity());
        result.add(schoolSize);
        result.add(this.getSchoolDifficulty());
        result.add(SafeConverter.toBoolean(this.getCalPerformance()) ? "是" : "否");
        result.add(this.getAgentSchoolPermeabilityType() == null ? "" : this.getAgentSchoolPermeabilityType());

        String regionGroupName = "";
        String areaGroupName = "";
        String cityGroupName = "";
        if(groupWithParent != null && Objects.equals(groupWithParent.getRoleId(), AgentGroupRoleType.City.getId())){
            cityGroupName = groupWithParent.getGroupName();
            if(groupWithParent.getParent() != null){
                GroupWithParent parentGroup = groupWithParent.getParent();
                if(Objects.equals(parentGroup.getRoleId(), AgentGroupRoleType.Area.getId())){
                    areaGroupName = parentGroup.getGroupName();
                    if(parentGroup.getParent() != null && Objects.equals(parentGroup.getParent().getRoleId(), AgentGroupRoleType.Region.getId())){
                        regionGroupName = parentGroup.getParent().getGroupName();
                    }
                }else if(Objects.equals(parentGroup.getRoleId(), AgentGroupRoleType.Region.getId())){
                    regionGroupName = parentGroup.getGroupName();
                }
            }
        }

        result.add(regionGroupName);
        result.add(areaGroupName);
        result.add(cityGroupName);
        result.add(cityManagerName);
        result.add(businessDeveloperName);
        result.add(businessDeveloperNumber);
        return result;
    }
}
