package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.OutsideReadingDynamicCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/16
 */
@Getter
@Setter
public class ReadingAchievementResp extends BaseResp {
    private static final long serialVersionUID = -2555380628508282028L;

    private Double totalReadingCount;            // 阅读成就字数
    private Double avgReadingCount;          // 班级平均阅读成就字数
    //private int targetReadingCount;       // 课标要求字数

    private List<OutsideReadingDynamicCacheMapper> readingDynamics;
    private List<String> labels;
}
