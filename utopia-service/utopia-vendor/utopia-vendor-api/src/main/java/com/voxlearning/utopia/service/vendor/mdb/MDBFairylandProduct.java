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
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductRedirectType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_FAIRYLAND_PRODUCT")
@DocumentDDL(path = "ddl/mdb/MDB_FAIRYLAND_PRODUCT.ddl")
public class MDBFairylandProduct implements Serializable {
    private static final long serialVersionUID = 116734781033691008L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("ID") private Long id;
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;
    @DocumentField private String platform;
    @DocumentField private String productType;
    @DocumentField private String appKey;
    @DocumentField private String productName;
    @DocumentField private String productDesc;
    @DocumentField private String productIcon;
    @DocumentField private String productRectIcon;
    @DocumentField private String backgroundImage;
    @DocumentField private String launchUrl;
    @DocumentField private String launchBtnText;
    @DocumentField private String status;
    @DocumentField private String suspendMessage;
    @DocumentField private Integer rank;
    @DocumentField private String operationMessage;
    @DocumentField private Boolean hotFlag;
    @DocumentField private Boolean newFlag;
    @DocumentField private Boolean disabled;
    @DocumentField private String usePlatformDesc;
    @DocumentField private Integer baseUsingNum;
    @DocumentField private FairylandProductRedirectType redirectType;
    @DocumentField private Boolean recommendFlag;
    @DocumentField private String catalogDesc;
    @DocumentField private String stagingLaunchUrl;
    @DocumentField private String bannerImage;
    @DocumentField private String descImage;
    @DocumentField private String promptMessage;

    public FairylandProduct transform() {
        FairylandProduct t = new FairylandProduct();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setUpdateDatetime(updateDatetime);
        t.setPlatform(platform);
        t.setProductType(productType);
        t.setAppKey(appKey);
        t.setProductName(productName);
        t.setProductDesc(productDesc);
        t.setProductIcon(productIcon);
        t.setProductRectIcon(productRectIcon);
        t.setBackgroundImage(backgroundImage);
        t.setLaunchUrl(launchUrl);
        t.setLaunchBtnText(launchBtnText);
        t.setStatus(status);
        t.setSuspendMessage(suspendMessage);
        t.setRank(rank);
        t.setOperationMessage(operationMessage);
        t.setHotFlag(hotFlag);
        t.setNewFlag(newFlag);
        t.setDisabled(disabled);
        t.setUsePlatformDesc(usePlatformDesc);
        t.setBaseUsingNum(baseUsingNum);
        t.setRedirectType(redirectType);
        t.setRecommendFlag(recommendFlag);
        t.setCatalogDesc(catalogDesc);
        t.setStagingLaunchUrl(stagingLaunchUrl);
        t.setBannerImage(bannerImage);
        t.setDescImage(descImage);
        t.setPromptMessage(promptMessage);
        return t;
    }
}