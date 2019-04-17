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

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductRedirectType;
import com.voxlearning.utopia.service.vendor.mdb.MDBFairylandProduct;
import lombok.Getter;
import lombok.Setter;

import static com.voxlearning.alps.annotation.common.Mode.STAGING;

/**
 * @author peng
 * @since 16-6-23
 * 课外乐园产品描述信息
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_vendor")
@DocumentTable(table = "VOX_FAIRYLAND_PRODUCT")
public class FairylandProduct extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 5301555263997780640L;

    @DocumentField private String platform;                             //所属平台STUDENT_PC STUDENT_APP,PARENT_APP
    @DocumentField private String productType;                          //产品类型 APPS BOOKS
    @DocumentField private String appKey;                               //如果是ProductType 是app的话 需要填入appKey
    @DocumentField private String productName;                          //产品名称
    @DocumentField private String productDesc;                          //产品详细描述
    @DocumentField private String productIcon;                          //产品图标地址
    @DocumentField private String productRectIcon;                      //长方形图标
    @DocumentField private String backgroundImage;                      //背景图片地址
    @DocumentField private String launchUrl;                            //点击按钮跳转地址
    @DocumentField private String launchBtnText;                        //点击按钮文字
    @DocumentField private String status;                               //状态 未上架，上架中，服务错误中
    @DocumentField private String suspendMessage;                       //维护信息
    @DocumentField private Integer rank;                                //产品排名
    @DocumentField private String operationMessage;                     //运营消息，如果配置显示，如果不配置显示开通人数
    @DocumentField private Boolean hotFlag;                             //热点标记是否打开
    @DocumentField private Boolean newFlag;                             //新产品标记是否打开
    @DocumentField private Boolean disabled;
    @DocumentField private String usePlatformDesc;                      //可以使用平台描述
    @DocumentField private Integer baseUsingNum;                        //正在使用人数基数
    @DocumentField private FairylandProductRedirectType redirectType;   //跳转类型
    @DocumentField private Boolean recommendFlag;                       //是否推荐标记
    @DocumentField private String catalogDesc;                          //类别描述
    @DocumentField private String stagingLaunchUrl;                     //预发布环境地址
    @DocumentField private String bannerImage;                          //购买页面banner图
    @DocumentField private String descImage;                            //购买页面详细介绍图
    @DocumentField private String promptMessage;                        //购买详情页面弹框描述


    @Override
    public String[] generateCacheDimensions() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }


    /**
     * 获取当前产品进入游戏地址，为空表示无法进入游戏
     * pc直接加载地址
     * app加载中间跳转地址
     */
    public String fetchRedirectUrl(Mode mode) {
        if (FairyLandPlatform.STUDENT_PC.name().equals(platform)) {
            return mode == STAGING ? stagingLaunchUrl : launchUrl;
        }
        String redirectUrl = FairylandProductRedirectType.fetchAppRedirectUrl(appKey, platform, productType);
        if (mode == STAGING && StringUtils.isBlank(stagingLaunchUrl)) {
            return null;
        } else if (mode != STAGING && StringUtils.isBlank(launchUrl)) {
            return null;
        }
        return redirectUrl;
    }

    public MDBFairylandProduct transform() {
        MDBFairylandProduct t = new MDBFairylandProduct();
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