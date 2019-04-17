package com.voxlearning.utopia.service.newhomework.api.context.bonus;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Getter
@Setter
public class QuestionDataAnswer implements Serializable {
    private static final long serialVersionUID = 519803258256121026L;
    private String questionId;              // 题ID
    private List<List<String>> answer;      // 用户答案
    private Long duration;                  // 完成时长

    public Boolean checkValid() {
        return !StringUtils.isEmpty(questionId) && duration == null && !answer.isEmpty() && !answer.get(0).isEmpty();
    }
}
