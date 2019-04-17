package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @author malong
 * @since 2018/06/19
 */
@Data
public class CommodityOrderMapper implements Serializable {
    private static final long serialVersionUID = -6958964267439589230L;
    private String id;
    private Long studentId;
    private String phone;
    private String commodityName;
    private String sendStatus;
    private String sendWay;
    private String logisticsCode;
    private Integer coin;
    private String commodityCategory;
    private Integer categoryLevel;
    private String orderStatus;

}
