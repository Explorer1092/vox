package com.voxlearning.utopia.agent.view.school;

import lombok.Getter;
import lombok.Setter;

/**
 * SchoolPositionData
 *
 * @author song.wang
 * @date 2018/7/19
 */
@Getter
@Setter
public class SchoolPositionData {
    private Long schoolId;
    private String schoolName;

    private String coordinateType;               // GPS类型:wgs84 ,百度类型: bd09ll 高德类型:autonavi
    private String latitude;                     // 地理坐标：纬度
    private String longitude;                    // 地理坐标：经度
    private String address;                      // 地址

    private String photoUrl;

}
