package com.voxlearning.utopia.service.vendor.api.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/14
 */
@Getter
@Setter
@NoArgsConstructor
public class ClazzExpandSellInfoMapper implements Serializable {


    private static final long serialVersionUID = -1445621411015663603L;

    private List<String> studentName;
    private String orderProductType;
}
