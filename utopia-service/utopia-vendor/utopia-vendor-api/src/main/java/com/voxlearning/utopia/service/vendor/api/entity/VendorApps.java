/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorApps;
import lombok.Getter;
import lombok.Setter;

/**
 * The 3rd Vendor App Information
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @serial
 * @since 2014-06-6
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_vendor")
@DocumentTable(table = "VOX_VENDOR_APPS")
public class VendorApps extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 4306459661360543742L;

    @DocumentField("DISABLED") private Boolean disabled;                            // 是否已被删除
    @DocumentField("VENDOR_ID") private Long vendorId;                              // VENDOR ID
    @DocumentField("CNAME") private String cname;                                   // 中文名称
    @DocumentField("ENAME") private String ename;                                   // 英文名称
    @DocumentField("SHORT_NAME") private String shortName;                          // 略称
    @DocumentField("APP_URL") private String appUrl;                                // 应用URL
    @DocumentField("VITALITY_TYPE") private Integer vitalityType;                   // 活力值类型
    @DocumentField("INTEGRAL_TYPE") private Integer integralType;                   // 银币类型
    @DocumentField("APP_ICON") private String appIcon;                              // 应用图标
    @DocumentField("APP_KEY") private String appKey;                                // APP Key
    @DocumentField("SECRET_KEY") private String secretKey;                          // SECRET KEY
    @DocumentField("CALLBACK_URL") private String callBackUrl;                      // CALLBACK URL
    @DocumentField("PURCHASE_URL") private String purchaseUrl;                      // PURCHASE URL
    @DocumentField("DAY_MAX_ACCESS") private Long dayMaxAccess;                     // 单日最大访问次数
    @DocumentField("DAY_MAX_ADD_PK") private Integer dayMaxAddPK;                   // 单日最大增加PK次数
    @DocumentField("DAY_MAX_ADD_INTEGRAL") private Integer dayMaxAddIntegral;       // 单日最大增加银币数
    @DocumentField("STATUS") private String status;                                 // 状态 ONLINE/OFFLINE/SUSPEND
    @DocumentField("RUNTIME_MODE") private Integer runtimeMode;                     // 运行环境
    @DocumentField("SERVER_IPS") private String serverIps;                          // 对方服务器IP白名单
    @DocumentField("SUSPEND_MESSAGE") private String suspendMessage;                // 暂时冻结状态的提示信息
    @DocumentField("RANK") private Integer rank;                                    // 展示顺序
    @Deprecated @DocumentField("IS_PAYMENT_FREE") private Boolean isPaymentFree;                // 是否免费APP
    @Deprecated @DocumentField("IS_EDU_APP") private Boolean isEduApp;              // 是否教育类APP
    @DocumentField("PLAY_SOURCES") private String playSources;                      // 学生哪端可以玩 OperationSourceType.java 逗号分隔
    @DocumentField("APPM_URL") private String appmUrl;                              // 移动端应用URL
    @DocumentField("VERSION") private String version;                               // 学生端ios最低版本
    @DocumentField("APPM_ICON") private String appmIcon;                            // 移动端应用图标地址
    @DocumentField("WECHAT_BUY_FLAG") private Boolean wechatBuyFlag;                // 是否开放家长端购买（家长app和家长微信）
    @DocumentField("SUBHEAD") private String subhead;                               // 副标题
    @DocumentField("ORIENTATION") private String orientation;                       // 屏幕适应，横屏landscape竖屏portrait自适应sensor
    @DocumentField("CLAZZ_LEVEL") private String clazzLevel;                        // 适配年级 多个逗号分隔 例：1,2,3
    @DocumentField("DESCRIPTION") private String description;                       // 应用描述
    @DocumentField("VIRTUAL_ITEM_EXIST") private Boolean virtualItemExist;          // 是否出售虚拟产品
    @DocumentField("BROWSER") private String browser;                               // 浏览器内核
    @DocumentField("VERSION_ANDROID") private String versionAndroid;                // 学生端android最低版本
    @DocumentField("IOS_PARENT_VERSION") private String iosParentVersion;           // 家长端ios最低版本
    @DocumentField("ANDROID_PARENT_VERSION") private String androidParentVersion;   // 家长端android最低版本

    @Override
    public String[] generateCacheDimensions() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    @JsonIgnore
    public boolean isOffLine() {
        return status == null || status.equals("OFFLINE");
    }

    @JsonIgnore
    public boolean isSuspend() {
        return status != null && status.equals("SUSPEND");
    }

    public boolean matchRuntimeMode(Integer runtimeMode) {
        return runtimeMode != null && runtimeMode <= this.runtimeMode;
    }

    public boolean matchPlaySources(OperationSourceType type) {
        if (type == null || playSources == null || playSources.trim().length() == 0) {
            return false;
        }
        for (String s : playSources.split(",")) {
            if (type.name().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVisible(Integer runtimeMode) {
        return !isDisabledTrue() && !isOffLine() && matchRuntimeMode(runtimeMode);
    }

    public boolean validateClazzLevel(int level) {
        if (this.clazzLevel == null || this.clazzLevel.trim().length() == 0) {
            return true;
        }
        String[] clazzLevels = this.clazzLevel.split(",");
        if (clazzLevels.length <= 0) {
            return true;
        }
        for (String clazzLevel : clazzLevels) {
            int i;
            try {
                i = Integer.parseInt(clazzLevel.trim());
            } catch (Exception ex) {
                i = 0;
            }
            if (i == level) {
                return true;
            }
        }
        return false;
    }

    public MDBVendorApps transform() {
        MDBVendorApps t = new MDBVendorApps();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setUpdateDatetime(updateDatetime);
        t.setDisabled(disabled);
        t.setVendorId(vendorId);
        t.setCname(cname);
        t.setEname(ename);
        t.setShortName(shortName);
        t.setAppUrl(appUrl);
        t.setVitalityType(vitalityType);
        t.setIntegralType(integralType);
        t.setAppIcon(appIcon);
        t.setAppKey(appKey);
        t.setSecretKey(secretKey);
        t.setCallBackUrl(callBackUrl);
        t.setPurchaseUrl(purchaseUrl);
        t.setDayMaxAccess(dayMaxAccess);
        t.setDayMaxAddPK(dayMaxAddPK);
        t.setDayMaxAddIntegral(dayMaxAddIntegral);
        t.setStatus(status);
        t.setRuntimeMode(runtimeMode);
        t.setServerIps(serverIps);
        t.setSuspendMessage(suspendMessage);
        t.setRank(rank);
        t.setIsPaymentFree(isPaymentFree);
        t.setIsEduApp(isEduApp);
        t.setPlaySources(playSources);
        t.setAppmUrl(appmUrl);
        t.setVersion(version);
        t.setAppmIcon(appmIcon);
        t.setWechatBuyFlag(wechatBuyFlag);
        t.setSubhead(subhead);
        t.setOrientation(orientation);
        t.setClazzLevel(clazzLevel);
        t.setDescription(description);
        t.setVirtualItemExist(virtualItemExist);
        t.setBrowser(browser);
        t.setVersionAndroid(versionAndroid);
        t.setIosParentVersion(iosParentVersion);
        t.setAndroidParentVersion(androidParentVersion);
        return t;
    }
}
