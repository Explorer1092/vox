package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupRoleType
 *
 * @author song.wang
 * @date 2016/6/20
 */
public enum AgentGroupRoleType {

    Admin(1, "系统管理部", "系统管理"),
    Finance(2, "财务部", "财务部门"),
    DataViewer(3, "协作部", "协作部门"),
    Vendor(4, "开发商", "第三方应用开发商"),

    Country(30, "全国", "市场部全国级别的部门"),
    BusinessUnit(33, "业务部", "市场部业务部级别的部门"),
    Operation(35, "运营部", "运营部"),
    Marketing(36, "市场", "市场"),
    Region(31, "大区", "市场部大区级别部门"),
    City(32, "分区", "市场部城市级部门"),
    Area(34, "区域", "市场部区域级别部门"),

    EVALUATION(40, "测评运营部", "测评运营部"),
    USAGEINFO(41, "应用信息部", "应用信息部"),
    MATERIAL(42, "物料中心", "物料中心"),
    RISK_MANAGEMENT(43, "风控组", "风控组"),
    PRODUCT_OPERATION(44, "产品运营", "产品运营")
    ;

    @Getter
    private Integer id;
    @Getter  private String roleName;
    @Getter  private String desc;

    private AgentGroupRoleType(Integer id, String roleName, String desc){
        this.id = id;
        this.roleName = roleName;
        this.desc = desc;
    }

    private final static Map<String, AgentGroupRoleType> ROLE_TYPE_MAP = new LinkedHashMap<>();
    private final static List<Integer> ROLE_ID_LIST = new ArrayList<>();
    private final static Map<String, AgentGroupRoleType> NAME_MAP = new LinkedHashMap<>();

    static {
        for (AgentGroupRoleType roleType : AgentGroupRoleType.values()) {
            ROLE_ID_LIST.add(roleType.getId());
            ROLE_TYPE_MAP.put(String.valueOf(roleType.getId()), roleType);
            NAME_MAP.put(roleType.name(),roleType);
        }
    }

    public static List<AgentGroupRoleType> getManageableRoleList(AgentGroupRoleType groupRoleType) {
        List<AgentGroupRoleType> retList = new ArrayList<>();
        if(groupRoleType == null){
            retList.addAll(ROLE_TYPE_MAP.values());
            return retList;
        }
        switch (groupRoleType){
            case Admin: // 管理员管理所有
                retList.add(Admin);
                break;
            case Finance: // 财务没有管理权限
                retList.add(Finance);
                break;
            case DataViewer:
                retList.add(DataViewer);
                break;
            case Vendor:
                retList.add(Vendor);
                break;
            case Country: // 全国
                retList.add(BusinessUnit);
                retList.add(Operation);
                break;
            case BusinessUnit: // 事业部
                retList.add(Marketing);
                retList.add(Operation);
                break;
            case Marketing: // 市场部
                retList.add(Region);
                retList.add(Area);
                retList.add(City);
                break;
            case Region: // 大区
                retList.add(Area);
                retList.add(City);
                break;
            case Area: // 区域
                retList.add(City);
                break;
            case City: // 分区
                break;
            default: // 其他
                break;
        }
        return retList;
    }

    public static AgentGroupRoleType of(Integer type) {
        if (type == null) {
            return null;
        }
        return ROLE_TYPE_MAP.get(String.valueOf(type));
    }
    public static Boolean isAgentGroupRoleType(Integer type){
        return ROLE_ID_LIST.contains(type);
    }

    public static AgentRoleType getGroupManagerRoleType(AgentGroupRoleType groupRoleType) {
        if(Country == groupRoleType){
            return AgentRoleType.Country;
        }else if (BusinessUnit == groupRoleType) {
            return AgentRoleType.BUManager;
        }else if (Region == groupRoleType) {
            return AgentRoleType.Region;
        }else if (Area == groupRoleType) {
            return AgentRoleType.AreaManager;
        } else {
            return AgentRoleType.CityManager;
        }
    }

    public static List<AgentGroupRoleType> getMarketRoleList(){
        List<AgentGroupRoleType> retList = new ArrayList<>();
        retList.add(Country);
        retList.add(BusinessUnit);
        retList.add(Marketing);
        retList.add(Region);
        retList.add(Area);
        retList.add(City);
        return retList;
    }

    public static AgentGroupRoleType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }
}
