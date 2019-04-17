package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.entity.constant.TeacherTaskCalType;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;

import java.util.Map;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;

public abstract class AbstractTeacherTaskEvaluator implements TeacherTaskEvaluator {

    /** Log Component **/
    protected static Logger logger = LoggerFactory.getLogger(AbstractTeacherTaskEvaluator.class);

    /**
     * 获取任务的事件类型
     * @return
     */
    abstract public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent();

    @SuppressWarnings({"unchecked"})
    public <T> T evaluate(String expression, Map<String, Object> varMap, String resultKey, Object initValue) {
        try {
            JexlEngine engine = new JexlEngine();
            // 如果有新注入的变量，则先merge到一起，否则直接取原来的变量集合
            MapContext context = new MapContext(varMap);
            context.set(resultKey, initValue);

            engine.createExpression(expression).evaluate(context);
            return (T) context.get(resultKey);
        } catch (Throwable t) {
            logger.error("TT:Evaluate value error!",t);
            return null;
        }
    }
}
