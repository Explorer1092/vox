package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * LiveEnrollmentView
 *
 * @author song.wang
 * @date 2018/12/18
 */
@Data
public class LiveEnrollmentView {
    private String id;
    private Long schoolId;
    private String schoolName;
    private Long userId;
    private Date workTime;
    private String address;
    private List<String> partners;
    private Long distance;
}
