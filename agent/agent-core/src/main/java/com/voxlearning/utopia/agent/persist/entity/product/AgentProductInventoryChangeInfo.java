package com.voxlearning.utopia.agent.persist.entity.product;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 商品库存变更信息
 *
 * @author deliang.che
 * @since 2018/7/16
 **/

@Getter
@Setter
public class AgentProductInventoryChangeInfo implements Serializable {

    private static final long serialVersionUID = 5788262543233368126L;
    public static final Integer  INVENTORY_OPT_ADD = 1;   //增加库存标识
    public static final Integer  INVENTORY_OPT_REDUCE = -1;   //减少库存标识
    private Long id;                //商品ID
    private Integer inventoryOpt;   //库存操作（1：增加库存 -1：减少库存）
    private Integer quantity;        //库存数量
    private String quantityChangeDesc; //变更原因
}
