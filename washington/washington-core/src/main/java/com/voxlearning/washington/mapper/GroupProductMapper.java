package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Summer on 2017/9/28.
 */
@NoArgsConstructor
@Getter
@Setter
public class GroupProductMapper implements Serializable {

    private static final long serialVersionUID = -45848473769196100L;

    private OrderProductItem picListenBookItem;
    private OrderProduct product;
}
