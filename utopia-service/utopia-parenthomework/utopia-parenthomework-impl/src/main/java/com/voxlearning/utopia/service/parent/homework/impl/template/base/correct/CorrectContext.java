package com.voxlearning.utopia.service.parent.homework.impl.template.base.correct;

import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.BaseContext;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 订正作业上下文
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Getter
@Setter
public class CorrectContext extends BaseContext {

    private Homework homework;//作业
    private HomeworkResult homeworkResult;//作业结果
    private List<HomeworkProcessResult> homeworkProcessResults;//作业结果详情
    private CorrectParam param;//参数
}
