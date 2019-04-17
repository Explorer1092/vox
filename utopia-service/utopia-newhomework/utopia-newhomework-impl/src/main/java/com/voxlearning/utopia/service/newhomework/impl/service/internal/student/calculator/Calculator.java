package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import java.util.Set;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/26
 */
public interface Calculator {
    CalculateResult calculate(String homeworkId, Set<String> processIds);
}
