package com.voxlearning.utopia.service.mizar.api.entity.shop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 机构导流—活动产品类型
 */
@Getter
@Setter
@NoArgsConstructor
public class MizarGoodsItem implements Serializable {

    private String itemId;              // 产品ID
    private String categoryName;       // 一级名称
    private String itemName;           // 二级名称
    private Integer inventory;         // 库存量
    private Double price;               // 价格
    private Integer remains;           // 剩余数量
    
}

