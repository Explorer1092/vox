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

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.*;

import java.util.Arrays;
import java.util.Date;

/**
 * Reward category entity data structure.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Jul 14, 2014
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_CATEGORY")
@UtopiaCacheRevision("20180417")
public class RewardCategory implements CacheDimensionDocument {
    private static final long serialVersionUID = -2372346131277821985L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") Date createDatetime;
    @DocumentUpdateTimestamp
    @DocumentField("UPDATE_DATETIME") Date updateDatetime;
    @DocumentField private Long parentId;          //暂时预留 目前奖品中心分类全一级
    @DocumentField private String categoryName;
    @DocumentField private String categoryCode;   // 子类别的编码
    @DocumentField private String productType;    //属于实物 OR 体验
    @DocumentField private Boolean teacherVisible;
    @DocumentField private Boolean studentVisible;
    @DocumentField private Boolean primaryVisible; //是否小学可见
    @DocumentField private Boolean juniorVisible; //是否中学可见
    @DocumentField private Boolean display;
    @DocumentField private Integer displayOrder;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    /**
     * 二级分类
     */
    @Getter
    public enum SubCategory {
        NONE("未知"),
        HEAD_WEAR("头像装扮"),
        TOBY_WEAR("托比装扮"),
        MINI_COURSE("微课"),
        @Deprecated
        CHOICEST_ARTICLE("精品文章"),
        COUPON("兑换优惠券", true),
        FLOW_PACKET("流量包", true),
        COURSE_WARE("课件", false, COUPON);

        private String name;
        private boolean needVerifyMobile;   // 是否需要验证手机号
        private SubCategory parent;     // 父类级别，子类和父类走相同的流程

        SubCategory(String name){
            this(name,false,null);
        }

        SubCategory(String name, boolean verify){
            this(name,verify,null);
        }

        SubCategory(String name, boolean verify, SubCategory parent){
            this.name = name;
            this.needVerifyMobile = verify;
            this.parent = parent;
        }

        public boolean belongCoupon(){
            return this == COUPON || this.parent == COUPON;
        }

        public static SubCategory parseByName(String name) {
            return Arrays.stream(SubCategory.values())
                    .filter(c -> c.getName().equals(name))
                    .findAny()
                    .orElse(NONE);
        }

        public static SubCategory parseByCode(String code){
            return Arrays.stream(SubCategory.values())
                    .filter(c -> c.name().equals(code))
                    .findAny()
                    .orElse(NONE);
        }
    }
}
