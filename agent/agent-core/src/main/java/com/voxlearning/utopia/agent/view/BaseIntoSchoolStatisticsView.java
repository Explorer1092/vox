package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 专员的进校统计
 * Created by yaguang.wang
 * on 2017/9/29.
 */
@Getter
@Setter
@NoArgsConstructor
public class BaseIntoSchoolStatisticsView {
    protected Integer intoSchoolCount = 0;    // 进校（次）
    protected Integer visitedSchoolCount = 0; // 已访问学校数
    protected Integer schoolTotal = 0;        // 学校总数
    protected double visitTeacherAvg = 0.0;     // 校均拜访老师数 当月板房老师总数(不含其它类,当月重复板房老师不去重)/当月进校次数
    protected String visitTeacherHwPro = "0";   // 拜访老师布置作业率 当月拜访老师在拜访后布置作业的老师数量（不包含其他）（需要去重）/当月拜访老师总数（不包含其他）(当月重复拜访老师需要去重)
}
