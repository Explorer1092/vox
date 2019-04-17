package com.voxlearning.utopia.service.newhomework.impl.service.processor.doData;

import com.voxlearning.utopia.service.newhomework.api.constant.AssignmentConfigType;
import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Getter
@Setter
public class AbilityExamDoContext extends AbstractContext<AbilityExamDoContext> {

    private static final long serialVersionUID = 4173885025667702418L;

    // in
    private Long studentId;

    //
    private AssignmentConfigType type = AssignmentConfigType.INTELLIGENCE_EXAM;

    // out
    private Map<String, Object> vars;
}
