/*
 *
 *  * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *  *
 *  *  Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *  *
 *  *  NOTICE: All information contained herein is, and remains the property of
 *  *  Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 *  *  and technical concepts contained herein are proprietary to Vox Learning
 *  *  Technology, Inc. and its suppliers and may be covered by patents, patents
 *  *  in process, and are protected by trade secret or copyright law. Dissemination
 *  *  of this information or reproduction of this material is strictly forbidden
 *  *  unless prior written permission is obtained from Vox Learning Technology, Inc.
 *
 */

package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shuai.Huan on 2014/7/15.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentRoleType {

    Admin(1, "管理员", "系统管理员", true),
    Finance(2, "财务", "财务人员", true),
    DataViewer(3, "协作", "协作人员", false),
    Vendor(4, "开发商", "第三方应用开发商", false),

    Country(10, "全国总监", "全国总监", true),
    Region(11, "大区经理", "大区经理", true),
    ProvinceManager(12, "省级负责人-省经理", "各省市场经理", false),
    ProvinceAgent(14, "省级负责人－省代理", "各省市场代理", false),
    CityManager(13, "市经理", "各区域负责人", true),
    CityAgent(15, "总代理商", "各区域负责人", true),
    BusinessDeveloper(21, "市场专员", "各地区学校专员", true),
    CityAgentLimited(16, "小代理商", "小代理商", true),
    BUManager(17, "业务部经理", "业务部经理", false),
    AreaManager(19, "区域经理", "区域经理", true),

    EVALUATION(40, "测评运营人员", "测评运营人员", false),
    USAGEINFO(41, "应用信息人员", "应用信息人员", false),
    MATERIAL(42, "物料管理人员", "物料管理人员", false),
    RISK_MANAGER(43, "信息审核员", "信息审核员", false),
    PRODUCT_OPERATOR(44, "产品运营", "产品运营", true),

    ChannelDirector(45, "渠道总监", "渠道总监", true),
    ChannelManager(46, "渠道经理", "渠道经理", true),


    BigCustomerDirector(47, "大客户总监", "大客户总监", true),
    BigCustomerSales(48, "大客户销售", "大客户销售", true),
    BigCustomerPreSales(49, "大客户售前", "大客户售前", true),

    FunctionalPerson(50, "一般职能", "一般职能", true),
    RiskManager(51, "风控", "风控", true),

    ChannelOperator(52, "渠道运营", "渠道运营", true),
    MOCK_EXAM_MANAGER(53, "测评管理员", "测评管理员", true),
    CUSTOMER_SERVICE(54, "客服", "客服", true),
    CompanyEmployee(55, "公司员工", "公司员工市场除外", true)

    ;

    /**
     * 市场角色与权限key值映射关系
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum RoleAuthority{
        Admin(1, 1),
        Country(10, 2),
        CityManager(13, 4),
        BusinessDeveloper(21, 8),
        ;
        @Getter private final Integer id;
        @Getter private final Integer authKey;
    }

    @Getter private final Integer id;
    @Getter private final String roleName;
    @Getter private final String desc;
    @Getter private final boolean valid;  // 角色是否有效

    private final static Map<String, AgentRoleType> ROLE_TYPE_MAP = new LinkedHashMap<>();
    private final static List<Integer> ROLE_ID_LIST = new ArrayList<>();
    private final static Map<String, AgentRoleType> NAME_MAP = new LinkedHashMap<>();

    static {
        for (AgentRoleType roleType : AgentRoleType.values()) {

            // 去掉省代理
            //if (roleType == ProvinceAgent) {
            //    continue;
            //}

            ROLE_ID_LIST.add(roleType.getId());
            ROLE_TYPE_MAP.put(String.valueOf(roleType.getId()), roleType);
            NAME_MAP.put(roleType.name(),roleType);
        }
    }

    public static Map<String, AgentRoleType> getAllAgentRoles() {
        return ROLE_TYPE_MAP;
    }

    public static AgentRoleType of(Integer type) {
        if (type == null) {
            return null;
        }
        return ROLE_TYPE_MAP.get(String.valueOf(type));
    }

    /**
     * 获取直营角色列表
     *
     * @return
     */
    private static List<AgentRoleType> getDirectRoleList() {
        List<AgentRoleType> retList = new ArrayList<>();
        retList.add(Admin);
        retList.add(Finance);
        retList.add(DataViewer);
        retList.add(Country);
        retList.add(Region);
        retList.add(ProvinceManager);
        retList.add(CityManager);
        retList.add(BusinessDeveloper);
        retList.add(BUManager);
        retList.add(AreaManager);
        return retList;
    }

    /**
     * 获取代理角色列表
     *
     * @return
     */
    private static List<AgentRoleType> getAgentRoleList() {
        List<AgentRoleType> retList = new ArrayList<>();
        retList.add(Vendor);
        retList.add(ProvinceAgent);
        retList.add(CityAgent);
        retList.add(CityAgentLimited);
        return retList;
    }

    /**
     * 是否是直营角色
     *
     * @param roleType
     * @return
     */
    public static boolean isDirectRole(AgentRoleType roleType) {
        return getDirectRoleList().contains(roleType);
    }

    /**
     * 是否是代理角色
     *
     * @param roleType
     * @return
     */
    public static boolean isAgentRole(AgentRoleType roleType) {
        return getAgentRoleList().contains(roleType);
    }

    public static List<AgentRoleType> getMarketRoleList(){
        List<AgentRoleType> retList = new ArrayList<>();
        retList.add(Country);
        retList.add(BUManager);
        retList.add(Region);
        retList.add(AreaManager);
        retList.add(CityManager);
        retList.add(BusinessDeveloper);
        return retList;
    }

    public static List<AgentRoleType> getValidRoleList(){
        List<AgentRoleType> retList = new ArrayList<>();
        for (AgentRoleType roleType : AgentRoleType.values()) {
            if(roleType.isValid()){
                retList.add(roleType);
            }
        }
        return retList;
    }

    public static AgentRoleType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }
}
