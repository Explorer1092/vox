package com.voxlearning.utopia.service.vendor.api.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AfentiImprovedOrderSynchronizeContext extends OrderSynchronizeContext {
    private static final long serialVersionUID = -3390300350377431231L;
    private String picProItemId;
    private Integer picPrice;
}
