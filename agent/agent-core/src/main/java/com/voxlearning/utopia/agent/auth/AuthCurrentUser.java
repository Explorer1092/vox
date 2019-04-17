/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.auth;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.utils.PathUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType.*;

/**
 * AuthCurrentUser
 * Created by Shuai.Huan on 2014/7/3.
 */
@Data
public class AuthCurrentUser implements Serializable {
    private Long userId;
    private String userName;
    private String realName;
    private String userPhone;
    private Integer status;                 // 用户状态，0:新建，强制更新密码，1:有效，9:关闭
    @Deprecated private String groupName;
    @Deprecated private List<String> authPathList;
    private List<Integer> roleList;
    private String deviceId;
    @Deprecated private Set<Integer> cityCodes; // 用户所在城市
    private Long shadowId; // 影子账号ID  当前用户可用影子账号使用自己没有权限的功能
    private List<String> pageElementCodes;
    private List<String> operationCodes;

    // 检查用户的权限
    public boolean checkSysAuth(String appName, String subSysPath) {
        return true;
    }

    public boolean isTargetRole(Integer roleId){
        if(CollectionUtils.isEmpty(roleList)){
            return false;
        }
        return roleList.contains(roleId);
    }

    public boolean isAdmin() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (Admin.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCountryManager() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (Country.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBuManager() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (BUManager.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRegionManager() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (Region.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAreaManager() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (AreaManager.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFinance() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (Finance.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCityAgent() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (CityAgent.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isProvinceAgent() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (ProvinceAgent.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCityAgentLimited() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (CityAgentLimited.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDataViewer() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (DataViewer.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCityManager() {
        if (roleList == null || roleList.size() == 0) {
            return false;
        }

        for (Integer roleId : roleList) {
            if (CityManager.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBusinessDeveloper() {
        if (CollectionUtils.isEmpty(roleList)) {
            return false;
        }
        for (Integer roleId : roleList) {
            if (BusinessDeveloper.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    // 是否是产品运营人员
    public boolean isProductOperator() {
        if (CollectionUtils.isEmpty(roleList)) {
            return false;
        }
        for (Integer roleId : roleList) {
            if (PRODUCT_OPERATOR.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    // 是否是大客户售前
    public boolean isBigCustomerPreSales() {
        if (CollectionUtils.isEmpty(roleList)) {
            return false;
        }
        for (Integer roleId : roleList) {
            if (BigCustomerPreSales.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }
}
