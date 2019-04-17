package com.voxlearning.utopia.service.mizar.api.constants;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Yuechen.Wang on 2016/9/18.
 * 评论审核状态
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarRatingStatus {
    OFFLINE("离线"),    // 离线
    PENDING("待审核"),  // 待审核
    ONLINE("在线"),     // 在线
    DELETED("删除"),    // 已删除
    ;

    @Getter private final String desc;

    private final static Map<String, MizarRatingStatus> DescriptionMap = new LinkedHashMap<>();

    static {
        for (MizarRatingStatus status : MizarRatingStatus.values()) {
            DescriptionMap.put(status.name(), status);
        }
    }

    public static MizarRatingStatus parse(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        return DescriptionMap.get(status);
    }
}
