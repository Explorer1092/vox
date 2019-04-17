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
public enum MizarGoodsStatus {
    OFFLINE("离线"),    // 离线
    PENDING("待审核"),  // 待审核
    ONLINE("在线"),     // 在线
    ;

    @Getter private final String desc;

    private final static Map<String, MizarGoodsStatus> DescriptionMap = new LinkedHashMap<>();

    static {
        for (MizarGoodsStatus status : MizarGoodsStatus.values()) {
            DescriptionMap.put(status.name(), status);
        }
    }

    public static MizarGoodsStatus parse(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        return DescriptionMap.get(status);
    }
}
