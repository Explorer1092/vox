package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * UnifiedExamApplyStatus
 *
 * @author song.wang
 * @date 2017/4/26
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UnifiedExamApplyStatus {
    SO_PENDING(1, "销运处理中"),
    SO_REJECTED(2, "销运已拒绝"),
    CR_PENDING(3, "内容库处理中"),
    CR_REJECTED(4, "内容库已拒绝"),
    CR_APPROVED(5, "内容库已通过"),
    REVOKE(6, "撤销");

    @Getter
    private final int id;
    @Getter
    private final String desc;

    private final static Map<Integer, UnifiedExamApplyStatus> statusMap = new HashMap<>();
    static {
        for (UnifiedExamApplyStatus status : UnifiedExamApplyStatus.values()) {
            statusMap.put(status.getId(), status);
        }
    }

    public static UnifiedExamApplyStatus of(Integer type) {
        return statusMap.get(type);
    }
}
