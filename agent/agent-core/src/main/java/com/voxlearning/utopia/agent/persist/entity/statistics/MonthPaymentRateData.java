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

package com.voxlearning.utopia.agent.persist.entity.statistics;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.*;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.crm.api.constants.ProductType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Alex on 15-1-31.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "month_payment_rate_data")
@DocumentIndexes({
        @DocumentIndex(def = "{'region_code':1}", background = true),
        @DocumentIndex(def = "{'month':-1}", background = true)
})
public class MonthPaymentRateData implements Serializable {
    private static final long serialVersionUID = -251241453635871243L;

    @DocumentId private String id;
    private Integer region_code;         //区域编码
    private String region_name;          //区域名称
    private Integer month;               //统计的月
    private Integer active_user_num;       //月活用户数
    private Integer auth_active_user_num;  //月活用户数据,
    private Integer total_pay_user_num;          //总付费用户数
    private Integer status;

    private Integer afentiexam_pay_user_num;     //阿分题付费用户数
    private Integer kaplanpicaro_pay_user_num;   //Picaro付费用户数
    private Integer walker_pay_user_num;         //沃克付费用户数
    private Integer travelamerica_pay_user_num;  //走遍美国付费用户数
    private Integer iandyou100_pay_user_num;     //爱儿优付费用户数
    private Integer sanguodmz_pay_user_num;      //进击的三国付费用户数
    private Integer petswar_pay_user_num;        //宠物大乱斗付费用户数

    public double getPaymentRate(String productName) {
        if (active_user_num == 0) {
            return 0d;
        } else {
            return getPayUserNum(productName) * 1.0d / active_user_num;
        }
    }

    public Integer getPayUserNum(String productName) {
        Integer payUserNum = 0;
        if (productName == null) {
            payUserNum = 0;
        } else if (productName.equals("所有")) {
            payUserNum = total_pay_user_num;
        } else if (productName.equals(ProductType.CARD_AFENTI.getCardName())) {
            payUserNum = SafeConverter.toInt(afentiexam_pay_user_num);
        } else if (productName.equals(ProductType.CARD_WALKER.getCardName())) {
            payUserNum = SafeConverter.toInt(walker_pay_user_num);
        } else if (productName.equals(ProductType.CARD_PICARO.getCardName())) {
            payUserNum = SafeConverter.toInt(kaplanpicaro_pay_user_num);
        } else if (productName.equals(ProductType.CARD_TRAVEL_AMERICA.getCardName())) {
            payUserNum = SafeConverter.toInt(travelamerica_pay_user_num);
        } else if (productName.equals(ProductType.CARD_IANDYOU100.getCardName())) {
            payUserNum = SafeConverter.toInt(iandyou100_pay_user_num);
        } else if (productName.equals(ProductType.CARD_SANGUODMZ.getCardName())) {
            payUserNum = SafeConverter.toInt(sanguodmz_pay_user_num);
        } else if (productName.equals(ProductType.CARD_PETSWAR.getCardName())) {
            payUserNum = SafeConverter.toInt(petswar_pay_user_num);
        }

        return payUserNum == null ? 0 : payUserNum;
    }
}