package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 专员工作记录统计学校详情列表
 * Created by yaguang.wang
 * on 2017/9/29.
 */
@Getter
@Setter
@NoArgsConstructor
public class IntoSchoolResultListView {
    private Long schoolId;                          // 学校ID
    private String schoolName;                      // 学校名称
    private String lastVisitTime;                     // 最后一次拜访学校的时间
    private int visitTeacherCount;              // 拜访老师数
    private List<String> visitOtherTeacher;         // 其他老师
    private int visitCountLte30;                // 最近三天专员拜访学校次数
}
