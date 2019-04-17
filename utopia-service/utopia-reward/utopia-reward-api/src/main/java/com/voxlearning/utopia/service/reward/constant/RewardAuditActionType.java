package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardAuditActionType {
    AUDIT_PASS("审核通过"),
    AUDIT_UNPASS("审核不通过");

    @Getter
    private final String description;
}
