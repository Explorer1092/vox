package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 家长通孩子完成任务领取学豆的任务类型
 * @author malong
 * @since 2017/2/23
 */
@Getter
@RequiredArgsConstructor
public enum HomeWorkReportMissionType {
    ERROR_CORRECTION("错题订正"),
    PIC_LISTEN_MISSION("点读机任务"),
    ;

    private final String desc;

}
