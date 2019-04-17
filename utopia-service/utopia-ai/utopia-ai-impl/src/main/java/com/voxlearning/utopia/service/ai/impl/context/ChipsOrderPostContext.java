package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import lombok.Data;

import java.util.Map;

@Data
public class ChipsOrderPostContext extends AbstractAIContext<ChipsOrderPostContext> {

    private static final long serialVersionUID = -8746327867170805154L;

    // in
    private String orderId;
    private Long userId;
    private Map<String, Object> param;


    private ChipsUserOrderExt orderExt = null;
    private UserOrder userOrder = null;
    private String userWechatName = "";


    // out
    private MapMessage result = MapMessage.successMessage();

}
