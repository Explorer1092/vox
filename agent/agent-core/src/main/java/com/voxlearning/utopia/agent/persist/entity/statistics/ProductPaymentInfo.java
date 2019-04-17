package com.voxlearning.utopia.agent.persist.entity.statistics;


import lombok.Data;

import java.io.Serializable;

/**
 *
 * Created by alex on 2015/11/18.
 */
@Data
public class ProductPaymentInfo implements Serializable {
    private static final long serialVersionUID = -251244452637871944L;

    private Integer order_count;
    private Integer pay_user_count;
    private Double order_amount;
    private Double card_pay_amount;
    private Double refund_amount;

}
