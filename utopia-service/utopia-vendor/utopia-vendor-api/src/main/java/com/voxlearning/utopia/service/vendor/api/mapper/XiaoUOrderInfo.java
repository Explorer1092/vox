package com.voxlearning.utopia.service.vendor.api.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/9
 */
@Getter
@Setter
@NoArgsConstructor
public class XiaoUOrderInfo implements Serializable {


    private static final long serialVersionUID = 34862547405547127L;


    private Long userId;
    private String orderProductServiceType;
}
