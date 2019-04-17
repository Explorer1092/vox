package com.voxlearning.utopia.admin.viewdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 学校线索的详情
 * Created by yaguang.wang
 * on 2017/4/24.
 */
@Setter
@Getter
@NoArgsConstructor
public class SchoolClueDetailView {
    private String clueId;
    private String createApplyTime;
    private String recorderName;
    private String recorderPhone;
    private String photoUrl;
    private String coordinateType;               // GPS类型:wgs84 ,百度类型: bd09ll 高德类型:autonavi
    private String latitude;                     // 地理坐标：纬度
    private String longitude;                    // 地理坐标：经度
    private String dateTime;                     // 照片时间
    private String checkStatus;                  // 审核状态
    private String reviewer;                     // 审核人
    private String reviewerName;                 // 审核人姓名
    private String reviewNote;                   // 审核意见
    private String reviewTime;                     // 审核时间
    private String address;
    private Long updateTime;
}
