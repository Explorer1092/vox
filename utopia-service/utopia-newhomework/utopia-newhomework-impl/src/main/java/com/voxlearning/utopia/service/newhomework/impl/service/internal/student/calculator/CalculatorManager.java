/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import com.voxlearning.alps.core.util.ClassUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/20
 */
@Named
public class CalculatorManager extends SpringContainerSupport {
    private Map<ObjectiveConfigType, Calculator> calculators = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.calculators = new HashMap<>();
        Map<String, Calculator> beans = applicationContext.getBeansOfType(Calculator.class);
        for (Calculator bean : beans.values()) {
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

    public Calculator getCalculator(ObjectiveConfigType type) {
        Calculator calculator = this.calculators.get(type);
        if (calculator == null) {
            calculator = applicationContext.getBean(Calculator_Default.class);
        }
        return calculator;
    }
}
