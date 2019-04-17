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

package com.voxlearning.utopia.service.reward.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.reward.constant.RewardCouponResource;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

/**
 * Reward product entity data structure.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Jun 16, 2014
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_PRODUCT")
@UtopiaCacheRevision("20181112")
public class RewardProduct implements CacheDimensionDocument {
    private static final long serialVersionUID = 3706272423575317565L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentUpdateTimestamp
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;
    @DocumentField private String productName;
    @DocumentField private Boolean disabled;
    @DocumentField private Boolean onlined;
    @DocumentField private Integer displayOrder;
    @DocumentField private String description;
    @DocumentField private Double priceT;
    @DocumentField private Double priceOldT;     //老学校老师价格
    @DocumentField private Double priceS;        // 奖品中心叫原始积分
    @DocumentField private Double priceOldS;     // 老学校学生价格，奖品中心叫兑换积分
    @DocumentField private Integer soldQuantity;
    @DocumentField private Integer wishQuantity;
    @DocumentField private String tags;
    @DocumentField private String saleGroup;
    @DocumentField private Boolean teacherVisible;
    @DocumentField private Boolean studentVisible;
    @DocumentField private Boolean primarySchoolVisible;  // 小学可见
    @DocumentField private Boolean juniorSchoolVisible;   // 中学可见
    @DocumentField private Boolean osVisible;    //老学校可见
    @DocumentField private Boolean nsVisible;    //新学校可见
    @DocumentField private Date onlineDatetime;
    @DocumentField private Date offlineDatetime;
    @DocumentField private String productType;   //实物  虚拟
    @DocumentField private Boolean rebated;  // 兼容之前的兑换卷 两个字段 是否返利
    @DocumentField private String usedUrl;   // 虚拟奖品也可使用 使用URL
    @DocumentField private Integer teacherLevel;   // 此奖品需要的等级 （老师等级）
    @DocumentField private Integer ambassadorLevel;   // 此奖品需要的等级 （大使等级）
    @DocumentField private Double buyingPrice;          // 进货价
    @DocumentField private Integer studentOrderValue;   // 学生排序值
    @DocumentField private Integer teacherOrderValue;   // 老师排序值
    @DocumentField private Integer expiryDate;   // 有效期
    @DocumentField private String relateVirtualItemId; // 奖品关联的虚拟物品id
    @DocumentField private String displayTerminal;// 展示端
    @DocumentField private String relateVirtualItemContent; // 奖品关联的虚拟物品内容
    @DocumentField private String remarks;// 备注
    @DocumentField private Integer minBuyNums;// 起兑件数
    @DocumentField private String gradeVisible;// 年级可见性
    @DocumentField private Boolean repeatExchanged;//是否重复兑换
    @DocumentField private RewardCouponResource couponResource;
    @DocumentField private Integer spendType; //花费类型，默认0是学豆，1是碎片
    @DocumentField private Long oneLevelCategoryId; //一级分类Id
    @DocumentField private Long twoLevelCategoryId; //二级分类Id
    @DocumentField private Boolean isNewProduct; //新品标志


    public enum SpendType {
        INTEGRAL(0),
        FRAGMENT(1),
        ;
        private int type;
        SpendType(int type) {
            this.type = type;
        }

        public int intValue() {
            return type;
        }
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    @JsonIgnore
    public boolean isShiwu() {
        return Objects.equals(productType, RewardProductType.JPZX_SHIWU.name());
    }

    @JsonIgnore
    public boolean isTiyan() {
        return Objects.equals(productType, RewardProductType.JPZX_TIYAN.name());
    }

    @JsonIgnore
    public Double fetchProductPrice(User user) {
        if (user != null && user.isStudent()) {
            return priceOldS;
        } else if (user != null && user.isTeacher()) {
            return priceOldT;
        }

        return 0d;
    }

}

