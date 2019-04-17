package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.Data;

import java.util.List;

@Data
public class ChipsUserOrderBO implements VerificationBO {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private List<String> productIds;
    private String productName;
    private String refer;
    private String channel;

    private Long inviter;//邀请人 微信邀请活动使用
    private String saleStaffId;//地推人员id
    private String groupCode;//拼团码

    public ChipsUserOrderBO(Long userId, List<String> productIds) {
        this.userId = userId;
        this.productIds = productIds;
    }

    @Override
    public boolean parameterCheck() {
        if (userId == null || userId.compareTo(0L) <= 0) {
            return false;
        }

        if (CollectionUtils.isEmpty(productIds)) {
            return false;
        }
        return true;
    }
}
