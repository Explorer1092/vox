package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

/**
 * LiveEnrollmentSchoolView
 *
 * @author song.wang
 * @date 2018/12/18
 */
@Data
public class LiveEnrollmentSchoolView {
    private Long schoolId;
    private String schoolName;
    private String cityName;
    private String countyName;
    private String distance;
    private Boolean isSignIn;   // 是否已签到
}
