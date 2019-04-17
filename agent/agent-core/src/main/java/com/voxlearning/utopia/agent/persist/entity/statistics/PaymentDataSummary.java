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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "payment_data_summary")
@DocumentIndexes({
        @DocumentIndex(def = "{'region_code':1,'date':1,'status':1}", background = true)
})
public class PaymentDataSummary implements Serializable {
    private static final long serialVersionUID = -251244452637871943L;

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

    private Map<String, ProductPaymentInfo> productPaymentData;

}