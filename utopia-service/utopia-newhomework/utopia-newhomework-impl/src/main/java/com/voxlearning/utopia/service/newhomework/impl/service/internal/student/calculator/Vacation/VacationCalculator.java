package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.Vacation;

import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import java.util.Set;

/**
 * @author guoqiang.li
 * @since 2016/12/20
 */
public interface VacationCalculator {
    CalculateResult calculate(Set<String> processIds);
}
