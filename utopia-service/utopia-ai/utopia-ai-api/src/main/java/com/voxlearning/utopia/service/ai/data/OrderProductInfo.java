package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class OrderProductInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String productId;
    private String productName;
    private Integer courses;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Date beginDate;
    private Boolean paid;
    private Integer grade;
    private Integer surplus;

}
