package com.voxlearning.utopia.service.vendor.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 8/8/17.
 */
@Getter
@Setter
public class OrderSynchronizeBookInfo implements Serializable {
    private static final long serialVersionUID = -6626595025608959520L;

    private String source;  //waiyan，renjiao,...
    private String bookId;
    private Integer period;
    private Integer price;  //对应OrderProductItem的价格
}
