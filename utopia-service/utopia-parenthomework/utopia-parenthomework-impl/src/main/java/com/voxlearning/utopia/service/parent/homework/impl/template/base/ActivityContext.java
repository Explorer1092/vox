package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动上下文
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
@Setter
@Getter
public class ActivityContext extends BaseContext {

    //local variables
    private Activity activity;//活动
    private UserActivity userActivity;//用户活动
    private Long studentId;//学生id
    private Long parentId;//家长id
    private String activityId;//活动id

}
