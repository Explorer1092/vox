package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 专员每日数据
 * Created by yaguang.wang
 * on 2017/9/29.
 */

@Getter
@Setter
@NoArgsConstructor
public class BaseTodayIntoSchoolView {
    protected Integer intoSchoolCount = 0;        // 今日进校数
    protected double visitTeacherAvg = 0.0;   // 校均拜访老师数

    public Boolean isReach() {
        return intoSchoolCount >= 2 && visitTeacherAvg >= 2;
    }
}
