package com.voxlearning.utopia.admin.viewdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 学校审核列表的视图
 * Created by yaguang.wang
 * on 2017/4/24.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolClueView {
    private Long id;
    private Date schoolCreateTime;
    private Date schoolUpdateTime;
    private String provinceName;
    private String cityName;
    private String countyName;
    private String cmainName;
    private String schoolDistrict;
    private String shortName;
    private String schoolPhase;
    private String authenticationState;
}
