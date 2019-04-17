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

package com.voxlearning.utopia.service.business.api.entity;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Alex on 15-1-16.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "daily_increasement_payment_data")
@DocumentIndexes({
        @DocumentIndex(def = "{'region_code':1,'date':1,'status':1}", background = true)
})
public class DailyIncreasementPaymentData implements Serializable {
    private static final long serialVersionUID = -251244452637879943L;

    @DocumentId private String id;
    private Integer status;
    private Integer region_code;         //区域编码
    private String region_name;          //区域名称
    private Integer date;                //统计的天

    // 总体订单数，购买人数和订单金额
    private Integer total_order_num;
    private Integer total_student_num;
    private Double total_order_amount;
    private Double total_cardpay_amount;
    private Double total_refund_amount;

    // 阿分题订单数，购买人数和订单金额
    private Integer afentiexam_order_num;
    private Integer afentiexam_student_num;
    private Double afentiexam_order_amount;
    private Double afentiexam_cardpay_amount;
    private Double afentiexam_refund_amount;

    // Picaro订单数，购买人数和订单金额
    private Integer kaplanpicaro_order_num;
    private Integer kaplanpicaro_student_num;
    private Double kaplanpicaro_order_amount;
    private Double kaplanpicaro_cardpay_amount;
    private Double kaplanpicaro_refund_amount;

    // 沃克大冒险订单数，购买人数和订单金额
    private Integer walker_order_num;
    private Integer walker_student_num;
    private Double walker_order_amount;
    private Double walker_cardpay_amount;
    private Double walker_refund_amount;

    // 走遍美国订单数，购买人数和订单金额
    private Integer travelamerica_order_num;
    private Integer travelamerica_student_num;
    private Double travelamerica_order_amount;
    private Double travelamerica_cardpay_amount;
    private Double travelamerica_refund_amount;

    // 爱儿优订单数，购买人数和订单金额
    private Integer iandyou100_order_num;
    private Integer iandyou100_student_num;
    private Double iandyou100_order_amount;
    private Double iandyou100_cardpay_amount;
    private Double iandyou100_refund_amount;

    // 进击的三国订单数，购买人数和订单金额
    private Integer sanguodmz_order_num;
    private Integer sanguodmz_student_num;
    private Double sanguodmz_order_amount;
    private Double sanguodmz_cardpay_amount;
    private Double sanguodmz_refund_amount;

    // 宠物大乱斗订单数，购买人数和订单金额
    private Integer petswar_order_num;
    private Integer petswar_student_num;
    private Double petswar_order_amount;
    private Double petswar_cardpay_amount;
    private Double petswar_refund_amount;

}
