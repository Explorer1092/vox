package com.voxlearning.utopia.service.mizar.api.constants;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Yuechen.Wang on 2016/9/7.
 * 审核状态
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarAuditStatus {
    PENDING("待审核"),    // 待审核
    APPROVE("审核通过"),  // 审核通过
    REJECT("驳回"),       // 驳回
    ;
    @Getter private final String desc;

    private final static Map<String, MizarAuditStatus> DescriptionMap = new LinkedHashMap<>();

    static {
        for (MizarAuditStatus status : MizarAuditStatus.values()) {
            DescriptionMap.put(status.name(), status);
        }
    }

    public static MizarAuditStatus parse(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        return DescriptionMap.get(status);
    }
}
