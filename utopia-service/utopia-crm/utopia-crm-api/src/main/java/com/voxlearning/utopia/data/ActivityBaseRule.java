package com.voxlearning.utopia.data;

import com.voxlearning.utopia.enums.ActivityDifficultyLevelEnum;
import com.voxlearning.utopia.enums.ActivityPatternEnum;
import com.voxlearning.utopia.enums.TwenTyFourExtent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 游戏规则
 */

@Getter
@Setter
public class ActivityBaseRule implements Serializable {

    private ActivityPatternEnum pattern; // 模式

    private Integer limitTime; // 时间限制 单位分钟

    private Integer limitAmount; // 数量限制

    private ActivityDifficultyLevelEnum level; // 难度等级

    private TwenTyFourExtent extent; // 24点出题区间

    private Integer playLimit;       // 学生在活动期间限制的答题次数
}
