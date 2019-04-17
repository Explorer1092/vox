package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.utopia.entity.constant.TeacherTaskCalType;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;

import java.util.Map;

/**
 * 事件维度的变量计算器，每个任务所需要的事件，都会有一个实现类
 *
 * 设计理念：
 * 1.每一个事件可能会影响好几个任务的完成进度或者是任务中数据的变化
 * 2.每个任务所需要的上下文数据结构也不一定完全相同，但是事件又要必须共享
 * 3.为了兼容事件，在事件计算器中，我们保证数据的最大化，即各个任务的数据都是它的一个子集
 * 4.如果某个任务的数据结构确实太特殊、数据又过于庞大、复杂，导致结构混乱，难以理解，甚至对数据库产生了影响，则拆分到具体任务的INIT中去
 *
 * @author zhouwei
 */
public interface TeacherTaskEvaluator {

    /**
     * 根据事件返回该事件的计算器
     * @return
     * @author zhouwei
     */
    TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent();

    /**
     * 计算表达式
     * @param expression
     * @param varMap
     * @param resultKey
     * @param initValue
     * @param <T>
     * @return
     */
    <T> T evaluate(String expression, Map<String,Object> varMap, String resultKey, Object initValue);

    /**
     * 任务需要完成的时候，对时间变量与当前的上下文变量做合并
     * @param orgVarMap
     * @param newVarMap
     * @return
     * @author zhouwei
     */
    Map<String,Object> mergeVar(Map<String,Object> orgVarMap,Map<String,Object> newVarMap);

    /**
     *
     * 初始化相关任务所需时间的变量，不过需要初始化，请返回new HashMap<>();
     *
     * @param teacherTaskTpl
     * @param subTask
     * @return
     *
     * @author
     */
    Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask);
}
