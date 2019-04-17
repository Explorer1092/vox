package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.Vacation;

import com.voxlearning.alps.core.util.ClassUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.RequireObjectiveConfigTypes;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/12/20
 */
@Named
public class VacationCalculatorManager extends SpringContainerSupport {
    private Map<ObjectiveConfigType, VacationCalculator> calculators = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.calculators = new HashMap<>();
        Map<String, VacationCalculator> beans = applicationContext.getBeansOfType(VacationCalculator.class);
        for (VacationCalculator bean : beans.values()) {
            Class<?> theClass = ClassUtils.filterCglibProxyClass(bean.getClass());
            RequireObjectiveConfigTypes annotation = theClass.getAnnotation(RequireObjectiveConfigTypes.class);
            if (annotation == null) continue;
            for (ObjectiveConfigType type : annotation.value()) {
                if (calculators.containsKey(type)) {
                    logger.error("Duplicated ObjectiveConfigType declared on template, check it");
                    throw new IllegalStateException();
                }
                calculators.put(type, bean);
            }
        }
    }

    public VacationCalculator getCalculator(ObjectiveConfigType type) {
        VacationCalculator calculator = this.calculators.get(type);
        if (calculator == null) {
            calculator = applicationContext.getBean(VacationCalculator_Default.class);
        }
        return calculator;
    }
}
