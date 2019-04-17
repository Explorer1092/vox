package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author xinxin
 * @since 2/24/17.
 */
@Getter
@Setter
public class StudentFairylandProductMapper implements Serializable {
    private static final long serialVersionUID = -1929542111216083692L;

    private String id;
    private String name;    //产品名称
    private BigDecimal price;   //价格
    private BigDecimal originalPrice;   //原价
    private List<StudentFairylandProductItemMapper> items;

}
