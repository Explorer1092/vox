package com.voxlearning.utopia.admin.viewdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 学校审核详情页
 * Created by yaguang.wang
 * on 2017/4/24.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolCheckDetailView {
    private Long schoolId;
    private Date schoolCreateDate;
    private String provinceName;
    private String cityName;
    private String countyName;
    private String fullName;
    private String shortName;
    private String authenticationState;
    private String schoolPhase;
    private String schoolLength;
    private String englishStartGrade;
    private Integer schoolSize;
    private String address;
    private List<SchoolClueDetailView> clues;
}
