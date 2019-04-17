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

package com.voxlearning.utopia.service.vendor.mdb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentDDL;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_VENDOR_APPS")
@DocumentDDL(path = "ddl/mdb/MDB_VENDOR_APPS.ddl")
public class MDBVendorApps implements Serializable {
    private static final long serialVersionUID = 8714023069874429959L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("ID") private Long id;
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;
    @DocumentField("DISABLED") private Boolean disabled;
    @DocumentField("VENDOR_ID") private Long vendorId;
    @DocumentField("CNAME") private String cname;
    @DocumentField("ENAME") private String ename;
    @DocumentField("SHORT_NAME") private String shortName;
    @DocumentField("APP_URL") private String appUrl;
    @DocumentField("VITALITY_TYPE") private Integer vitalityType;
    @DocumentField("INTEGRAL_TYPE") private Integer integralType;
    @DocumentField("APP_ICON") private String appIcon;
    @DocumentField("APP_KEY") private String appKey;
    @DocumentField("SECRET_KEY") private String secretKey;
    @DocumentField("CALLBACK_URL") private String callBackUrl;
    @DocumentField("PURCHASE_URL") private String purchaseUrl;
    @DocumentField("DAY_MAX_ACCESS") private Long dayMaxAccess;
    @DocumentField("DAY_MAX_ADD_PK") private Integer dayMaxAddPK;
    @DocumentField("DAY_MAX_ADD_INTEGRAL") private Integer dayMaxAddIntegral;
    @DocumentField("STATUS") private String status;
    @DocumentField("RUNTIME_MODE") private Integer runtimeMode;
    @DocumentField("SERVER_IPS") private String serverIps;
    @DocumentField("SUSPEND_MESSAGE") private String suspendMessage;
    @DocumentField("RANK") private Integer rank;
    @Deprecated @DocumentField("IS_PAYMENT_FREE") private Boolean isPaymentFree;
    @Deprecated @DocumentField("IS_EDU_APP") private Boolean isEduApp;
    @DocumentField("PLAY_SOURCES") private String playSources;
    @DocumentField("APPM_URL") private String appmUrl;
    @DocumentField("VERSION") private String version;
    @DocumentField("APPM_ICON") private String appmIcon;
    @DocumentField("WECHAT_BUY_FLAG") private Boolean wechatBuyFlag;
    @DocumentField("SUBHEAD") private String subhead;
    @DocumentField("ORIENTATION") private String orientation;
    @DocumentField("CLAZZ_LEVEL") private String clazzLevel;
    @DocumentField("DESCRIPTION") private String description;
    @DocumentField("VIRTUAL_ITEM_EXIST") private Boolean virtualItemExist;
    @DocumentField("BROWSER") private String browser;
    @DocumentField("VERSION_ANDROID") private String versionAndroid;
    @DocumentField("IOS_PARENT_VERSION") private String iosParentVersion;
    @DocumentField("ANDROID_PARENT_VERSION") private String androidParentVersion;

    public VendorApps transform() {
        VendorApps t = new VendorApps();
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
