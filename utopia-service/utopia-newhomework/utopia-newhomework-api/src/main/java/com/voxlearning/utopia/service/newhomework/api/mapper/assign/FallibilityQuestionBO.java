package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/1/10 16:51
 */
@Setter
@Getter
public class FallibilityQuestionBO implements Serializable {
    private static final long serialVersionUID = -1544848760288629623L;
    private String timeSpan;    //周期时间
    private List<WeekWrongQuestionBO> weekWrongQuestionBOList;
}
