package com.voxlearning.utopia.service.newhomework.impl.service.processor.index;

import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Getter
@Setter
public class AbilityExamIndexContext extends AbstractContext<AbilityExamIndexContext> {

    private static final long serialVersionUID = 1501690319430422583L;

    // in
    private Long studentId;             // 学生id

    // out
    private Map<String, Object> dataMap = new HashMap<>();

}
