package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 未布置作业老师视图详情
 * Created by yaguang.wang
 * on 2017/9/29.
 */
@Getter
@Setter
@NoArgsConstructor
public class UnAssignHomeworkTeacherView {
    private Long schoolId;                          // 学校ID
    private String schoolName;                      // 学校名称
    private List<String> visitTime;                 // 所有的访问时间
    private List<Map<String, String>> teacherInfo;  // 未布置作业的老师信息
    private Date lastVisitTime;                     // 最后一次拜访学校的时间
}
