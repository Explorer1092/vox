package com.voxlearning.utopia.service.newhomework.api.entity.basicreview;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Getter
@Setter
public class BasicReviewHomeworkAnswer implements Serializable {

    private static final long serialVersionUID = 774111945554300767L;

    private Boolean grasp;          // 是否掌握
    private String questionId;
}
